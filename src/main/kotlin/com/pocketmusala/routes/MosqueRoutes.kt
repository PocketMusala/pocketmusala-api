package com.pocketmusala.routes

import com.pocketmusala.config.FirestoreConfig
import com.pocketmusala.models.ErrorResponse
import com.pocketmusala.models.NearbyMosquesResponse
import com.pocketmusala.services.MosqueService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureApiRoutes() {
    val mosqueService = MosqueService()
    
    route("/mosques") {
        get("/nearby") {
            try {
                // Extract query parameters
                val lat = call.request.queryParameters["lat"]?.toDoubleOrNull()
                val lng = call.request.queryParameters["lng"]?.toDoubleOrNull()
                val radiusKm = call.request.queryParameters["radiusKm"]?.toDoubleOrNull()
                    ?: FirestoreConfig.getDefaultRadius()
                
                // Validate required parameters
                when {
                    lat == null -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Invalid latitude", "Parameter 'lat' is required and must be a valid number")
                        )
                        return@get
                    }
                    lng == null -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Invalid longitude", "Parameter 'lng' is required and must be a valid number")
                        )
                        return@get
                    }
                    lat !in -90.0..90.0 -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Invalid latitude", "Latitude must be between -90 and 90")
                        )
                        return@get
                    }
                    lng !in -180.0..180.0 -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Invalid longitude", "Longitude must be between -180 and 180")
                        )
                        return@get
                    }
                    radiusKm <= 0 || radiusKm > 100 -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Invalid radius", "Radius must be between 0 and 100 kilometers")
                        )
                        return@get
                    }
                }
                
                // Query nearby mosques (lat and lng are guaranteed non-null here)
                val mosques = mosqueService.findNearbyMosques(lat!!, lng!!, radiusKm)
                
                // Return response
                call.respond(
                    HttpStatusCode.OK,
                    NearbyMosquesResponse(mosques = mosques)
                )
                
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Server error", "An error occurred while searching for nearby mosques")
                )
            }
        }
    }
}