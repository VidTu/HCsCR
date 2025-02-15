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
 */

plugins {
    alias(libs.plugins.architectury.loom)
}

val javaLanguage = JavaLanguageVersion.of(property("dependencies.java").toString())
val javaNumber = javaLanguage.asInt()
val javaVersion = JavaVersion.toVersion(javaNumber)
java.sourceCompatibility = javaVersion
java.targetCompatibility = javaVersion
java.toolchain.languageVersion = javaLanguage

val currentProject = stonecutter.current.project
group = "ru.vidtu.hcscr"
base.archivesName = "HCsCR-Fabric-$currentProject"
description = "Remove your end crystals before the server even knows you hit 'em!"

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") // Fabric Loader and API.
    maven("https://maven.terraformersmc.com/releases/") // ModMenu.
    if (currentProject == "1.20.4") { // Workaround for ModMenu requiring Text Placeholder API.
        maven("https://maven.nucleoid.xyz/") // Text Placeholder API.
    }
}

loom {
    silentMojangMappingsLicense()
    runs.named("client") {
        vmArgs(
            // Allow JVM without hotswap to work.
            "-XX:+IgnoreUnrecognizedVMOptions",

            // Set up RAM.
            "-Xmx2G",

            // Allow hot swapping on supported JVM.
            "-XX:+AllowEnhancedClassRedefinition",
            "-XX:+AllowRedefinitionToAddDeleteMethods",
            "-XX:HotswapAgent=fatjar",
            "-Dfabric.debug.disableClassPathIsolation=true"
        )
    }
    @Suppress("UnstableApiUsage")
    mixin {
        defaultRefmapName = "hcscr.mixins.refmap.json"
    }
}

val dependenciesMinecraft = property("dependencies.minecraft")
dependencies {
    // Annotations
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.error.prone.annotations)

    // Dependencies
    implementation(libs.gson)
    implementation(libs.log4j)

    // Minecraft
    minecraft("com.mojang:minecraft:$dependenciesMinecraft")
    mappings(loom.officialMojangMappings())

    // Fabric
    modImplementation(libs.fabric.loader)
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("dependencies.fabric_api")}")
    modImplementation("com.terraformersmc:modmenu:${property("dependencies.modmenu")}")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    if (javaVersion.isJava9Compatible) {
        options.release = javaNumber
    }
}

tasks.withType<ProcessResources> {
    inputs.property("version", version)
    inputs.property("minecraftVersion", dependenciesMinecraft)
    filesMatching(listOf("fabric.mod.json", "quilt.mod.json")) {
        expand(inputs.properties)
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
            "Implementation-Title" to "HCsCR-Fabric-$currentProject",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "VidTu, Offenderify"
        )
    }
}
