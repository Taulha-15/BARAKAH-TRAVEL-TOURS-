package com.barakahtravel.staff

object Validation {
    private val emailRe = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val phoneRe = Regex("^0\\d{2}\\s?\\d{3}\\s?\\d{4}$")

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !emailRe.matches(email.trim()) -> "Enter a valid email address"
        else -> null
    }

    fun validatePassword(pw: String): String? = when {
        pw.isBlank() -> "Password is required"
        pw.length < 8 -> "Password must be at least 8 characters"
        !pw.any { it.isUpperCase() } -> "Password must contain an uppercase letter"
        !pw.any { it.isDigit() } -> "Password must contain a digit"
        else -> null
    }

    fun validatePhone(phone: String): String? = when {
        phone.isBlank() -> "Phone number is required"
        !phoneRe.matches(phone.trim()) -> "Format: 082 555 0101"
        else -> null
    }

    fun validateName(name: String): String? = when {
        name.isBlank() -> "Name is required"
        name.length < 2 -> "Name is too short"
        else -> null
    }

    fun validateRegistrationCode(code: String): String? = when {
        code.isBlank() -> "Code is required"
        !code.startsWith("BRK-") -> "Code must start with BRK-"
        code.length != 12 -> "Code must be 12 characters (BRK-XXXXXXXX)"
        else -> null
    }
}