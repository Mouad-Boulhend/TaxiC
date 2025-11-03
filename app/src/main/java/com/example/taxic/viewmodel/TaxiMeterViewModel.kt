package com.example.taxic.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taxic.data.Driver
import kotlin.math.roundToInt

/**
 * ViewModel for the Taxi Meter functionality
 * Handles all business logic for fare calculation, distance tracking, and time tracking
 */
class TaxiMeterViewModel : ViewModel() {

    // Fare Configuration (in DH - Moroccan Dirham)
    private val BASE_FARE = 2.5
    private val FARE_PER_KM = 1.5
    private val FARE_PER_MINUTE = 0.5

    // LiveData for UI observation
    private val _distance = MutableLiveData<Double>(0.0)
    val distance: LiveData<Double> = _distance

    private val _elapsedTime = MutableLiveData<Long>(0L) // in seconds
    val elapsedTime: LiveData<Long> = _elapsedTime

    private val _totalFare = MutableLiveData<Double>(BASE_FARE)
    val totalFare: LiveData<Double> = _totalFare

    private val _isRideActive = MutableLiveData<Boolean>(false)
    val isRideActive: LiveData<Boolean> = _isRideActive

    private val _driverProfile = MutableLiveData<Driver>()
    val driverProfile: LiveData<Driver> = _driverProfile

    // Private tracking variables
    private var startTime: Long = 0L
    private var lastLocation: Location? = null
    private var totalDistanceMeters: Double = 0.0

    init {
        // Initialize with sample driver data
        // In a real app, this would come from a database or API
        _driverProfile.value = Driver(
            firstName = "Mohammed",
            lastName = "Alami",
            age = 35,
            licenseType = "Professional Driver License",
            photoUrl = null,
            phoneNumber = "+212 6XX XXX XXX"
        )
    }

    /**
     * Start the ride - called when "Démarrer la course" button is pressed
     */
    fun startRide() {
        if (_isRideActive.value == true) return

        _isRideActive.value = true
        startTime = System.currentTimeMillis()
        totalDistanceMeters = 0.0
        lastLocation = null

        _distance.value = 0.0
        _elapsedTime.value = 0L
        _totalFare.value = BASE_FARE
    }

    /**
     * Stop the ride - called when "Terminer la course" button is pressed
     */
    fun stopRide() {
        _isRideActive.value = false
        lastLocation = null
    }

    /**
     * Update location - called whenever GPS provides new location
     * @param newLocation The new location from GPS
     */
    fun updateLocation(newLocation: Location) {
        if (_isRideActive.value != true) return

        // Calculate distance from last location
        lastLocation?.let { last ->
            val distanceInMeters = last.distanceTo(newLocation)
            totalDistanceMeters += distanceInMeters

            // Convert to kilometers and update
            val distanceInKm = totalDistanceMeters / 1000.0
            _distance.value = distanceInKm
        }

        // Update last location
        lastLocation = newLocation

        // Calculate elapsed time
        val currentTime = System.currentTimeMillis()
        val elapsedMillis = currentTime - startTime
        _elapsedTime.value = elapsedMillis / 1000 // Convert to seconds

        // Recalculate fare
        calculateFare()
    }

    /**
     * Calculate the total fare based on distance and time
     */
    private fun calculateFare() {
        val distanceKm = _distance.value ?: 0.0
        val timeMinutes = (_elapsedTime.value ?: 0L) / 60.0

        val fare = BASE_FARE + (distanceKm * FARE_PER_KM) + (timeMinutes * FARE_PER_MINUTE)

        // Round to 2 decimal places
        _totalFare.value = (fare * 100).roundToInt() / 100.0
    }

    /**
     * Get formatted distance string
     */
    fun getFormattedDistance(): String {
        val dist = _distance.value ?: 0.0
        return String.format("%.2f km", dist)
    }

    /**
     * Get formatted time string
     */
    fun getFormattedTime(): String {
        val totalSeconds = _elapsedTime.value ?: 0L
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Get formatted fare string
     */
    fun getFormattedFare(): String {
        val fare = _totalFare.value ?: 0.0
        return String.format("%.2f DH", fare)
    }

    /**
     * Update driver profile
     */
    fun updateDriverProfile(driver: Driver) {
        _driverProfile.value = driver
    }

    /**
     * Get ride summary for notification
     */
    fun getRideSummary(): String {
        return """
            Distance: ${getFormattedDistance()}
            Temps: ${getFormattedTime()}
            Tarif Total: ${getFormattedFare()}
        """.trimIndent()
    }

    /**
     * Reset everything (useful for testing or new ride)
     */
    fun reset() {
        stopRide()
        _distance.value = 0.0
        _elapsedTime.value = 0L
        _totalFare.value = BASE_FARE
        totalDistanceMeters = 0.0
        lastLocation = null
    }
}