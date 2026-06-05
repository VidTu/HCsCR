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

// This is the Forge loader buildscript. It is processed by the
// Stonecutter multiple times, for each version. (compiled once)
// Based on ForgeGradle and processes the preparation/complation/building
// of the most of the mod that is not covered by the Stonecutter or Blossom.
// See "build.fabric-intermediary.gradle.kts" for legacy Intermediary Fabric.
// See "build.fabric-mojmap.gradle.kts" for modern Mojmap Fabric.
// See "build.neoforge.gradle.kts" for NeoForge.
// See "build.neoforge-hacky.gradle.kts" for NeoForge ugly hack for 1.20.1.
// See "stonecutter.gradle.kts" for the Stonecutter configuration.
// See "settings.gradle.kts" for the Gradle configuration.

import com.google.gson.Gson
import com.google.gson.JsonElement
import net.minecraftforge.renamer.gradle.RenameJar
import ru.vidtu.hcscr.buildsrc.Strip
import java.io.FileInputStream
import java.util.Properties

// Plugins.
plugins {
    id("java")
    alias(libs.plugins.blossom)
    alias(libs.plugins.forgegradle)
    alias(libs.plugins.forgerenamer)
}

// Extract versions.
val mc = sc.current
val mcv = mc.version // Literal version. (toString)
val mcp = mc.parsed // Comparable version. (operator overloading)

// Language.
val javaTarget = when {
    (mcp >= "26.1.2") -> 25
    (mcp >= "1.20.6") -> 21
    (mcp >= "1.18.2") -> 17
    (mcp >= "1.17.1") -> 16
    else -> 8
}
val javaVersion = JavaVersion.toVersion(javaTarget)
java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    val javaToolchain = if (javaTarget == 16) 17 else javaTarget
    toolchain.languageVersion = JavaLanguageVersion.of(javaToolchain)
}

// Metadata.
group = "ru.vidtu.hcscr"
base.archivesName = "HCsCR"
version = "${version}+${name}"
description = "Remove your end crystals before the server even knows you hit 'em!"

// Add GSON to buildscript classpath, we use it for minifying JSON files.
buildscript {
    dependencies {
        classpath(libs.gson)
    }
}

sc {
    // Define Stonecutter preprocessor variables/constants.
    constants["fabric"] = false
    constants["forge"] = true
    constants["hacky_neoforge"] = false
    constants["neoforge"] = false
    properties.tags(mcv, "forge")

    // Define MCP replacements.
    replacements.string(mcp <= "1.16.5") {
        val remaps = Properties()
        FileInputStream(rootDir.resolve("dev/mcp.properties")).use { remaps.load(it) }
        remaps.forEach { mojmap, mcp ->
            replace("import ${"${mojmap}".replace('$', '.')};", "import ${"${mcp}".replace('$', '.')};")
            replace("L${"${mojmap}".replace('.', '/')};", "L${"${mcp}".replace('.', '/')};")
            val mojmapLast = "${mojmap}".substringAfterLast('.').replace('$', '.');
            val mcpLast = "${mcp}".substringAfterLast('.').replace('$', '.');
            if (mojmapLast == mcpLast) return@forEach
            replace("${mojmapLast}.", "${mcpLast}.")
            replace("<${mojmapLast}>", "<${mcpLast}>")
            replace("(${mojmapLast}) ", "(${mcpLast}) ")
            replace("extends ${mojmapLast}", "extends ${mcpLast}")
            replace("final ${mojmapLast}", "final ${mcpLast}")
            replace("final @UnknownNullability ${mojmapLast}", "final @UnknownNullability ${mcpLast}")
            replace(" instanceof ${mojmapLast}", " instanceof ${mcpLast}")
            replace("@Mixin(${mojmapLast}.class)", "@Mixin(${mcpLast}.class)")
            replace("new ${mojmapLast}", "new ${mcpLast}")
            replace("/*non-final*/ ${mojmapLast}", "/*non-final*/ ${mcpLast}")
            replace("/*package-private*/ ${mojmapLast}", "/*package-private*/ ${mcpLast}")
            replace("/*shadow-final*/ ${mojmapLast}", "/*shadow-final*/ ${mcpLast}")
            replace("static ${mojmapLast}", "static ${mcpLast}")
        }
    }
}

minecraft {
    // Mappings.
    if (mcp <= "26.1.2") {
        mappings("official", mcv)
    }

    // Set up runs.
    runs {
        // Customize the client run.
        register("client") {
            // Set up debug VM args.
            if (javaVersion.isJava9Compatible) {
                jvmArgs("@../dev/args.vm.txt")
            } else {
                jvmArgs(rootDir.resolve("dev/args.vm.txt").readLines()
                    .filter { it.isNotEmpty() && !it.startsWith('#') && ("line.separator" !in it) })
            }

            // Set the run dir.
            workingDir = file("../../run")

            // Register source sets for debugging.
            mods {
                register("hcscr") {
                    source(sourceSets["main"])
                }
            }
        }
    }
}

// Repositories for dependencies.
repositories {
    mavenCentral()
    maven(fg.forgeMaven) // Forge.
    maven(fg.minecraftLibsMaven) // Minecraft Libraries.
    minecraft.mavenizer(this) // Minecraft.
}

// Dependencies.
dependencies {
    // Annotations.
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.error.prone.annotations)

    // Mixin.
    if (mcp < "1.20.6") {
        annotationProcessor("${libs.mixin.get()}:processor")
    }

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

    // Minecraft and Forge.
    val forge = "${property("loader")}"
    require(forge.isNotBlank() && forge != "null") { "Forge version is not provided via 'loader' in ${project}." }
    val extractedMinecraft = forge.substringBefore('-')
    require(mcp eq extractedMinecraft) { "Forge version '${forge}' provides Minecraft ${extractedMinecraft} in ${project}, but we want ${mcv}." }
    implementation(minecraft.dependency("net.minecraftforge:forge:${forge}"))
}

