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

// This is the main (multi-version loader) buildscript. It is processed by the
// Stonecutter multiple times, for each version and each loader. (compiled once)
// Based on Architectury Loom and processes the preparation/complation/building
// of the most of the mod that is not covered by the Stonecutter or Blossom.
// See "compile" for the compile-time constants and Blossom configuration.
// See "stonecutter.gradle.kts" for the Stonecutter configuration.
// See "settings.gradle.kts" for the Gradle configuration.

import com.google.gson.Gson
import com.google.gson.JsonElement
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RunGameTask

plugins {
    alias(libs.plugins.architectury.loom)
}

// Extract the platform and Minecraft version.
val platform = loom.platform.get().id()!!
// NeoForge 1.20.1 is loosely Forge, but not Forge. It uses ModPlatform.FORGE loom platform
// and Forge packages, but diverges from (can't keep up with) the (Lex/Upstream) MCForge 1.20.1.
val hackyNeoForge = (name == "1.20.1-neoforge")

// Extract versions.
val mc = sc.current
val mcv = mc.version // Literal version. (toString)
val mcp = mc.parsed // Comparable version. (operator overloading)

// Language.
val javaTarget = if (mcp >= "26.1") 25
else if (mcp >= "1.20.6") 21
else if (mcp >= "1.18.2") 17
else if (mcp >= "1.17.1") 16
else 8
val javaVersion = JavaVersion.toVersion(javaTarget)!!
java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    toolchain.languageVersion = JavaLanguageVersion.of(javaTarget)
}

// Metadata.
group = "ru.vidtu.hcscr"
base.archivesName = "HCsCR"
version = "${version}+${name}"
description = "Remove your end crystals before the server even knows you hit 'em!"

// Define Stonecutter preprocessor variables/constants.
sc {
    constants["hacky_neoforge"] = hackyNeoForge
    constants {
        match(platform, "fabric", "forge", "neoforge")
    }
}

loom {
    // Prepare development environment.
    log4jConfigs.setFrom(rootDir.resolve("dev/log4j2.xml"))
    silentMojangMappingsLicense()

    // Set up runs.
    runs {
        // Customize the client run.
        named("client") {
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
            if (mcp eq "1.16.5") {
                vmArgs(
                    "-Dminecraft.api.auth.host=http://0.0.0.0:0/",
                    "-Dminecraft.api.account.host=http://0.0.0.0:0/",
                    "-Dminecraft.api.session.host=http://0.0.0.0:0/",
                    "-Dminecraft.api.services.host=http://0.0.0.0:0/",
                )
            }
        }

        // Remove server run, the mod is client-only.
        remove(findByName("server"))
    }

    // Configure Mixin.
    @Suppress("UnstableApiUsage") // <- Required to configure Mixin.
    mixin {
        // Use direct remapping instead of annotation processor and refmaps.
        useLegacyMixinAp = false
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
        if (mcp eq "1.20.4") { // Fix for ModMenu not shading Text Placeholder API.
            maven("https://maven.nucleoid.xyz/") // ModMenu. (Text Placeholder API)
        }
    }
}

dependencies {
    // Annotations.
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.error.prone.annotations)

    // Compile-time constants. (Blossom)
    compileOnly(project(":compile"))

    // Minecraft. The dependency may be manually specified for example for snapshots.
    val minecraftDependencyProperty = findProperty("sc.minecraft-dependency")
    require(minecraftDependencyProperty != mcv) { "Unneeded 'sc.minecraft-dependency' property set to ${minecraftDependencyProperty} in ${project}, it already uses this version." }
    val minecraftDependency = minecraftDependencyProperty ?: mcv
    minecraft("com.mojang:minecraft:${minecraftDependency}")

    // Mappings.
    if (mcp < "26.1") {
        mappings(loom.officialMojangMappings())
    }

    // MixinExtras.
    if (loom.isForge) {
        compileOnly(libs.mixinextras)
        annotationProcessor(libs.mixinextras)
    }

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
            val neoforge = "${property("sc.neoforge")}"
            require(neoforge.isNotBlank() && neoforge != "[SC]") { "NeoForge (legacy) version is not provided via 'sc.neoforge' in ${project}." }
            val extractedMinecraft = neoforge.substringBefore('-')
            require(mcp eq extractedMinecraft) { "NeoForge (legacy) version '${neoforge}' provides Minecraft ${extractedMinecraft} in ${project}, but we want ${mcv}." }
            "forge"("net.neoforged:forge:${neoforge}")
        } else {
            // Forge.
            val forge = "${property("sc.forge")}"
            require(forge.isNotBlank() && forge != "[SC]") { "Forge version is not provided via 'sc.forge' in ${project}." }
            val extractedMinecraft = forge.substringBefore('-')
            require(mcp eq extractedMinecraft) { "Forge version '${forge}' provides Minecraft ${extractedMinecraft} in ${project}, but we want ${mcv}." }
            "forge"("net.minecraftforge:forge:${forge}")
        }
    } else if (loom.isNeoForge) {
        // NeoForge.
        val neoforge = "${property("sc.neoforge")}"
        require(neoforge.isNotBlank() && neoforge != "[SC]") { "NeoForge version is not provided via 'sc.neoforge' in ${project}." }
        val extractedMinecraft = "1.${neoforge.substringBeforeLast('.')}"
        require(mcp eq extractedMinecraft) { "NeoForge version '${neoforge}' provides Minecraft ${extractedMinecraft} in ${project}, but we want ${mcv}." }
        "neoForge"("net.neoforged:neoforge:${neoforge}")
    } else {
        // Fabric Loader.
        modImplementation(libs.fabric.loader)

        // Modular Fabric API.
        val fapi = "${property("sc.fabric-api")}"
        require(fapi.isNotBlank() && fapi != "[SC]") { "Fabric API version is not provided via 'sc.fabric-api' in ${project}." }
        val resourceLoader = if (mcp >= "1.21.10") "v1" else "v0"
        modImplementation(fabricApi.module("fabric-key-binding-api-v1", fapi)) // Handles the keybinds.
        modImplementation(fabricApi.module("fabric-lifecycle-events-v1", fapi)) // Handles game ticks.
        modImplementation(fabricApi.module("fabric-networking-api-v1", fapi)) // Registers the channel, see README.
        modImplementation(fabricApi.module("fabric-resource-loader-${resourceLoader}", fapi)) // Loads languages.

        // ModMenu.
        val modmenu = "${property("sc.modmenu")}"
        require(modmenu.isNotBlank() && modmenu != "[SC]") { "ModMenu version is not provided via 'sc.modmenu' in ${project}." }
        // Sometimes, ModMenu is not yet updated for the version. (it almost never updates to snapshots nowadays)
        // So we should depend on it compile-time (it is really an optional dependency for us) to allow both
        // compilation of an optional ModMenu compatibility class (HModMenu.java) and launching the game.
        if ("${findProperty("sc.modmenu.compile-only")}".toBoolean()) {
            modCompileOnly("com.terraformersmc:modmenu:${modmenu}")
        } else {
            modImplementation("com.terraformersmc:modmenu:${modmenu}")
            modImplementation(fabricApi.module("fabric-resource-loader-v0", fapi)) // ModMenu dependency.
            modImplementation(fabricApi.module("fabric-screen-api-v1", fapi)) // ModMenu dependency.
        }
    }
}

