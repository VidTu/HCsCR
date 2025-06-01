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

// Extract the platform and Minecraft version.
val loomPlatform = loom.platform.get()
val legacyNeoForge = loom.isForge && name.contains(ModPlatform.NEOFORGE.id())
val mcVersion = stonecutter.current.version

// Determine and set Java toolchain version.
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

stonecutter.allowExtensions("json")

loom {
    // Prepare development environment.
    log4jConfigs.setFrom(rootDir.resolve("dev/log4j2.xml"))
    silentMojangMappingsLicense()

    // Setup JVM args, see that file.
    runs.named("client") {
        vmArgs("@../../../dev/args.vm.txt")
    }

    // Configure Mixin.
    @Suppress("UnstableApiUsage") // <- Required to configure Mixin.
    mixin {
        // Some platforms don't set this and fail preparing the Mixin.
        useLegacyMixinAp = true

        // Set the Mixin refmap name. This is completely optional.
        defaultRefmapName = "hcscr.mixins.refmap.json"
    }

    // Add Mixin configs.
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
            maven("https://maven.neoforged.net/releases/") // NeoForge. (Legacy)
        }
        maven("https://maven.minecraftforge.net/") // Forge.
    } else if (loom.isNeoForge) {
        maven("https://maven.neoforged.net/releases/") // NeoForge.
    } else {
        maven("https://maven.fabricmc.net/") // Fabric.
        maven("https://maven.terraformersmc.com/releases/") // ModMenu.
        if (mcVersion == "1.20.4") { // Fix for ModMenu not shading Text Placeholder API.
            maven("https://maven.nucleoid.xyz/") // ModMenu. (Text Placeholder API)
        }
    }
}

dependencies {
    // Annotations.
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)

    // Minecraft.
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())

    // Force non-vulnerable Log4J, so that vulnerability scanners don't scream loud.
    // It's also cool for our logging config. (dev/log4j2.xml)
    implementation(libs.log4j)

    // Loader.
    if (loom.isForge) {
        if (legacyNeoForge) {
            // Legacy NeoForge.
            "forge"("net.neoforged:forge:${property("stonecutter.neo")}")
        } else {
            // Forge.
            "forge"("net.minecraftforge:forge:${property("stonecutter.forge")}")
        }
    } else if (loom.isNeoForge) {
        // Forge.
        "neoForge"("net.neoforged:neoforge:${property("stonecutter.neo")}")
    } else {
        // Fabric.
        val fabricApiVersion = property("stonecutter.fabric-api").toString()
        modImplementation(libs.fabric.loader)
        modImplementation(fabricApi.module("fabric-key-binding-api-v1", fabricApiVersion)) // Handles the keybinds.
        modImplementation(fabricApi.module("fabric-lifecycle-events-v1", fabricApiVersion)) // Handles game ticks.
        modImplementation(fabricApi.module("fabric-networking-api-v1", fabricApiVersion)) // Registers the channel, see README.
        modImplementation(fabricApi.module("fabric-rendering-v1", fabricApiVersion)) // Handles frame ticks.
        modImplementation(fabricApi.module("fabric-resource-loader-v0", fabricApiVersion)) // Loads languages.
        modImplementation(fabricApi.module("fabric-screen-api-v1", fabricApiVersion)) // ModMenu dependency.
        modImplementation("com.terraformersmc:modmenu:${property("stonecutter.modmenu")}")
    }
}

// Compile with UTF-8, compatble Java, and with all debug options.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    // JDK 8 (used by 1.16.x) doesn't support the "-release" flag
    // (at the top of the file), so we must NOT specify it or the "javac" will fail.
    // JDK 9+ listen to this option.
    if (javaVersion.isJava9Compatible) {
        options.release = javaMajor
    }
}

tasks.withType<ProcessResources> {
    // Exclude not needed files.
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

    // Expand version and dependencies.
    inputs.property("version", version)
    inputs.property("minecraft", mcVersion)
    inputs.property("java", javaMajor)
    inputs.property("platform", loomPlatform.id())
    filesMatching(listOf("fabric.mod.json", "quilt.mod.json", "hcscr.mixins.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
        expand(inputs.properties)
    }

    // Minify JSON (including ".mcmeta") and TOML files.
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

// Reproducible builds.
tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

// Add LICENSE and manifest into the JAR file.
// Manifest also controls Mixin/mod loading on some loaders/versions.
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
    // Output into "build/libs" instead of "versions/<ver>/build/libs".
    destinationDirectory = rootProject.layout.buildDirectory.file("libs").get().asFile

    // Minify JSON files. (after Fabric Loom processing)
    val minifier = UnsafeUnaryOperator<String> { Gson().fromJson(it, JsonElement::class.java).toString() }
    doLast {
        ZipUtils.transformString(archiveFile.get().asFile.toPath(), mapOf(
            "hcscr.mixins.json" to minifier,
            "hcscr.mixins.refmap.json" to minifier,
        ))
    }
}
