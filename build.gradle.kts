/*
 * Copyright (c) 2023 Offenderify
 * Copyright (c) 2023-2025 VidTu
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

import com.google.gson.Gson
import com.google.gson.JsonElement
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.util.ZipUtils
import net.fabricmc.loom.util.ZipUtils.UnsafeUnaryOperator

plugins {
    alias(libs.plugins.architectury.loom)
}

val mcVersion = stonecutter.current.version
val javaMajor = if (stonecutter.eval(mcVersion, ">=1.20.6")) 21
else if (stonecutter.eval(mcVersion, ">=1.18.2")) 17
else if (stonecutter.eval(mcVersion, ">=1.17.1")) 16
else 8
val javaVersion = JavaVersion.toVersion(javaMajor)
java.sourceCompatibility = javaVersion
java.targetCompatibility = javaVersion
java.toolchain.languageVersion = JavaLanguageVersion.of(javaMajor)

group = "ru.vidtu.hcscr"
base.archivesName = "HCsCR"
version = "$version+$mcVersion-fabric"
description = "Remove your end crystals before the server even knows you hit 'em!"

loom {
    log4jConfigs.setFrom(rootDir.resolve("log4j2.xml"))
    silentMojangMappingsLicense()
    runs.named("client") {
        vmArgs(
            // Allow JVM without hotswap to work.
            "-XX:+IgnoreUnrecognizedVMOptions",

            // Set up RAM.
            "-Xmx2G",

            // Debug arguments.
            "-ea",
            "-esa",
            "-Dmixin.debug=true",
            "-Dmixin.debug.strict=false", // TODO
            "-Dmixin.checks=true",
            "-Dio.netty.tryReflectionSetAccessible=true",
            "-Dio.netty.leakDetection.level=PARANOID",

            // Allow hot swapping on supported JVM.
            "-XX:+AllowEnhancedClassRedefinition",
            "-XX:+AllowRedefinitionToAddDeleteMethods",
            "-XX:HotswapAgent=fatjar",
            "-Dfabric.debug.disableClassPathIsolation=true",

            // Open modules for Netty.
            "--add-opens",
            "java.base/java.nio=ALL-UNNAMED",
            "--add-opens",
            "java.base/jdk.internal.misc=ALL-UNNAMED"
        )
    }
    @Suppress("UnstableApiUsage") // <- I want the fancy refmap name. It's completely optional and can be removed anytime.
    mixin {
        defaultRefmapName = "hcscr.mixins.refmap.json"
    }
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") // Fabric.
    maven("https://maven.terraformersmc.com/releases/") // ModMenu.
    if (mcVersion == "1.20.4") { // Fix for ModMenu not shading Text Placeholder API.
        maven("https://maven.nucleoid.xyz/") // ModMenu. (Text Placeholder API)
    }
}

dependencies {
    // Annotations
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)

    // Minecraft
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())

    // Fabric
    modImplementation(libs.fabric.loader)
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("stonecutter.fabric-api")}")
    modImplementation("com.terraformersmc:modmenu:${property("stonecutter.modmenu")}")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    if (javaVersion.isJava9Compatible) {
        options.release = javaMajor
    }
}

tasks.withType<ProcessResources> {
    inputs.property("version", version)
    inputs.property("minecraft", mcVersion)
    inputs.property("java", javaMajor)
    filesMatching(listOf("fabric.mod.json", "quilt.mod.json", "hcscr.mixins.json")) {
        expand(inputs.properties)
    }
    fileTree(outputs.files.asPath).forEach {
        if (it.name.endsWith(".json")) {
            doLast {
                it.writeText(Gson().fromJson(it.readText(), JsonElement::class.java).toString())
            }
        }
    }
}

tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.withType<Jar> {
    from(rootDir.resolve("LICENSE"))
    from(rootDir.resolve("NOTICE"))
    manifest {
        attributes(
            "Specification-Title" to "HCsCR",
            "Specification-Version" to version,
            "Specification-Vendor" to "VidTu",
            "Implementation-Title" to "HCsCR",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "VidTu, Offenderify"
        )
    }
}

tasks.withType<RemapJarTask> {
    val minifier = UnsafeUnaryOperator<String> { Gson().fromJson(it, JsonElement::class.java).toString() }
    doLast {
        ZipUtils.transformString(archiveFile.get().asFile.toPath(), mapOf(
            "hcscr.mixins.json" to minifier,
            "hcscr.mixins.refmap.json" to minifier,
        ))
    }
}
