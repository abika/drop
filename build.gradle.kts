val ktor_version: String by project
//val kotlin_version: String by project
//val logback_version: String by project

val kotlin_version = "3.0.0"

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "3.0.0"
}

group = "org.abika"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-thymeleaf-jvm")
    implementation("io.ktor:ktor-server-host-common-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-network-tls-certificates")

    runtimeOnly("org.bouncycastle:bcprov-jdk18on:1.78.1")
    runtimeOnly("org.bouncycastle:bcpkix-jdk18on:1.78.1")

    //implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")
    implementation("ch.qos.logback:logback-classic:1.5.11")

    implementation("com.github.ajalt.clikt:clikt:5.0.1")

    testImplementation("io.ktor:ktor-server-test-host-jvm")
    //testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}


kotlin {
    jvmToolchain(17)
}
