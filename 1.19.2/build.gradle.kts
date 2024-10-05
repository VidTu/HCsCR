plugins {
    alias(libs.plugins.architectury.loom)
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
java.toolchain.languageVersion = JavaLanguageVersion.of(17)
group = "ru.vidtu.hcscr"
base.archivesName = "HCsCR-1.19.2"
description = "Remove your end crystals before the server even knows you hit 'em!"

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://api.modrinth.com/maven/")
}

loom {
    silentMojangMappingsLicense()
    runs.named("client") {
        vmArgs(
            "-XX:+IgnoreUnrecognizedVMOptions",
            "-Xmx2G",
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

dependencies {
    // Annotations (Compile)
    compileOnlyApi(libs.jetbrains.annotations)
    compileOnlyApi(libs.error.prone.annotations)

    // Minecraft (Provided)
    minecraft(libs.minecraft.mc1192)
    mappings(loom.officialMojangMappings())

    // Fabric (Provided)
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.mc1192)
    modImplementation(libs.modmenu.mc1192)

    // Root
    compileOnly(rootProject)

    // Testing
    runtimeOnly(libs.lazydfu.mc1192)
}

tasks.withType<JavaCompile> {
    source(rootProject.sourceSets.main.get().java)
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    options.release = 17
}

tasks.withType<ProcessResources> {
    from(rootProject.sourceSets.main.get().resources)
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<Jar> {
    from(rootDir.resolve("LICENSE"))
    from(rootDir.resolve("NOTICE"))
    manifest {
        attributes(
            "Specification-Title" to "HCsCR",
            "Specification-Version" to project.version,
            "Specification-Vendor" to "VidTu, Offenderify",
            "Implementation-Title" to "HCsCR-1.19.2",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "VidTu, Offenderify"
        )
    }
}
