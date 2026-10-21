package com.barakahtravel.staff

import retrofit2.http.*

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/auth/google")
    suspend fun googleLogin(@Body body: GoogleLoginRequest): AuthResponse

    @POST("api/auth/change-password")
    suspend fun changePassword(@Body body: UpdatePasswordRequest)

    @PUT("api/auth/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): AuthResponse

    // ---- Customers ----
    @GET("api/customers")
    suspend fun getCustomers(): List<Customer>

    @POST("api/customers")
    suspend fun createCustomer(@Body customer: Customer): Customer

    @PUT("api/customers/{id}")
    suspend fun updateCustomer(@Path("id") id: Int, @Body customer: Customer): Customer

    @POST("api/customers/{id}/registration-code")
    suspend fun generateRegistrationCode(@Path("id") id: Int): Customer

    @GET("api/customers/validate-code/{code}")
    suspend fun validateRegistrationCode(@Path("code") code: String): Map<String, Boolean>

    // ---- Applications ----
    @GET("api/applications")
    suspend fun getApplications(): List<TravelApplication>

    @PUT("api/applications/{id}/status")
    suspend fun updateApplicationStatus(@Path("id") id: Int, @Body body: Map<String, String>): TravelApplication

    // ---- Packages / Bookings / Payments / Schedules / Documents ----
    @GET("api/packages")      suspend fun getPackages(): List<TravelPackage>
    @GET("api/bookings")      suspend fun getBookings(): List<Booking>
    @GET("api/payments")      suspend fun getPayments(): List<Payment>
    @GET("api/schedules")     suspend fun getSchedules(): List<TravelSchedule>
    @GET("api/documents")     suspend fun getDocuments(): List<StaffDocument>
}