tasks.withType<JavaCompile> {
    // Compile with UTF-8.
    options.encoding = "UTF-8"

    // Set the compiler debug options.
    if ("${findProperty("ru.vidtu.hcscr.debug.javac") ?: findProperty("ru.vidtu.hcscr.debug")}".toBoolean()) {
        options.compilerArgs.addAll(listOf("-g", "-parameters"))
    } else if ("${findProperty("ru.vidtu.hcscr.slim")}".toBoolean()) {
        options.compilerArgs.add("-g:none")
    } else {
        options.compilerArgs.add("-g")
    }

    // Set the compatible Java target.
    // JDK 8 (used by 1.16.x) doesn't support the "-release" flag and
    // uses "-source" and "-target" ones (see the top of the file),
    // so we must NOT specify it, or the "javac" will fail.
    // JDK 9+ does listen to this option.
    if (javaVersion.isJava9Compatible) {
        options.release = javaTarget
    }

    // Post-process classes. (strip metadata)
    if (!"${findProperty("ru.vidtu.hcscr.debug.metadata") ?: findProperty("ru.vidtu.hcscr.debug")}".toBoolean()) {
        doLast {
            Strip(destinationDirectory.get().asFile, classpath).use { strip ->
                destinationDirectory.asFileTree
                    .filter { it.name != "package-info.class" }
                    .forEach { strip.stripBytecode(it) }
            }
        }
    }
}

sourceSets.main {
    // Add compile-time stub classes.
    java.srcDir("src/main/java-compile")

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
        property("minecraft", "${mcv}")
    }
}

tasks.withType<ProcessResources> {
    // Filter with UTF-8.
    filteringCharset = "UTF-8"

    // Exclude not needed loader entrypoint files.
    exclude("fabric.mod.json", "META-INF/neoforge.mods.toml")

    // Determine and replace the version range constraints.
    val constraints = "${project.property("constraints")}"
    require(constraints.isNotBlank() && constraints != "null") { "Constraints are not provided via 'constraints' in ${project}." }
    inputs.property("constraints", constraints)

    // Expand the updater URL.
    inputs.property("forgeUpdaterUrl", "https://raw.githubusercontent.com/VidTu/HCsCR/main/updater_hcscr_forge.json")

    // Expand Mixin Java version. Forge is full of edge-cases covered here.
    val mixinJava = if (mcp >= "26.1.2") 21
    else if (mcp eq "1.20.6") 18
    else javaTarget
    inputs.property("mixinJava", mixinJava)

    // Expand version and dependencies.
    inputs.property("minecraft", mcv)
    inputs.property("version", version)
    inputs.property("platform", "forge")
    filesMatching(listOf("hcscr.mixins.json", "META-INF/mods.toml")) {
        expand(inputs.properties)
    }

    // Minify JSON-alike (including ".mcmeta") and TOML files.
    if (!"${findProperty("ru.vidtu.hcscr.debug.resources") ?: findProperty("ru.vidtu.hcscr.debug")}".toBoolean()) {
        val files = fileTree(outputs.files.asPath)
        doLast {
            val jsonAlike = Regex("^.*\\.(?:json|mcmeta)$", RegexOption.IGNORE_CASE)
            files.forEach {
                if (it.name.matches(jsonAlike)) {
                    it.writeText(Gson().fromJson(it.readText(), JsonElement::class.java).toString())
                } else if (it.name.endsWith(".toml", ignoreCase = true)) {
                    it.writeText(it.readLines()
                        .filter { it.isNotEmpty() && !it.startsWith('#') }
                        .joinToString("\n")
                        .replace(" = ", "="))
                }
            }
        }
    }
}

tasks.withType<Jar> {
    // Add LICENSE and NOTICE.
    from(rootDir.resolve("LICENSE"))
    from(rootDir.resolve("NOTICE"))

    // Exclude compile-only code.
    exclude("ru/vidtu/hcscr/compile/**")

    // Remove package-info.class, unless package debug is on. (to save space)
    if (!"${findProperty("ru.vidtu.hcscr.debug.package") ?: findProperty("ru.vidtu.hcscr.debug")}".toBoolean()) {
        exclude("**/package-info.class")
    }

    // Add manifest.
    manifest {
        attributes("MixinConfigs" to "hcscr.mixins.json")
    }
}

if (mcp >= "1.20.6") {
    // Output into "build/libs" instead of "versions/<ver>/build/libs".
    tasks.withType<Jar> {
        destinationDirectory = rootProject.layout.buildDirectory.file("libs").get().asFile
    }
} else {
    // Rename. (remap)
    renamer {
        // Specify mappings.
        mappings(minecraft.dependency.toSrg)

        // Remap mixins.
        enableMixinRefmaps {
            config("hcscr.mixins.json")
            source(sourceSets["main"]) {
                refMap = "hcscr.mixins.refmap.json"
            }
            jar(tasks.named<Jar>("jar"))
        }

        // Use Mixin mappings for field remapping.
        classes(tasks.named<Jar>("jar")) {
            mappings(renamer.mixin.generatedMappings)
            archiveClassifier = "srg"
        }
    }

    // Output remapped JAR into "build/libs" instead of "versions/<ver>/build/libs".
    tasks.withType<RenameJar> {
        output = rootProject.layout.buildDirectory.file("libs").get().asFile.resolve("HCsCR-${version}.jar")
    }
}
