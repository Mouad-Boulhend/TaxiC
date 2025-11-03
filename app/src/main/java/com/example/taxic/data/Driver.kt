package com.example.taxic.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Driver(
    val firstName: String,
    val lastName: String,
    val age: Int,
    val licenseType: String,
    val phoneNumber: String,
    val photoUrl: String? = null,
    val carModel: String? = null,
    val carPlate: String? = null,
    val rating: Float = 5.0f
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