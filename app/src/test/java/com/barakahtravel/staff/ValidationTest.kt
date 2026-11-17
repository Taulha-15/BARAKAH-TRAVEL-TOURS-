package com.barakahtravel.staff

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [Validation] – the input rules used on the sign-in,
 * registration, customer and settings forms. Pure Kotlin, so they run on the
 * JVM in GitHub Actions with ./gradlew :app:testDebugUnitTest.
 */
class ValidationTest {

    // ---------- Email ----------
    @Test fun email_blank_isRequired() =
        assertEquals("Email is required", Validation.validateEmail("   "))

    @Test fun email_missingAtSymbol_isRejected() =
        assertEquals("Enter a valid email address", Validation.validateEmail("staff.barakah.com"))

    @Test fun email_missingDomainSuffix_isRejected() =
        assertEquals("Enter a valid email address", Validation.validateEmail("staff@barakah"))

    @Test fun email_valid_isAccepted() =
        assertNull(Validation.validateEmail("staff@barakah.co.za"))

    @Test fun email_surroundingWhitespace_isTrimmed() =
        assertNull(Validation.validateEmail("  staff@barakah.com  "))

    // ---------- Password ----------
    @Test fun password_blank_isRequired() =
        assertEquals("Password is required", Validation.validatePassword(""))

    @Test fun password_tooShort_isRejected() =
        assertEquals("Password must be at least 8 characters", Validation.validatePassword("Ab1"))

    @Test fun password_noUppercase_isRejected() =
        assertEquals("Password must contain an uppercase letter", Validation.validatePassword("lowercase1"))

    @Test fun password_noDigit_isRejected() =
        assertEquals("Password must contain a digit", Validation.validatePassword("NoDigitsHere"))

    @Test fun password_meetingAllRules_isAccepted() =
        assertNull(Validation.validatePassword("Barakah2026"))

    @Test fun password_exactlyEightCharacters_isAccepted() =
        assertNull(Validation.validatePassword("Abcdefg1"))

    // ---------- Phone ----------
    @Test fun phone_blank_isRequired() =
        assertEquals("Phone number is required", Validation.validatePhone(""))

    @Test fun phone_spacedFormat_isAccepted() =
        assertNull(Validation.validatePhone("082 555 0101"))

    @Test fun phone_unspacedFormat_isAccepted() =
        assertNull(Validation.validatePhone("0825550101"))

    @Test fun phone_notStartingWithZero_isRejected() =
        assertEquals("Format: 082 555 0101", Validation.validatePhone("+27825550101"))

    @Test fun phone_tooShort_isRejected() =
        assertEquals("Format: 082 555 0101", Validation.validatePhone("082 555 01"))

    @Test fun phone_letters_areRejected() =
        assertEquals("Format: 082 555 0101", Validation.validatePhone("082 abc 0101"))

    // ---------- Name ----------
    @Test fun name_blank_isRequired() =
        assertEquals("Name is required", Validation.validateName(" "))

    @Test fun name_singleCharacter_isTooShort() =
        assertEquals("Name is too short", Validation.validateName("A"))

    @Test fun name_twoCharacters_isAccepted() =
        assertNull(Validation.validateName("Al"))

    // ---------- Registration code ----------
    @Test fun code_blank_isRequired() =
        assertEquals("Code is required", Validation.validateRegistrationCode(""))

    @Test fun code_wrongPrefix_isRejected() =
        assertEquals("Code must start with BRK-", Validation.validateRegistrationCode("ABC-12345678"))

    @Test fun code_wrongLength_isRejected() =
        assertEquals(
            "Code must be 12 characters (BRK-XXXXXXXX)",
            Validation.validateRegistrationCode("BRK-1234")
        )

    @Test fun code_validFormat_isAccepted() =
        assertNull(Validation.validateRegistrationCode("BRK-1A2B3C4D"))

    // ---------- Cross-checks ----------
    @Test fun everyValidator_returnsMessageForEmptyInput() {
        assertNotNull(Validation.validateEmail(""))
        assertNotNull(Validation.validatePassword(""))
        assertNotNull(Validation.validatePhone(""))
        assertNotNull(Validation.validateName(""))
        assertNotNull(Validation.validateRegistrationCode(""))
    }
}
