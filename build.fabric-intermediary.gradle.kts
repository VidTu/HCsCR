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

// This is the Intermediary Fabric loader buildscript. It is processed by the
// Stonecutter multiple times, for each remapped version. (compiled once)
// Based on Loom and processes the preparation/complation/building
// of the most of the mod that is not covered by the Stonecutter or Blossom.
// See "build.fabric-mojmap.gradle.kts" for modern Mojmap Fabric.
// See "build.forge.gradle.kts" for Forge.
// See "build.neoforge.gradle.kts" for NeoForge.
// See "build.neoforge-hacky.gradle.kts" for NeoForge ugly hack for 1.20.1.
// See "compile" for the compile-time constants and Blossom configuration.
// See "stonecutter.gradle.kts" for the Stonecutter configuration.
// See "settings.gradle.kts" for the Gradle configuration.

import com.google.gson.Gson
import com.google.gson.JsonElement
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RunGameTask

// Configure plugins.
plugins {
    alias(libs.plugins.fabric.loom.remap)
}

// Extract versions.
val mc = sc.current
val mcv = mc.version // Literal version. (toString)
val mcp = mc.parsed // Comparable version. (operator overloading)

// Language.
val javaTarget = if (mcp >= "1.20.6") 21
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
    constants["fabric"] = true
    constants["forge"] = false
    constants["hacky_neoforge"] = false
    constants["neoforge"] = false
    swaps["minecraft_version"] = "\"${mcv}\""
}

loom {
    // Use debug logging config.
    log4jConfigs.setFrom(rootDir.resolve("dev/log4j2.xml"))

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
            // to fix issues with multiplayer testing.
            if (mcp eq "1.16.5") {
                vmArgs(
                    "-Dminecraft.api.account.host=http://0.0.0.0:0/",
                    "-Dminecraft.api.auth.host=http://0.0.0.0:0/",
                    "-Dminecraft.api.services.host=http://0.0.0.0:0/",
                    "-Dminecraft.api.session.host=http://0.0.0.0:0/"
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
}

// Make the game run with the compatible Java. (e.g,. Java 17 for 1.20.1)
tasks.withType<RunGameTask> {
    javaLauncher = javaToolchains.launcherFor(java.toolchain)
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") // Fabric.
    maven("https://maven.terraformersmc.com/releases/") // ModMenu.
    if (mcp eq "1.20.4") { // Fix for ModMenu not providing Text Placeholder API.
        maven("https://maven.nucleoid.xyz/") // ModMenu. (Text Placeholder API)
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
    mappings(loom.officialMojangMappings())

    // Force non-vulnerable Log4J, so that vulnerability scanners don't scream loud.
    // It's also cool for our logging config. (see the "dev/log4j2.xml" file)
    implementation(libs.log4j) {
        exclude("biz.aQute.bnd")
        exclude("com.github.spotbugs")
        exclude("org.osgi")
    }

    // Fabric Loader.
    modImplementation(libs.fabric.loader)

    // Modular Fabric API.
    val fapi = "${property("sc.fabric-api")}"
    require(fapi.isNotBlank() && fapi != "[SC]") { "Fabric API version is not provided via 'sc.fabric-api' in ${project}." }
    val fabricResourceLoaderRevision = if (mcp >= "1.21.10") "v1" else "v0"
    modImplementation(fabricApi.module("fabric-key-binding-api-v1", fapi)) // Handles the keybinds. (NOTE: >=26.1.2 script uses "mapping", not "binding")
    modImplementation(fabricApi.module("fabric-lifecycle-events-v1", fapi)) // Handles game ticks.
    modImplementation(fabricApi.module("fabric-networking-api-v1", fapi)) // Registers the channel, see README.
    modImplementation(fabricApi.module("fabric-resource-loader-${fabricResourceLoaderRevision}", fapi)) // Loads languages.

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
        if (mcp eq "1.21.10") {
            modImplementation(fabricApi.module("fabric-resource-loader-v0", fapi)) // ModMenu dependency.
        }
        modImplementation(fabricApi.module("fabric-screen-api-v1", fapi)) // ModMenu dependency.
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
    exclude("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "pack.mcmeta")

    // Determine and replace the Fabric Resource Loader version.
    val fabricResourceLoaderRevision = if (mcp >= "1.21.10") "v1" else "v0"
    inputs.property("fabricResourceLoaderRevision", fabricResourceLoaderRevision)

    // Replace Fabric Keybinding/Keymapping module name.
    // >=26.1.2 has "mapping", previous versions have "binding".
    inputs.property("fabricKeyApiName", "binding")

    // Determine and replace the Fabric API module name.
    val fabricApiName = if (mcp >= "1.18.2") "fabric-api" else "fabric"
    inputs.property("fabricApiName", fabricApiName)

    // Determine and replace the platform version range requirement.
    val platformRequirement = "${project.property("sc.platform-requirement")}"
    require(platformRequirement == "[SC]") { "Platform requirement is provided via 'sc.platform-requirement' in ${project}, but Fabric builds ignore it." }

    // Expand Minecraft requirement that can be manually overridden for reasons. (e.g., snapshots)
    val minecraftRequirementProperty = findProperty("sc.minecraft-requirement")
    require(minecraftRequirementProperty != mcv) { "Unneeded 'sc.minecraft-requirement' property set to ${minecraftRequirementProperty} in ${project}, it already uses this version." }
    val minecraftRequirement = minecraftRequirementProperty ?: mcv
    inputs.property("minecraft", minecraftRequirement)

    // Expand Mixin Java version.
    inputs.property("mixinJava", javaTarget)

    // Expand version and dependencies.
    inputs.property("version", version)
    filesMatching(listOf("fabric.mod.json", "quilt.mod.json", "hcscr.mixins.json")) {
        expand(inputs.properties)
    }

    // Minify JSON files.
    val files = fileTree(outputs.files.asPath)
    doLast {
        files.forEach {
            if (it.name.endsWith(".json", ignoreCase = true)) {
                it.writeText(Gson().fromJson(it.readText(), JsonElement::class.java).toString())
            }
        }
    }
}

tasks.withType<Jar> {
    // Add LICENSE and NOTICE.
    from(rootDir.resolve("LICENSE"))
    from(rootDir.resolve("NOTICE"))

    // Remove package-info.class, unless package debug is on. (to save space)
    if (!"${findProperty("ru.vidtu.hcscr.debug.package")}".toBoolean()) {
        exclude("**/package-info.class")
    }
}

// Output into "build/libs" instead of "versions/<ver>/build/libs".
tasks.withType<RemapJarTask> {
    destinationDirectory = rootProject.layout.buildDirectory.file("libs").get().asFile
}
