plugins {
    kotlin("jvm") version "2.3.20"
    java
    application
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

application {
    mainClass.set("com.lightning323.packInstaller.Main")
}

sourceSets {
    main {
        java {
            // This tells Gradle to look for both Java and Kotlin files
            // in both directories during the compilation phase
            setSrcDirs(listOf("src/main/java", "src/main/kotlin"))
        }
    }
}