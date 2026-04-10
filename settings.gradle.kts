/*
 * HCsCR is a third-party mod for Minecraft Java Edition
 * that allows removing the end crystals faster.
 *
 * Copyright (c) 2023 Offenderify
 * Copyright (c) 2023-2026 VidTu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

// This is the root Gradle entrypoint. It installs the Stonecutter preprocessor,
// and various root Gradle things, as well as includes and generates every
// virtual subproject by the Stonecutter. Also includes compile-time project.
// See "build.fabric-intermediary.gradle.kts" for legacy Intermediary Fabric.
// See "build.fabric-mojmap.gradle.kts" for modern Mojmap Fabric.
// See "build.forge.gradle.kts" for Forge.
// See "build.neoforge.gradle.kts" for NeoForge.
// See "build.neoforge-hacky.gradle.kts" for NeoForge ugly hack for 1.20.1.
// See "compile" for the compile-time constants and Blossom configuration.
// See "stonecutter.gradle.kts" for the Stonecutter configuration.

// Plugins.
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") // Architectury Loom. (Fabric dependencies)
        maven("https://maven.architectury.dev/") // Architectury Loom.
        maven("https://maven.minecraftforge.net/") // Architectury Loom. (Forge dependencies)
        maven("https://maven.neoforged.net/releases/") // Architectury Loom. (NeoForge dependencies)
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9"
}

// Project.
rootProject.name = "HCsCR"

// Compile-time constants. (Blossom)
include(":compile")

// Prepare the list of versions and types.
val types = listOf("fabric", "forge", "neoforge")
val versions = listOf("26.1.2", "1.21.11", "1.21.10", "1.21.8", "1.21.5", "1.21.4", "1.21.3", "1.21.1", "1.20.6", "1.20.4", "1.20.2", "1.20.1", "1.19.4", "1.19.2", "1.18.2", "1.17.1", "1.16.5")

// Process the "only" version feature.
// Pass the "ru.vidtu.hcscr.only" system property with "<version>-<type>"
// to the Gradle daemon and it will compile only* the required version,
// which may reduce the build time if you don't need other versions.
// (* Sometimes, the latest version will also be compiled due to how this works)
val onlyId = System.getProperty("ru.vidtu.hcscr.only")
val latestId = "${versions[0]}-${types[0]}"

// Check the "only" version validity.
if (onlyId != null) {
    logger.warn("Processing only version '${onlyId}' via 'ru.vidtu.hcscr.only'.")
    val idx = onlyId.indexOf('-')
    require(idx != -1) { "Invalid only version '${onlyId}', no '-' delimiter extracted from 'ru.vidtu.hcscr.only'." }
    val onlyVersion = onlyId.substring(0, idx)
    val onlyType = onlyId.substring(idx + 1)
    require(versions.contains(onlyVersion)) { "Invalid only version '${onlyId}', version number '${onlyVersion}' extracted from 'ru.vidtu.hcscr.only' not found in ${types.joinToString()}." }
    require(types.contains(onlyType)) { "Invalid only version '${onlyId}', type '${onlyType}' extracted from 'ru.vidtu.hcscr.only' not found in ${versions.joinToString()}." }
}

// Create the "ignored" version list.
// It serves no real purpose, just to warn.
val ignored = mutableListOf<String>()

// Setup stonecutter.
stonecutter {
    // Enable kts support.
    kotlinController = true

    // Setup.
    create(rootProject) {
        // Create projects.
        for (version in versions) {
            for (type in types) {
                // Extract the ID.
                val id = "${version}-${type}"
                if (id == "1.16.5-forge") continue // FIXME

                // Process the "only" version.
                if ((onlyId != null) && (id != onlyId) && (id != latestId)) continue

                // Check if version is ignored.
                val subPath = file("versions/${id}")
                if (subPath.resolve(".ignored").isFile) {
                    ignored.add(id)
                    continue
                }

                // Setup the project.
                val project = version(id, version)
                if (type == "fabric") {
                    // Fabric builds require "special care",
                    // because they use different plugin systems:
                    // - "intermediary" (remapped) for older (<=1.21.11) versions.
                    // - "mojmap" (non-remapped) for newer (>=26.1) versions.
                    val flavor = if (version.startsWith("1.")) "intermediary" else "mojmap"
                    project.buildscript = "build.fabric-${flavor}.gradle.kts"
                } else if (id == "1.20.1-neoforge") {
                    // NeoForge 1.20.1 is a piece of hacky mess that's basically
                    // Forge 1.20.1 with a "95% OFF" discount. It is loosely
                    // Forge, but not Forge. It uses Forge packages, but
                    // diverges from (can't keep up with) the (Lex) MCForge
                    // 1.20.1. I don't know why support this edge case
                    // for approximately 6 or 7 users total.
                    project.buildscript = "build.neoforge-hacky.gradle.kts"
                } else {
                    project.buildscript = "build.${type}.gradle.kts"
                }
            }
        }

        // Make the VCS version the latest one.
        vcsVersion = latestId
    }
}

// Warn about ignored versions.
if (ignored.isNotEmpty()) {
    logger.warn("Ignored versions: ${ignored.joinToString()}")
}
