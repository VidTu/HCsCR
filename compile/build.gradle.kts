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

// This is the buildscript for the compile-time constants used by the project.
// See "build.gradle.kts" in the root for the per-version Gradle buildscript.
// See "stonecutter.gradle.kts" for the Stonecutter configuration.
// See "settings.gradle.kts" for the Gradle configuration.

plugins {
    id("java")
    alias(libs.plugins.blossom)
}

// Language.
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

// Metadata.
group = "ru.vidtu.hcscr.compile"
base.archivesName = "HCsCR"
version = "${version}+compile"
description = "Remove your end crystals before the server even knows you hit 'em! (compile-only constants)"

repositories {
    mavenCentral()
}

dependencies {
    // Annotations.
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.error.prone.annotations)
}

// Compile with UTF-8, compatible Java, and with all debug options.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    // JDK 8 (used by this buildscript) doesn't support the "-release" flag
    // (at the top of the file), so we must NOT specify it or the "javac" will fail.
    // If we ever gonna compile on newer Java versions, uncomment this line.
    // options.release = 8
}

// Expand the debug.
sourceSets.main {
    blossom.javaSources {
        property("debug", provider {
            "${gradle.taskGraph.allTasks.any { it.name == "runClient" }}"
        })
    }
}

// Filter with UTF-8.
tasks.withType<ProcessResources> {
    filteringCharset = "UTF-8"
}

// Add LICENSE and manifest into the JAR file.
tasks.withType<Jar> {
    from(rootDir.resolve("LICENSE"))
    from(rootDir.resolve("NOTICE"))
    manifest {
        attributes(
            "Specification-Title" to "HCsCR",
            "Specification-Version" to version,
            "Specification-Vendor" to "VidTu",
            "Implementation-Title" to "HCsCR-compile",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "VidTu, Offenderify, libffi"
        )
    }
}
