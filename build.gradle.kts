/*
 * HCsCR is a third-party mod for Minecraft Java Edition
 * that allows removing the end crystals faster.
 *
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
import net.fabricmc.loom.task.RunGameTask
import net.fabricmc.loom.util.ZipUtils
import net.fabricmc.loom.util.ZipUtils.UnsafeUnaryOperator

plugins {
    alias(libs.plugins.architectury.loom)
}

// Extract the platform and Minecraft version.
val platform = loom.platform.get().id()!!
// NeoForge 1.20.1 is loosely Forge, but not Forge. It uses ModPlatform.FORGE loom platform
// and Forge packages, but diverges from (can't keep up with) the (Lex/Upstream) MCForge 1.20.1.
val hackyNeoForge = (name == "1.20.1-neoforge")
val minecraft = stonecutter.current.version

// Determine and set Java toolchain version.
val javaTarget = if (stonecutter.eval(minecraft, ">=1.20.6")) 21
else if (stonecutter.eval(minecraft, ">=1.18.2")) 17
else if (stonecutter.eval(minecraft, ">=1.17.1")) 16
else 8
val javaVersion = JavaVersion.toVersion(javaTarget)
java.sourceCompatibility = javaVersion
java.targetCompatibility = javaVersion
java.toolchain.languageVersion = JavaLanguageVersion.of(javaTarget)

group = "ru.vidtu.hcscr"
base.archivesName = "HCsCR"
version = "$version+$name"
description = "Remove your end crystals before the server even knows you hit 'em!"

stonecutter {
    // Define Stonecutter preprocessor variables.
    constants["hacky_neoforge"] = hackyNeoForge
    constants {
        match(platform, "fabric", "forge", "neoforge")
    }

    // Process the JSON files via Stonecutter.
    // This is needed for the Mixin configuration.
    filters {
        include("**/*.json")
    }
}

