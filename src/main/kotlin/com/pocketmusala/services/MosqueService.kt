package com.pocketmusala.services

import com.pocketmusala.config.FirestoreConfig
import com.pocketmusala.models.*
import com.google.cloud.firestore.QueryDocumentSnapshot
import kotlin.math.*

class MosqueService {
    
    private val firestore = FirestoreConfig.getFirestore()
    private val collectionName = FirestoreConfig.getCollectionName()
    
    fun findNearbyMosques(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): List<Mosque> {
        try {
            println("DEBUG: Starting mosque search for lat=$latitude, lng=$longitude, radius=$radiusKm")
            
            // Calculate bounding box for initial Firestore query
            val boundingBox = calculateBoundingBox(latitude, longitude, radiusKm)
            println("DEBUG: Bounding box calculated")
            
            // Query Firestore with bounding box (synchronous)
            val query = firestore.collection(collectionName)
                .whereGreaterThanOrEqualTo("location.latitude", boundingBox.minLat)
                .whereLessThanOrEqualTo("location.latitude", boundingBox.maxLat)
                .whereGreaterThanOrEqualTo("location.longitude", boundingBox.minLng)
                .whereLessThanOrEqualTo("location.longitude", boundingBox.maxLng)
            
            println("DEBUG: About to execute Firestore query on collection '$collectionName'")
            val querySnapshot = query.get().get() // Synchronous call - no await needed
            println("DEBUG: Query returned ${querySnapshot.documents.size} documents")
            
            // Filter results by exact distance and convert to Mosque objects
            val mosques = querySnapshot.documents
                .mapNotNull { doc -> 
                    println("DEBUG: Processing document ${doc.id}")
                    convertDocumentToMosque(doc)
                }
                .filter { mosque ->
                    val distance = calculateDistance(
                        latitude, longitude,
                        mosque.location.latitude, mosque.location.longitude
                    )
                    println("DEBUG: Mosque ${mosque.id} is ${distance}km away")
                    distance <= radiusKm
                }
                .sortedBy { mosque ->
                    calculateDistance(
                        latitude, longitude,
                        mosque.location.latitude, mosque.location.longitude
                    )
                }
            
            println("DEBUG: Returning ${mosques.size} mosques")
            return mosques
            
        } catch (e: Exception) {
            println("ERROR in findNearbyMosques: ${e.message}")
            e.printStackTrace()
            return emptyList() // Return empty list instead of throwing
        }
    }
    
    private fun convertDocumentToMosque(doc: QueryDocumentSnapshot): Mosque? {
        return try {
            val data = doc.data
            val locationData = data["location"] as? Map<String, Any> ?: return null
            
            // Handle missing fields gracefully
            val thumbnailData = data["thumbnail"] as? Map<String, Any> ?: mapOf(
                "url" to "",
                "storageReference" to ""
            )
            val photosData = data["photos"] as? List<Map<String, Any>> ?: emptyList()
            val calculationData = data["calculationParameters"] as? Map<String, Any> ?: emptyMap()
            
            Mosque(
                id = doc.id,
                firstName = data["firstName"] as? String ?: "",
                lastName = data["lastName"] as? String ?: "",
                email = data["email"] as? String ?: "",
                phoneNumber = data["phoneNumber"] as? String ?: "",
                location = Location(
                    latitude = (locationData["latitude"] as? Number)?.toDouble() ?: 0.0,
                    longitude = (locationData["longitude"] as? Number)?.toDouble() ?: 0.0,
                    address = locationData["address"] as? String ?: ""
                ),
                communityName = data["communityName"] as? String ?: "",
                lastUpdatedAt = data["lastUpdatedAt"] as? String ?: "",
                thumbnail = Photo(
                    url = thumbnailData["url"] as? String ?: "",
                    storageReference = thumbnailData["storageReference"] as? String ?: ""
                ),
                donationLink = data["donationLink"] as? String ?: "",
                isDebug = data["isDebug"] as? Boolean ?: false,
                verificationStatus = data["verificationStatus"] as? String ?: "Unverified",
                photos = photosData.map { photo ->
                    Photo(
                        url = photo["url"] as? String ?: "",
                        storageReference = photo["storageReference"] as? String ?: ""
                    )
                },
                calculationParameters = parseCalculationParameters(calculationData)
            )
        } catch (e: Exception) {
            println("Error converting document ${doc.id}: ${e.message}")
            null
        }
    }
    
    private fun parseCalculationParameters(data: Map<String, Any>): CalculationParameters {
        val iqamahRules = data["iqamahCalculationRules"] as? Map<String, Any> ?: emptyMap()
        val advancedSettings = data["advancedSettings"] as? Map<String, Any> ?: emptyMap()
        val prayerAdjustments = advancedSettings["prayerAdjustments"] as? Map<String, Any> ?: emptyMap()
        
        return CalculationParameters(
            method = data["method"] as? String ?: "MoonSighting",
            madhabType = data["madhabType"] as? String ?: "Default",
            iqamahCalculationRules = IqamahCalculationRules(
                fajr = parseIqamahRule(iqamahRules["fajr"] as? Map<String, Any>, "Fajr"),
                dhuhr = parseIqamahRule(iqamahRules["dhuhr"] as? Map<String, Any>, "Dhuhr"),
                asr = parseIqamahRule(iqamahRules["asr"] as? Map<String, Any>, "Asr"),
                maghrib = parseIqamahRule(iqamahRules["maghrib"] as? Map<String, Any>, "Maghrib"),
                isha = parseIqamahRule(iqamahRules["isha"] as? Map<String, Any>, "Isha")
            ),
            advancedSettings = AdvancedSettings(
                elevationRule = advancedSettings["elevationRule"] as? String,
                fajrAngle = (advancedSettings["fajrAngle"] as? Number)?.toDouble() ?: 18.0,
                ishaAngle = (advancedSettings["ishaAngle"] as? Number)?.toDouble() ?: 17.0,
                prayerAdjustments = PrayerAdjustments(
                    fajr = (prayerAdjustments["fajr"] as? Number)?.toInt() ?: 0,
                    sunrise = (prayerAdjustments["sunrise"] as? Number)?.toInt() ?: 0,
                    dhuhr = (prayerAdjustments["dhuhr"] as? Number)?.toInt() ?: 0,
                    asr = (prayerAdjustments["asr"] as? Number)?.toInt() ?: 0,
                    maghrib = (prayerAdjustments["maghrib"] as? Number)?.toInt() ?: 0,
                    isha = (prayerAdjustments["isha"] as? Number)?.toInt() ?: 0
                )
            )
        )
    }
    
    private fun parseIqamahRule(data: Map<String, Any>?, prayerType: String): IqamahRule {
        return IqamahRule(
            prayerType = prayerType,
            type = data?.get("type") as? String ?: "Variable",
            fixedTime = data?.get("fixedTime") as? String,
            minutesAfter = (data?.get("minutesAfter") as? Number)?.toInt() ?: 10
        )
    }
    
    private data class BoundingBox(
        val minLat: Double,
        val maxLat: Double,
        val minLng: Double,
        val maxLng: Double
    )
    
    private fun calculateBoundingBox(lat: Double, lng: Double, radiusKm: Double): BoundingBox {
        val latDelta = radiusKm / 111.0
        val lngDelta = radiusKm / (111.0 * cos(Math.toRadians(lat)))
        
        return BoundingBox(
            minLat = lat - latDelta,
            maxLat = lat + latDelta,
            minLng = lng - lngDelta,
            maxLng = lng + lngDelta
        )
    }
    
    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
}