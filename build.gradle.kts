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
import net.fabricmc.loom.util.ModPlatform
import net.fabricmc.loom.util.ZipUtils
import net.fabricmc.loom.util.ZipUtils.UnsafeUnaryOperator

plugins {
    alias(libs.plugins.architectury.loom)
}

val loomPlatform = loom.platform.get()
val legacyNeoForge = loom.isForge && name.contains(ModPlatform.NEOFORGE.id())
val mcVersion = stonecutter.current.version

val javaMajor = if (stonecutter.eval(mcVersion, ">=1.20.6")) 21
else if (stonecutter.eval(mcVersion, ">=1.18.2")) 17
else if (stonecutter.eval(mcVersion, ">=1.17.1")) 16
else 8
val javaVersion = JavaVersion.toVersion(javaMajor)
java.sourceCompatibility = javaVersion
java.targetCompatibility = javaVersion
java.toolchain.languageVersion = JavaLanguageVersion.of(javaMajor)

val versionSuffix = if (legacyNeoForge) ModPlatform.NEOFORGE.id()!! else loomPlatform.id()!!
group = "ru.vidtu.hcscr"
base.archivesName = "HCsCR"
version = "$version+$mcVersion-$versionSuffix"
description = "Remove your end crystals before the server even knows you hit 'em!"

stonecutter.const("legacyNeoForge", legacyNeoForge)
ModPlatform.values().forEach {
    stonecutter.const(it.id(), it == loomPlatform)
}

loom {
    log4jConfigs.setFrom(rootDir.resolve("log4j2.xml"))
    silentMojangMappingsLicense()
    runs.named("client") {
        vmArgs(
            // Allow JVM without hotswap to work.
            "-XX:+IgnoreUnrecognizedVMOptions",

            // Set up RAM.
            "-Xmx2G",

            // Force UNIX newlines.
            "-Dline.separator=\n",

            // Debug arguments.
            "-ea",
            "-esa",
            "-Dmixin.debug=true",
            "-Dmixin.debug.strict.unique=true",
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
    @Suppress("UnstableApiUsage") // <- Required to have stable refmap name and Mixin processing.
    mixin {
        useLegacyMixinAp = true
        defaultRefmapName = "hcscr.mixins.refmap.json"
    }
    if (loom.isForge) {
        forge {
            mixinConfigs("hcscr.mixins.json")
        }
    } else if (loom.isNeoForge) {
        neoForge {}
    }
}

repositories {
    mavenCentral()
    if (loom.isForge) {
        if (legacyNeoForge) {
            maven("https://maven.neoforged.net/releases/") // Neo. (Legacy)
        }
        maven("https://maven.minecraftforge.net/") // Forge.
    } else if (loom.isNeoForge) {
        maven("https://maven.neoforged.net/releases/") // Neo.
    } else {
        maven("https://maven.fabricmc.net/") // Fabric.
        maven("https://maven.terraformersmc.com/releases/") // ModMenu.
        if (mcVersion == "1.20.4") { // Fix for ModMenu not shading Text Placeholder API.
            maven("https://maven.nucleoid.xyz/") // ModMenu. (Text Placeholder API)
        }
    }
}

dependencies {
    // Annotations
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)

    // Minecraft
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())

    // Loader.
    if (loom.isForge) {
        if (legacyNeoForge) {
            // Legacy NeoForge
            "forge"("net.neoforged:forge:${property("stonecutter.neo")}")
        } else {
            // Forge
            "forge"("net.minecraftforge:forge:${property("stonecutter.forge")}")
        }
    } else if (loom.isNeoForge) {
        // Forge
        "neoForge"("net.neoforged:neoforge:${property("stonecutter.neo")}")
    } else {
        // Fabric
        val fabricApiVersion = property("stonecutter.fabric-api").toString()
        modImplementation(libs.fabric.loader)
        modImplementation(fabricApi.module("fabric-key-binding-api-v1", fabricApiVersion))
        modImplementation(fabricApi.module("fabric-lifecycle-events-v1", fabricApiVersion))
        modImplementation(fabricApi.module("fabric-networking-api-v1", fabricApiVersion))
        modImplementation(fabricApi.module("fabric-rendering-v1", fabricApiVersion))
        modRuntimeOnly(fabricApi.module("fabric-resource-loader-v0", fabricApiVersion)) // Loads assets and also a ModMenu dependency.
        modRuntimeOnly(fabricApi.module("fabric-screen-api-v1", fabricApiVersion)) // ModMenu dependency.
        modImplementation("com.terraformersmc:modmenu:${property("stonecutter.modmenu")}")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    if (javaVersion.isJava9Compatible) {
        options.release = javaMajor
    }
}

tasks.withType<ProcessResources> {
    if (loom.isForge) {
        exclude("fabric.mod.json", "quilt.mod.json", "META-INF/neoforge.mods.toml")
    } else if (loom.isNeoForge) {
        if (stonecutter.eval(mcVersion, ">=1.20.6")) {
            exclude("fabric.mod.json", "quilt.mod.json", "META-INF/mods.toml")
        } else {
            exclude("fabric.mod.json", "quilt.mod.json", "META-INF/neoforge.mods.toml")
        }
    } else {
        exclude("META-INF/mods.toml", "META-INF/neoforge.mods.toml")
    }
    inputs.property("version", version)
    inputs.property("minecraft", mcVersion)
    inputs.property("java", javaMajor)
    inputs.property("platform", loomPlatform.id())
    filesMatching(listOf("fabric.mod.json", "quilt.mod.json", "hcscr.mixins.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
        expand(inputs.properties)
    }
    val files = fileTree(outputs.files.asPath)
    doLast {
        val jsonAlike = Regex("^.*\\.(?:json|mcmeta)$", RegexOption.IGNORE_CASE)
        files.forEach {
            if (it.name.matches(jsonAlike)) {
                it.writeText(Gson().fromJson(it.readText(), JsonElement::class.java).toString())
            } else if (it.name.endsWith(".toml", ignoreCase = true)) {
                it.writeText(it.readLines()
                    .filter { s -> s.isNotBlank() }
                    .joinToString("\n")
                    .replace(" = ", "="))
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
            "Implementation-Vendor" to "VidTu, Offenderify",
            "MixinConfigs" to "hcscr.mixins.json"
        )
    }
}

tasks.withType<RemapJarTask> {
    destinationDirectory = rootProject.layout.buildDirectory.file("libs").get().asFile
    val minifier = UnsafeUnaryOperator<String> { Gson().fromJson(it, JsonElement::class.java).toString() }
    doLast {
        ZipUtils.transformString(archiveFile.get().asFile.toPath(), mapOf(
            "hcscr.mixins.json" to minifier,
            "hcscr.mixins.refmap.json" to minifier,
        ))
    }
}
