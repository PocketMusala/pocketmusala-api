package com.pocketmusala.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import io.ktor.server.application.*
import java.io.ByteArrayInputStream
import java.io.FileInputStream

fun Application.configureFirebase() {
    val projectId = System.getenv("GOOGLE_CLOUD_PROJECT") ?: "pocketmusala-api"
    val credentialsJson = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON")
    val credentialsFile = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
    
    val options = when {
        credentialsJson != null -> {
            log.info("Using JSON credentials")
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(ByteArrayInputStream(credentialsJson.toByteArray())))
                .setProjectId(projectId)
                .build()
        }
        credentialsFile != null -> {
            log.info("Using credentials file: $credentialsFile")
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(FileInputStream(credentialsFile)))
                .setProjectId(projectId)
                .build()
        }
        else -> {
            log.info("No credentials found, using default")
            FirebaseOptions.builder()
                .setProjectId(projectId)
                .build()
        }
    }
    
    if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options)
    }
}

object FirestoreConfig {
    fun getFirestore(): Firestore {
        return FirestoreClient.getFirestore()
    }
    
    fun getCollectionName(): String {
        return System.getenv("FIRESTORE_MOSQUES_COLLECTION") ?: "mosques"
    }
    
    fun getDefaultRadius(): Double {
        return System.getenv("DEFAULT_RADIUS_KM")?.toDouble() ?: 10.0
    }
}