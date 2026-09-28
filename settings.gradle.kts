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
// See "build.fabric.gradle.kts" for Fabric.
// See "build.forge.gradle.kts" for Forge.
// See "build.neoforge.gradle.kts" for NeoForge 1.20.2+.
// See "build.neoforge-old.gradle.kts" for NeoForge 1.20.1.
// See "stonecutter.gradle.kts" for Stonecutter.

// Plugins.
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") // Fabric.
        maven("https://maven.minecraftforge.net/") // Forge.
        maven("https://maven.neoforged.net/releases/") // NeoForge.
        maven("https://maven.kikugie.dev/releases") // LoomX.
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("dev.kikugie.loom-back-compat") version "0.4.3"
}

// Project.
rootProject.name = "HCsCR"

// Prepare the list of versions and types.
val types = listOf("fabric", "forge", "neoforge")
val versions = (file("dev/versions/versions_beta.txt").readLines()
        + file("dev/versions/versions_active.txt").readLines()
        + file("dev/versions/versions_legacy.txt").readLines())
    .filter { it.isNotEmpty() && !it.startsWith('#') }
    .toSet()

// Ignored version IDs. See that file for reasoning on such ignorance.
val ignoredIds = file("dev/versions/ignored.txt").readLines()
    .filter { it.isNotEmpty() && !it.startsWith('#') }
    .toSet()

// Actively supported version system. See README.md for the support policy.
// Depends on the "ru.vidtu.hcscr.legacy" boolean system property:
// - "false" (default): Compile only versions listed in "supportedVersions".
// - "true": Compile all versions listed in "versions".
// If the "single version" feature is used, this is ignored.
val supportedVersions = (file("dev/versions/versions_beta.txt").readLines()
        + file("dev/versions/versions_active.txt").readLines())
    .filter { it.isNotEmpty() && !it.startsWith('#') }
    .toSet()
require(versions.containsAll(supportedVersions)) { "Not all actively supported versions '${supportedVersions}' are listed in all supported versions '${versions}'." }
val legacyMode = System.getProperty("ru.vidtu.hcscr.legacy").toBoolean()

// Process the "single" version feature.
// Pass the "ru.vidtu.hcscr.single" system property with "<version>-<type>"
// to the Gradle daemon, and it will compile only* the required version,
// which may reduce the build time if you don't need other versions.
// (* Sometimes, the latest version will also be compiled due to how this works)
val singleId: String? = System.getProperty("ru.vidtu.hcscr.single")
val latestId = "${versions.first()}-${types.first()}"

// Check the "single version" feature validity.
if (singleId != null) {
    logger.warn("Processing single version '${singleId}' via 'ru.vidtu.hcscr.single'.")
    val idx = singleId.indexOf('-')
    require(idx != -1) { "Invalid single version '${singleId}', no '-' delimiter extracted from 'ru.vidtu.hcscr.single'." }
    val singleVersion = singleId.take(idx)
    val singleType = singleId.substring(idx + 1)
    require(singleVersion in versions) { "Invalid single version '${singleId}', version number '${singleVersion}' extracted from 'ru.vidtu.hcscr.single' not found in ${versions.joinToString()}." }
    require(singleType in types) { "Invalid single version '${singleId}', type '${singleType}' extracted from 'ru.vidtu.hcscr.single' not found in ${types.joinToString()}." }
}

// Setup stonecutter.
stonecutter {
    // Enable kts support.
    kotlinController = true

    // Setup.
    create(rootProject) {
        // Create projects.
        for (version in versions) {
            // Process the "supported" versions.
            // Note: There's no concept of "supported" loaders.
            if ((singleId == null) && !legacyMode && (version !in supportedVersions)) continue

            // Iterate types.
            for (type in types) {
                // Extract the ID.
                val id = "${version}-${type}"

                // Process the "single version" ID.
                if ((singleId != null) && (id != singleId) && (id != latestId)) continue

                // Check if version ID is ignored.
                if (id in ignoredIds) continue

                // Set up the project.
                val project = version(id, version)
                if (id == "1.20.1-neoforge") {
                    // NeoForge 1.20.1 is a piece of hacky mess that's basically
                    // Forge 1.20.1 with a "95% OFF" discount. It is loosely
                    // Forge, but not Forge. It uses Forge packages, but
                    // diverges from (can't keep up with) the (Lex) MCForge
                    // 1.20.1. I don't know why support this edge case
                    // for approximately 6 or 7 users total.
                    project.buildscript = "build.neoforge-old.gradle.kts"
                } else {
                    project.buildscript = "build.${type}.gradle.kts"
                }
            }
        }

        // Make the VCS version the latest one.
        vcsVersion = latestId
    }
}

// Log about mode.
val mode = when {
    (singleId != null) -> "Single:${singleId}"
    (legacyMode) -> "Legacy"
    else -> "Normal"
}
logger.lifecycle("Mode: '${mode}'.")
