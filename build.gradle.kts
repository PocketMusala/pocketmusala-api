val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.4"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1" 
}

group = "com.pocketmusala"
version = "1.0.0"

application {
    mainClass.set("com.pocketmusala.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor server
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-config-yaml")
    
    // Content negotiation & serialization
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    
    // CORS support
    implementation("io.ktor:ktor-server-cors")
    
    // Status pages for error handling
    implementation("io.ktor:ktor-server-status-pages")
    
    // Call logging - ADD THIS LINE
    implementation("io.ktor:ktor-server-call-logging-jvm")
    
    // Firebase Admin SDK for Firestore
    implementation("com.google.firebase:firebase-admin:9.2.0")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")
    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // Testing
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

tasks {
    shadowJar {
        archiveFileName.set("pocketmusala-api-${version}-all.jar")
    }
}
