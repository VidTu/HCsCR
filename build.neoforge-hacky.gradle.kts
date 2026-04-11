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

// This is the main (multi-version loader) buildscript. It is processed by the
// Stonecutter multiple times, for each version and each loader. (compiled once)
// Based on Architectury Loom and processes the preparation/complation/building
// of the most of the mod that is not covered by the Stonecutter or Blossom.
// See "build.fabric-intermediary.gradle.kts" for legacy Intermediary Fabric.
// See "build.fabric-mojmap.gradle.kts" for modern Mojmap Fabric.
// See "build.forge.gradle.kts" for Forge.
// See "build.neoforge.gradle.kts" for NeoForge.
// See "compile" for the compile-time constants and Blossom configuration.
// See "stonecutter.gradle.kts" for the Stonecutter configuration.
// See "settings.gradle.kts" for the Gradle configuration.

// NeoForge 1.20.1 is a piece of hacky mess that's basically Forge 1.20.1 with
// a "95% OFF" discount. It is loosely Forge, but not Forge. It uses Forge
// packages, but diverges from (can't keep up with) the (Lex) MCForge 1.20.1.
// I don't know why support this edge case for approximately 6 or 7 users total.

import com.google.gson.Gson
import com.google.gson.JsonElement

plugins {
    alias(libs.plugins.moddevgradle.legacy)
}

// Language.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

// Metadata.
group = "ru.vidtu.hcscr"
base.archivesName = "HCsCR"
version = "${version}+${name}"
description = "Remove your end crystals before the server even knows you hit 'em!"

// Define Stonecutter preprocessor variables/constants.
sc {
    constants["fabric"] = false
    constants["forge"] = true // Yes, that's correct for NeoForge 1.20.1.
    constants["hacky_neoforge"] = true // And that's extremely correct.
    constants["neoforge"] = false // Yes, that's also correct.
}

legacyForge {
    // Minecraft and Forge.
    val neoforge = "${property("sc.neoforge")}"
    require(neoforge.isNotBlank() && neoforge != "[SC]") { "NeoForge (Hacky) version is not provided via 'sc.neoforge' in ${project}." }
    val extractedMinecraft = neoforge.substringBefore('-')
    require(extractedMinecraft == "1.20.1") { "NeoForge (Hacky) version '${neoforge}' provides Minecraft ${extractedMinecraft} in ${project}, but we want 1.20.1." }
    enable {
        // Set the version.
        neoForgeVersion = neoforge

        // Enable recompilation for CI.
        // NOTE: Binpatching produces uncompilable artifacts for
        // NeoForge 1.20.1, at least for HCsCR. Specifically, it
        // has issues with anonymous classes in HStonecutter.java.
        setDisableRecompilation(false)
    }

    // Set up runs.
    runs {
        // Customize the client run.
        register("client") {
            // Make client.
            client()

            // Set up debug VM args.
            //jvmArguments("@../dev/args.vm.txt")

            // Set the run dir.
            //workingDirectory = file("../../run")
        }
    }
}

mixin {
    add(sourceSets["main"], "hcscr.mixins.refmap.json")
    config("hcscr.mixins.json")
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases/") // NeoForge.
    maven("https://maven.minecraftforge.net/") // Forge.
}

dependencies {
    // Annotations.
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.error.prone.annotations)

    // Compile-time constants. (Blossom)
    compileOnly(project(":compile"))

    // Mixin.
    annotationProcessor("${libs.mixin.get()}:processor")

    // MixinExtras.
    compileOnly(libs.mixinextras)
    annotationProcessor(libs.mixinextras)

    // Force non-vulnerable Log4J, so that vulnerability scanners don't scream loud.
    // It's also cool for our logging config. (see the "dev/log4j2.xml" file)
    implementation(libs.log4j) {
        exclude("biz.aQute.bnd")
        exclude("com.github.spotbugs")
        exclude("org.osgi")
    }
}

// Compile with UTF-8, compatible Java, and with all debug options.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    options.release = 17
}

tasks.withType<ProcessResources> {
    // Filter with UTF-8.
    filteringCharset = "UTF-8"

    // Exclude not needed loader entrypoint files.
    exclude("fabric.mod.json", "quilt.mod.json", "META-INF/neoforge.mods.toml")

    // Determine and replace the platform version range requirement.
    val platformRequirement = "${project.property("sc.platform-requirement")}"
    require(platformRequirement.isNotBlank() && platformRequirement != "[SC]") { "Platform requirement is not provided via 'sc.platform-requirement' in ${project}." }
    inputs.property("platformRequirement", platformRequirement)

    // Expand the updater URL.
    inputs.property("forgeUpdaterUrl", "https://raw.githubusercontent.com/VidTu/HCsCR/main/updater_hcscr_neoforge.json")

    // Expand Minecraft requirement that can be manually overridden for reasons. (e.g., snapshots)
    val minecraftRequirementProperty = findProperty("sc.minecraft-requirement")
    require(minecraftRequirementProperty != "1.20.1") { "Unneeded 'sc.minecraft-requirement' property set to ${minecraftRequirementProperty} in ${project}, it already uses this version." }
    val minecraftRequirement = minecraftRequirementProperty ?: "1.20.1"
    inputs.property("minecraft", minecraftRequirement)

    // Expand Mixin Java version.
    inputs.property("mixinJava", 17)

    // Expand version and dependencies.
    inputs.property("version", version)
    inputs.property("platform", "forge")
    filesMatching(listOf("hcscr.mixins.json", "META-INF/mods.toml")) {
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

tasks.withType<Jar> {
    // Add LICENSE and NOTICE.
    from(rootDir.resolve("LICENSE"))
    from(rootDir.resolve("NOTICE"))

    // Remove package-info.class, unless package debug is on. (to save space)
    if (!"${findProperty("ru.vidtu.hcscr.debug.package")}".toBoolean()) {
        exclude("**/package-info.class")
    }

    // Add manifest.
    manifest {
        attributes("MixinConfigs" to "hcscr.mixins.json")
    }
}

// Output into "build/libs" instead of "versions/<ver>/build/libs".
tasks.withType<Jar> {
    destinationDirectory = rootProject.layout.buildDirectory.file("libs").get().asFile
}