loom {
    // Prepare development environment.
    log4jConfigs.setFrom(rootDir.resolve("dev/log4j2.xml"))
    silentMojangMappingsLicense()

    // Setup JVM args, see that file.
    runs.named("client") {
        // Set up debug VM args.
        if (javaVersion.isJava9Compatible) {
            vmArgs("@../dev/args.vm.txt")
        } else {
            vmArgs(rootDir.resolve("dev/args.vm.txt")
                .readLines()
                .filter { "line.separator" !in it }
                .filter { it.isNotBlank() })
        }

        // Set the run dir.
        runDir = "../../run"

        // AuthLib for 1.16.5 is bugged, disable Mojang API
        // to fix issues with MP testing.
        if (minecraft == "1.16.5") {
            vmArgs(
                "-Dminecraft.api.auth.host=http://0.0.0.0:0/",
                "-Dminecraft.api.account.host=http://0.0.0.0:0/",
                "-Dminecraft.api.session.host=http://0.0.0.0:0/",
                "-Dminecraft.api.services.host=http://0.0.0.0:0/",
            )
        }
    }

    // Configure Mixin.
    @Suppress("UnstableApiUsage") // <- Required to configure Mixin.
    mixin {
        // Some platforms don't set this and fail processing the Mixin.
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

// Make the game run with the required Java path.
tasks.withType<RunGameTask> {
    javaLauncher = javaToolchains.launcherFor(java.toolchain)
}

repositories {
    mavenCentral()
    if (loom.isForge) {
        if (hackyNeoForge) {
            maven("https://maven.neoforged.net/releases/") // NeoForge. (Legacy)
        }
        maven("https://maven.minecraftforge.net/") // Forge.
    } else if (loom.isNeoForge) {
        maven("https://maven.neoforged.net/releases/") // NeoForge.
    } else {
        maven("https://maven.fabricmc.net/") // Fabric.
        maven("https://maven.terraformersmc.com/releases/") // ModMenu.
        if (minecraft == "1.20.4") { // Fix for ModMenu not shading Text Placeholder API.
            maven("https://maven.nucleoid.xyz/") // ModMenu. (Text Placeholder API)
        }
    }
}

dependencies {
    // Annotations.
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.error.prone.annotations)

    // Minecraft.
    val minecraftDependencyProperty = findProperty("stonecutter.minecraft-dependency")
    require(minecraftDependencyProperty != minecraft) { "Unneeded 'stonecutter.minecraft-dependency' property set to $minecraftDependencyProperty in $project, it already uses this version." }
    val minecraftDependency = minecraftDependencyProperty ?: minecraft
    minecraft("com.mojang:minecraft:$minecraftDependency")
    mappings(loom.officialMojangMappings())

    // Force non-vulnerable Log4J, so that vulnerability scanners don't scream loud.
    // It's also cool for our logging config. (see the "dev/log4j2.xml" file)
    implementation(libs.log4j) {
        exclude("biz.aQute.bnd")
        exclude("com.github.spotbugs")
        exclude("org.osgi")
    }

    // Loader.
    if (loom.isForge) {
        if (hackyNeoForge) {
            // Legacy NeoForge.
            "forge"("net.neoforged:forge:${property("stonecutter.neoforge")}")
        } else {
            // Forge.
            "forge"("net.minecraftforge:forge:${property("stonecutter.forge")}")
        }
    } else if (loom.isNeoForge) {
        // Forge.
        "neoForge"("net.neoforged:neoforge:${property("stonecutter.neoforge")}")
    } else {
        // Fabric.
        val fabricApiVersion = property("stonecutter.fabric-api").toString()
        modImplementation(libs.fabric.loader)
        modImplementation(fabricApi.module("fabric-key-binding-api-v1", fabricApiVersion)) // Handles the keybinds.
        modImplementation(fabricApi.module("fabric-lifecycle-events-v1", fabricApiVersion)) // Handles game ticks.
        modImplementation(fabricApi.module("fabric-networking-api-v1", fabricApiVersion)) // Registers the channel, see README.
        modImplementation(fabricApi.module("fabric-resource-loader-v0", fabricApiVersion)) // Loads languages.
        modImplementation(fabricApi.module("fabric-screen-api-v1", fabricApiVersion)) // ModMenu dependency.
        modImplementation("com.terraformersmc:modmenu:${property("stonecutter.modmenu")}")
    }
}

// Compile with UTF-8, compatible Java, and with all debug options.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    // JDK 8 (used by 1.16.x) doesn't support the "-release" flag and
    // uses "-source" and "-target" ones (see the top of the file),
    // so we must NOT specify it or the "javac" will fail.
    // JDK 9+ does listen to this option.
    if (javaVersion.isJava9Compatible) {
        options.release = javaTarget
    }
}

tasks.withType<ProcessResources> {
    // Filter with UTF-8.
    filteringCharset = "UTF-8"

    // Exclude not needed loader entrypoint files.
    if (loom.isForge) {
        exclude("fabric.mod.json", "quilt.mod.json", "META-INF/neoforge.mods.toml")
    } else if (loom.isNeoForge) {
        if (stonecutter.eval(minecraft, ">=1.20.6")) {
            exclude("fabric.mod.json", "quilt.mod.json", "META-INF/mods.toml")
        } else {
            exclude("fabric.mod.json", "quilt.mod.json", "META-INF/neoforge.mods.toml")
        }
    } else {
        exclude("META-INF/mods.toml", "META-INF/neoforge.mods.toml")
    }

    // Expand version and dependencies.
    val minecraftRequirementProperty = findProperty("stonecutter.minecraft-requirement")
    require(minecraftRequirementProperty != minecraft) { "Unneeded 'stonecutter.minecraft-requirement' property set to $minecraftRequirementProperty in $project, it already uses this version." }
    val minecraftRequirement = minecraftRequirementProperty ?: minecraft
    inputs.property("version", version)
    inputs.property("minecraft", minecraftRequirement)
    inputs.property("java", javaTarget)
    inputs.property("platform", platform)
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
                    .filter { s -> !s.startsWith('#') }
                    .filter { s -> s.isNotBlank() }
                    .joinToString("\n")
                    .replace(" = ", "="))
            }
        }
    }
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
