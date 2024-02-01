plugins {
    kotlin("jvm") version "1.9.22"
}

group = "com.craftinginterpreters.lox"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}

// Ref: https://stackoverflow.com/a/72285005/1754752
tasks.jar {
    manifest.attributes["Main-Class"] = "com.craftinginterpreters.lox.MainKt"
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree)
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}


kotlin {
    jvmToolchain(21)
}
