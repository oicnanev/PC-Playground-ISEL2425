plugins {
    kotlin("jvm") version "2.1.10" // Verifique se esta é a versão correta do Kotlin
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral() // Certifique-se de que o Maven Central está configurado
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // Dependências de teste
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform() // Configura o uso do JUnit Platform
}

kotlin {
    jvmToolchain(21) // Define a versão do JDK como 21
}