package com.example.taxic.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * This class holds information about a taxi driver.
 *
 * Think of it like a form with fields:
 * - First name
 * - Last name
 * - Age
 * - etc.
 *
 * ALSO USED FOR LOGIN:
 * - username: The driver's login username
 * - password: The driver's login password
 *
 * @Parcelize lets us easily pass this data between screens
 */
@Parcelize
data class Driver(
    // Login credentials
    val username: String,        // Example: "mohalami"
    val password: String,        // Example: "pass123"

    // Personal information
    val firstName: String,       // Example: "Mohammed"
    val lastName: String,        // Example: "Alami"
    val age: Int,                // Example: 35
    val licenseType: String,     // Example: "Professional Driver License"
    val phoneNumber: String,     // Example: "+212 6XX XXX XXX"

    // Optional information
    val photoUrl: String? = null,      // Optional: Link to driver photo
    val carModel: String? = null,      // Optional: "Toyota Corolla"
    val carPlate: String? = null,      // Optional: "ABC-1234"
    val rating: Float = 5.0f           // Driver rating (out of 5)
) : Parcelable {

    /**
     * Combines first and last name into full name
     * Example: "Mohammed" + "Alami" = "Mohammed Alami"
     */
    fun getFullName(): String {
        return "$firstName $lastName"
    }

    /**
     * Creates a text string with all driver info
     * This will be used to generate a QR code later
     */
    fun getDriverInfoText(): String {
        val info = StringBuilder()
        info.append("TAXI DRIVER INFO\n")
        info.append("Name: ${getFullName()}\n")
        info.append("Age: $age\n")
        info.append("License: $licenseType\n")
        info.append("Phone: $phoneNumber\n")

        // Add optional info if available
        if (carModel != null) {
            info.append("Car: $carModel\n")
        }
        if (carPlate != null) {
            info.append("Plate: $carPlate\n")
        }

        info.append("Rating: $rating/5.0")

        return info.toString()
    }
}