plugins {
    id("java")
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
java.toolchain.languageVersion = JavaLanguageVersion.of(17)
group = "ru.vidtu.hcscr"
base.archivesName = "HCsCR-Root"
description = "Remove your end crystals before the server even knows you hit 'em!"

repositories {
    mavenCentral()
}

dependencies {
    // Annotations (Compile)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.error.prone.annotations)

    // Generic (Provided)
    implementation(libs.gson)
    implementation(libs.slf4j)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    options.release = 17
}

tasks.withType<Jar> {
    from(rootDir.resolve("LICENSE"))
    from(rootDir.resolve("NOTICE"))
    manifest {
        attributes(
            "Specification-Title" to "HCsCR",
            "Specification-Version" to project.version,
            "Specification-Vendor" to "VidTu, Offenderify",
            "Implementation-Title" to "HCsCR-Root",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "VidTu, Offenderify"
        )
    }
}
