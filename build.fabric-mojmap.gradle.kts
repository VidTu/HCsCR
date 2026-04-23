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

// This is the Mojmap Fabric loader buildscript. It is processed by the
// Stonecutter multiple times, for each non-remapped version. (compiled once)
// Based on Loom and processes the preparation/complation/building
// of the most of the mod that is not covered by the Stonecutter or Blossom.
// See "build.fabric-intermediary.gradle.kts" for legacy Intermediary Fabric.
// See "build.forge.gradle.kts" for Forge.
// See "build.neoforge.gradle.kts" for NeoForge.
// See "build.neoforge-hacky.gradle.kts" for NeoForge ugly hack for 1.20.1.
// See "stonecutter.gradle.kts" for the Stonecutter configuration.
// See "settings.gradle.kts" for the Gradle configuration.

import com.google.gson.Gson
import com.google.gson.JsonElement
import net.fabricmc.loom.task.RunGameTask

// Plugins.
plugins {
    id("java")
    alias(libs.plugins.blossom)
    alias(libs.plugins.fabric.loom)
}

// Extract versions.
val mc = sc.current
val mcv = mc.version // Literal version. (toString)
val mcp = mc.parsed // Comparable version. (operator overloading)

// Language.
val javaTarget = 25
val javaVersion = JavaVersion.toVersion(javaTarget)
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
}

loom {
    // Use debug logging config.
    log4jConfigs.setFrom(rootDir.resolve("dev/log4j2.xml"))

    // Set up runs.
    runs {
        // Customize the client run.
        named("client") {
            // Set up debug VM args.
            vmArgs("@../dev/args.vm.txt")

            // Set the run dir.
            runDir = "../../run"
        }

        // Remove server run, the mod is client-only.
        remove(findByName("server"))
    }
}

// Make the game run with the compatible Java. (e.g., Java 17 for 1.20.1)
tasks.withType<RunGameTask> {
    javaLauncher = javaToolchains.launcherFor(java.toolchain)
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") // Fabric.
    maven("https://maven.terraformersmc.com/releases/") // ModMenu.
}

dependencies {
    // Annotations.
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.error.prone.annotations)

    // Minecraft. The dependency may be manually specified for example for snapshots.
    val minecraftDependencyProperty = findProperty("sc.minecraft-dependency")
    require(minecraftDependencyProperty != mcv) { "Unneeded 'sc.minecraft-dependency' property set to ${minecraftDependencyProperty} in ${project}, it already uses this version." }
    val minecraftDependency = minecraftDependencyProperty ?: mcv
    minecraft("com.mojang:minecraft:${minecraftDependency}")

    // Force non-vulnerable Log4J, so that vulnerability scanners don't scream loud.
    // It's also cool for our logging config. (see the "dev/log4j2.xml" file)
    implementation(libs.log4j) {
        exclude("biz.aQute.bnd")
        exclude("com.github.spotbugs")
        exclude("org.osgi")
    }

    // Fabric Loader.
    implementation(libs.fabric.loader)

    // Modular Fabric API.
    val fapi = "${property("sc.fabric-api")}"
    require(fapi.isNotBlank() && fapi != "[SC]") { "Fabric API version is not provided via 'sc.fabric-api' in ${project}." }
    implementation(fabricApi.module("fabric-key-mapping-api-v1", fapi)) // Handles the keybinds. (NOTE: <=1.21.11 script uses "binding", not "mapping")
    implementation(fabricApi.module("fabric-lifecycle-events-v1", fapi)) // Handles game ticks.
    implementation(fabricApi.module("fabric-networking-api-v1", fapi)) // Registers the channel, see README.
    implementation(fabricApi.module("fabric-resource-loader-v1", fapi)) // Loads languages.

    // ModMenu.
    val modmenu = "${property("sc.modmenu")}"
    require(modmenu.isNotBlank() && modmenu != "[SC]") { "ModMenu version is not provided via 'sc.modmenu' in ${project}." }
    // Sometimes, ModMenu is not yet updated for the version. (it almost never updates to snapshots nowadays)
    // So we should depend on it compile-time (it is really an optional dependency for us) to allow both
    // compilation of an optional ModMenu compatibility class (HModMenu.java) and launching the game.
    if ("${findProperty("sc.modmenu.compile-only")}".toBoolean()) {
        compileOnly("com.terraformersmc:modmenu:${modmenu}")
    } else {
        implementation("com.terraformersmc:modmenu:${modmenu}")
        implementation(fabricApi.module("fabric-screen-api-v1", fapi)) // ModMenu dependency.
    }
}

// Compile with UTF-8, compatible Java, and with all debug options.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    options.release = javaTarget
}

sourceSets.main {
    blossom.javaSources {
        // Point to root directory.
        templates(rootDir.resolve("src/main/java-templates"))

        // Expand compile-time variables.
        val fallbackProvider = providers.gradleProperty("ru.vidtu.hcscr.debug")
            .orElse(provider { "${gradle.taskGraph.allTasks.any { it.name == "runClient" }}" })
        property("debugAsserts", providers.gradleProperty("ru.vidtu.hcscr.debug.asserts").orElse(fallbackProvider))
        property("debugLogs", providers.gradleProperty("ru.vidtu.hcscr.debug.logs").orElse(fallbackProvider))
        property("debugProfiler", providers.gradleProperty("ru.vidtu.hcscr.debug.profiler").orElse(fallbackProvider))
        property("version", "${version}")
    }
}

tasks.withType<ProcessResources> {
    // Filter with UTF-8.
    filteringCharset = "UTF-8"

    // Exclude not needed loader entrypoint files.
    exclude("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "pack.mcmeta")

    // Replace the Fabric Resource Loader version.
    // >=26.1.2 has consistent v1, this is used by Intermediary.
    inputs.property("fabricResourceLoaderRevision", "v1")

    // Replace Fabric Keybinding/Keymapping module name.
    // >=26.1.2 has "mapping", previous versions have "binding".
    inputs.property("fabricKeyApiName", "mapping")

    // Replace the Fabric API module name.
    // >=26.1.2 has consistent fabric-api, this is used by Intermediary.
    inputs.property("fabricApiName", "fabric-api")

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
    filesMatching(listOf("fabric.mod.json", "hcscr.mixins.json")) {
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

    // Exclude compile-only code.
    exclude("ru/vidtu/hcscr/platform/HCompile.class")

    // Remove package-info.class, unless package debug is on. (to save space)
    if (!"${findProperty("ru.vidtu.hcscr.debug.package")}".toBoolean()) {
        exclude("**/package-info.class")
    }
}

// Output into "build/libs" instead of "versions/<ver>/build/libs".
tasks.withType<Jar> {
    destinationDirectory = rootProject.layout.buildDirectory.file("libs").get().asFile
}
