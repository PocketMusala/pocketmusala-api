package com.pocketmusala.models

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

@Serializable
data class Photo(
    val url: String,
    val storageReference: String
)

@Serializable
data class IqamahRule(
    val prayerType: String,
    val type: String, // "Variable" or "Fixed"
    val fixedTime: String? = null,
    val minutesAfter: Int? = null
)

@Serializable
data class IqamahCalculationRules(
    val fajr: IqamahRule,
    val dhuhr: IqamahRule,
    val asr: IqamahRule,
    val maghrib: IqamahRule,
    val isha: IqamahRule
)

@Serializable
data class PrayerAdjustments(
    val fajr: Int,
    val sunrise: Int,
    val dhuhr: Int,
    val asr: Int,
    val maghrib: Int,
    val isha: Int
)

@Serializable
data class AdvancedSettings(
    val elevationRule: String? = null,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val prayerAdjustments: PrayerAdjustments
)

@Serializable
data class CalculationParameters(
    val method: String,
    val madhabType: String,
    val iqamahCalculationRules: IqamahCalculationRules,
    val advancedSettings: AdvancedSettings
)

@Serializable
data class Mosque(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val location: Location,
    val communityName: String,
    val lastUpdatedAt: String,
    val thumbnail: Photo,
    val donationLink: String,
    val isDebug: Boolean,
    val verificationStatus: String,
    val photos: List<Photo>,
    val calculationParameters: CalculationParameters
)

@Serializable
data class NearbyMosquesResponse(
    val mosques: List<Mosque>
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)