package com.pocketmusala.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import io.ktor.server.application.*
import java.io.ByteArrayInputStream

fun Application.configureFirebase() {
    val projectId = System.getenv("GOOGLE_CLOUD_PROJECT") ?: "your-project-id"
    val credentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON")
    
    val options = if (credentials != null) {
        FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(ByteArrayInputStream(credentials.toByteArray())))
            .setProjectId(projectId)
            .build()
    } else {
        // Use default credentials (for local development with gcloud auth)
        FirebaseOptions.builder()
            .setProjectId(projectId)
            .build()
    }
    
    // Initialize Firebase if not already initialized
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