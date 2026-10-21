package com.barakahtravel.staff

class AppRepository(private val api: ApiService) {

    suspend fun getCustomers(): List<Customer> = api.getCustomers()
    suspend fun saveCustomer(c: Customer): Customer =
        if (c.id == 0) api.createCustomer(c) else api.updateCustomer(c.id, c)

    suspend fun generateCode(customer: Customer): Customer =
        api.generateRegistrationCode(customer.id)

    suspend fun validateCode(code: String): Boolean =
        api.validateRegistrationCode(code)["valid"] ?: false

    suspend fun getApplications(): List<TravelApplication> = api.getApplications()

    suspend fun setApplicationStatus(id: Int, status: String): TravelApplication =
        api.updateApplicationStatus(id, mapOf("status" to status))

    suspend fun getPackages(): List<TravelPackage> = api.getPackages()
    suspend fun getBookings(): List<Booking> = api.getBookings()
    suspend fun getPayments(): List<Payment> = api.getPayments()
    suspend fun getSchedules(): List<TravelSchedule> = api.getSchedules()
    suspend fun getDocuments(): List<StaffDocument> = api.getDocuments()
}