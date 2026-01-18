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
// See "build.gradle.kts" for the per-version Gradle buildscript.
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
    id("dev.kikugie.stonecutter") version "0.8.3"
}

// Project.
rootProject.name = "HCsCR"

// Compile-time constants. (Blossom)
include(":compile")

// Stonecutter.
val types = listOf("fabric", "forge", "neoforge")
val versions = listOf("1.21.11", "1.21.10", "1.21.8", "1.21.5", "1.21.4", "1.21.3", "1.21.1", "1.20.6", "1.20.4", "1.20.2", "1.20.1", "1.19.4", "1.19.2", "1.18.2", "1.17.1", "1.16.5")
val ignored = mutableListOf<String>()
val onlyId = System.getProperty("ru.vidtu.hcscr.only")
val latestId = "${versions[0]}-${types[0]}"
if (onlyId != null) {
    logger.warn("Processing only version '${onlyId}' via 'ru.vidtu.hcscr.only'.")
    val idx = onlyId.indexOf('-')
    require(idx != -1) { "Invalid only version '${onlyId}', no '-' delimiter extracted from 'ru.vidtu.hcscr.only'." }
    val onlyVersion = onlyId.substring(0, idx)
    val onlyType = onlyId.substring(idx + 1)
    require(versions.contains(onlyVersion)) { "Invalid only version '${onlyId}', version number '${onlyVersion}' extracted from 'ru.vidtu.hcscr.only' not found in ${types.joinToString()}." }
    require(types.contains(onlyType)) { "Invalid only version '${onlyId}', type '${onlyType}' extracted from 'ru.vidtu.hcscr.only' not found in ${versions.joinToString()}." }
}
stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    create(rootProject) {
        for (version in versions) {
            for (type in types) {
                val id = "${version}-${type}"
                if ((onlyId != null) && (id != onlyId) && (id != latestId)) continue
                val subPath = file("versions/${id}")
                if (subPath.resolve(".ignored").isFile) {
                    ignored.add(id)
                    continue
                }
                version(id, version)
            }
        }
        vcsVersion = latestId
    }
}
if (ignored.isNotEmpty()) {
    logger.warn("Ignored versions: ${ignored.joinToString()}")
}
