plugins {
    kotlin("jvm") version "2.3.20"
}

group = "org.lightning323"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
// Jackson Core + TOML Support
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-toml:2.17.0")

    // Allows Jackson to work with Kotlin Data Classes
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}