package com.example.taxic.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing a Taxi Driver
 * @Parcelize allows easy passing between fragments/activities
 */
@Parcelize
data class Driver(
    val firstName: String,
    val lastName: String,
    val age: Int,
    val licenseType: String,
    val photoUrl: String? = null,
    val phoneNumber: String,
    val carModel: String? = null,
    val carPlate: String? = null,
    val rating: Float = 5.0f
) : Parcelable {

    /**
     * Get full name of driver
     */
    fun getFullName(): String = "$firstName $lastName"

    /**
     * Generate QR Code data string
     * This string will be encoded into a QR code
     */
    fun toQRCodeData(): String {
        return """
            TAXI DRIVER INFO
            Name: ${getFullName()}
            Age: $age
            License: $licenseType
            Phone: $phoneNumber
            ${carModel?.let { "Car: $it" } ?: ""}
            ${carPlate?.let { "Plate: $it" } ?: ""}
            Rating: $rating/5.0
        """.trimIndent()
    }
}