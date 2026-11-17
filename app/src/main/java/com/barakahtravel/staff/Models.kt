
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
    @SerialName("packageName")
    val packageName: String = "",
    val status: String = "Pending"
)

@Serializable
data class TravelPackage(
    val id: Int = 0,
    val name: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    val durationDays: Int = 0,
    val departureDate: String? = null,
    val returnDate: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class Booking(
    val id: Int = 0,
    val applicationId: Int = 0,
    val customerId: Int = 0,
    val customer: String = "",
    val packageId: Int = 0,
    val packageName: String = "",
    val ref: String = "",
    val status: String = "Pending",
    val bookingDate: String = "",
    val totalAmount: Double = 0.0,
    val notes: String? = null
)

@Serializable
data class Payment(
    val id: Int = 0,
    val bookingId: Int = 0,
    val customerId: Int = 0,
    val customer: String = "",
    val bookingReference: String = "",
    val amount: Double = 0.0,
    val paymentDate: String = "",
    val paymentMethod: String? = null,
    val referenceNumber: String? = null,
    val status: String = "Recorded",
    val notes: String? = null
)

@Serializable
data class TravelSchedule(
    val id: Int = 0,
    val bookingId: Int = 0,
    val bookingReference: String = "",
    val customer: String = "",
    val packageName: String = "",
    val departureDate: String = "",
    val returnDate: String = "",
    val departureTime: String? = null,
    val returnTime: String? = null,
    val departureAirport: String? = null,
    val arrivalAirport: String? = null,
    val airline: String? = null,
    val flightNumber: String? = null,
    val status: String = "Scheduled",
    val notes: String? = null
)

data class StaffDocument(
    val id: Int,
    val applicationId: Int,
    val customerName: String,
    val documentType: String,
    val fileName: String,
    val fileUrl: String,
    val status: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

@Serializable
data class GoogleLoginRequest(
    val idToken: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val email: String,
    val displayName: String,
    val role: String
)

@Serializable
data class UpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class UpdateProfileRequest(
    val displayName: String
)

@Serializable
data class ApiError(
    val message: String = "Unknown error"
)
