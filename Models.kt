package com.barakahtravel.staff

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val status: String = "Pending",
    val code: String = ""
)

@Serializable
data class TravelApplication(
    val id: Int = 0,
    val customer: String = "",
    @SerialName("packageName") val packageName: String = "",
    val status: String = "Pending"
)

@Serializable
data class TravelPackage(
    val id: Int = 0,
    val name: String = "",
    val durationDays: Int = 0,
    val priceZar: Int = 0,
    val available: Boolean = true
)

@Serializable
data class Booking(
    val id: Int = 0,
    val ref: String = "",
    val customer: String = "",
    val packageName: String = "",
    val status: String = "Pending"
)

@Serializable
data class Payment(
    val id: Int = 0,
    val customer: String = "",
    val amountZar: Int = 0,
    val status: String = "Awaiting payment"
)

@Serializable
data class TravelSchedule(
    val id: Int = 0,
    val date: String = "",
    val packageName: String = "",
    val departureCity: String = ""
)

@Serializable
data class StaffDocument(
    val id: Int = 0,
    val customer: String = "",
    val docType: String = "",
    val status: String = "Pending"
)

@Serializable data class LoginRequest(val email: String, val password: String)
@Serializable data class RegisterRequest(val email: String, val password: String, val displayName: String)
@Serializable data class GoogleLoginRequest(val idToken: String)
@Serializable data class AuthResponse(val token: String, val email: String, val displayName: String, val role: String)
@Serializable data class UpdatePasswordRequest(val currentPassword: String, val newPassword: String)
@Serializable data class UpdateProfileRequest(val displayName: String)
@Serializable data class ApiError(val message: String = "Unknown error")