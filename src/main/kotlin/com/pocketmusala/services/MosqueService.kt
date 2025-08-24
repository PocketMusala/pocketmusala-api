package com.pocketmusala.services

import com.pocketmusala.config.FirestoreConfig
import com.pocketmusala.models.*
import kotlin.math.*

class MosqueService {
    
    suspend fun findNearbyMosques(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): List<Mosque> {
        // For now, return an empty list until Firestore is configured
        // This will let the project compile and run
        return emptyList()
    }
}