// Compile with UTF-8, compatible Java, and with all debug options.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    // JDK 8 (used by 1.16.x) doesn't support the "-release" flag and
    // uses "-source" and "-target" ones (see the top of the file),
    // so we must NOT specify it, or the "javac" will fail.
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
        if (mcp >= "1.20.6") {
            exclude("fabric.mod.json", "quilt.mod.json", "META-INF/mods.toml")
        } else {
            exclude("fabric.mod.json", "quilt.mod.json", "META-INF/neoforge.mods.toml")
        }
    } else {
        exclude("META-INF/mods.toml", "META-INF/neoforge.mods.toml")

        // Determine and replace the Fabric Resource Loader version.
        val fabricResourceLoader = if (mcp >= "1.21.10") "v1" else "v0"
        inputs.property("fabricResourceLoader", fabricResourceLoader)
    }

    // Determine and replace the platform version range requirement.
    val platformRequirement = "${project.property("sc.platform-requirement")}"
    if (loom.isForge || loom.isNeoForge) {
        require(platformRequirement.isNotBlank() && platformRequirement != "[SC]") { "Platform requirement is not provided via 'sc.platform-requirement' in ${project}." }
        inputs.property("platformRequirement", platformRequirement)
    } else {
        require(platformRequirement == "[SC]") { "Platform requirement is provided via 'sc.platform-requirement' in ${project}, but Fabric builds ignore it." }
    }

    // Expand the updater URL for Forge-like loaders.
    if (loom.isNeoForge || hackyNeoForge) {
        inputs.property("forgeUpdaterUrl", "https://raw.githubusercontent.com/VidTu/HCsCR/main/updater_hcscr_neoforge.json")
    } else if (loom.isForge) {
        inputs.property("forgeUpdaterUrl", "https://raw.githubusercontent.com/VidTu/HCsCR/main/updater_hcscr_forge.json")
    }

    // Expand Minecraft requirement that can be manually overridden for reasons. (e.g., snapshots)
    val minecraftRequirementProperty = findProperty("sc.minecraft-requirement")
    require(minecraftRequirementProperty != mcv) { "Unneeded 'sc.minecraft-requirement' property set to ${minecraftRequirementProperty} in ${project}, it already uses this version." }
    val minecraftRequirement = minecraftRequirementProperty ?: mcv
    inputs.property("minecraft", minecraftRequirement)

    // Expand Mixin Java version. It's pretty much the same Java, except the Forge 1.20.6 edge case,
    // where it must not be JAVA_21 (even tho we're using Java 21), but instead a Java 18 due to Mixin 0.8.5:
    // https://github.com/SpongePowered/Mixin/blob/releases/0.8.5/src/main/java/org/spongepowered/asm/mixin/MixinEnvironment.java#L747
    val mixinJava = if (loom.isForge && mcp eq "1.20.6") 18 else javaTarget
    inputs.property("mixinJava", mixinJava)

    // Expand version and dependencies.
    inputs.property("version", version)
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
            "Implementation-Vendor" to "VidTu, Offenderify, libffi",
            "MixinConfigs" to "hcscr.mixins.json"
        )
    }
}

// Output into "build/libs" instead of "versions/<ver>/build/libs".
tasks.withType<RemapJarTask> {
    destinationDirectory = rootProject.layout.buildDirectory.file("libs").get().asFile
}
