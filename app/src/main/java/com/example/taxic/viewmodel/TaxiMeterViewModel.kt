package com.example.taxic.viewmodel

import android.content.res.loader.ResourcesProvider
import android.location.Location
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taxic.data.Driver
import kotlin.math.roundToInt

/**
 * ===========================================
 * TAXI METER VIEW MODEL
 * ===========================================
 *
 * This is the "brain" of our taxi meter app.
 * It stores all the data and does all the calculations.
 *
 * WHAT IT DOES:
 * 1. Tracks how long the ride has been going (timer)
 * 2. Tracks how far the taxi has traveled (distance)
 * 3. Calculates the fare (price) based on distance and time
 * 4. Stores driver information
 *
 * WHY USE VIEWMODEL:
 * - Data survives when you rotate the phone
 * - Separates logic from UI (cleaner code)
 * - Easy to test
 */
class TaxiMeterViewModel : ViewModel() {



    // ===========================================
    // CONSTANTS - Prices (in Moroccan Dirham)
    // ===========================================
    private val BASE_FARE = 2.5          // Starting price (2.5 DH)
    private val PRICE_PER_KILOMETER = 1.5  // 1.5 DH for each kilometer
    private val PRICE_PER_MINUTE = 0.5     // 0.5 DH for each minute


    // ===========================================
    // LIVEDATA - Data that the UI watches
    // ===========================================

    // Distance traveled (in kilometers)
    // Private _distance can be changed by ViewModel
    // Public distance can only be READ by the UI (safer!)
    private val _distanceInKm = MutableLiveData<Double>(0.0)
    val distanceInKm: LiveData<Double> = _distanceInKm

    // Time elapsed (in seconds)
    private val _timeInSeconds = MutableLiveData<Long>(0L)
    val timeInSeconds: LiveData<Long> = _timeInSeconds

    // Total fare/price (in DH)
    private val _totalFare = MutableLiveData<Double>(BASE_FARE)
    val totalFare: LiveData<Double> = _totalFare

    // Is the ride currently active?
    private val _isRideActive = MutableLiveData<Boolean>(false)
    val isRideActive: LiveData<Boolean> = _isRideActive

    // Driver profile information
    private val _driverProfile = MutableLiveData<Driver>()
    val driverProfile: LiveData<Driver> = _driverProfile


    // ===========================================
    // PRIVATE VARIABLES - Internal tracking
    // ===========================================

    // When did the ride start? (timestamp in milliseconds)
    private var rideStartTime: Long = 0L

    // The last GPS location we received
    private var lastKnownLocation: Location? = null

    // Total distance in METERS (we convert to KM for display)
    private var totalDistanceInMeters: Double = 0.0

    // Handler and Runnable for our timer
    private val timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null


    // ===========================================
    // INITIALIZATION
    // ===========================================

    init {
        // This runs when the ViewModel is first created
        // We'll set the actual driver when MainActivity gets it from login
    }

    /**
     * Set the logged-in driver
     * Called by MainActivity after login
     *
     * @param driver The logged-in driver
     */


    fun setDriver(driver: Driver) {
        _driverProfile.value = driver
    }


    // ===========================================
    // PUBLIC METHODS - Called by the UI
    // ===========================================

    /**
     * START THE RIDE
     *
     * Called when the driver presses "Start Ride" button
     *
     * What it does:
     * 1. Marks ride as active
     * 2. Records the start time
     * 3. Resets all counters to zero
     * 4. Starts the timer
     */
    fun startRide() {
        // Don't start if already active
        if (_isRideActive.value == true) {
            return
        }

        // Mark ride as active
        _isRideActive.value = true

        // Record when we started
        rideStartTime = System.currentTimeMillis()

        // Reset everything to zero
        totalDistanceInMeters = 0.0
        lastKnownLocation = null
        _distanceInKm.value = 0.0
        _timeInSeconds.value = 0L
        _totalFare.value = BASE_FARE

        // Start the timer
        startTimerUpdates()
    }

    /**
     * STOP THE RIDE
     *
     * Called when the driver presses "Stop Ride" button
     *
     * What it does:
     * 1. Marks ride as inactive
     * 2. Stops the timer
     * 3. Clears location data
     */
    fun stopRide() {
        // Mark ride as not active
        _isRideActive.value = false

        // Clear location
        lastKnownLocation = null

        // Stop the timer
        stopTimerUpdates()
    }

    /**
     * UPDATE LOCATION
     *
     * Called whenever GPS gives us a new location
     *
     * What it does:
     * 1. Calculates distance from last location
     * 2. Adds to total distance
     * 3. Updates the fare
     *
     * @param newLocation The new GPS location
     */
    fun updateLocation(newLocation: Location) {
        // Only update if ride is active
        if (_isRideActive.value != true) {
            return
        }

        // If we have a previous location, calculate distance
        val previousLocation = lastKnownLocation
        if (previousLocation != null) {
            // Calculate distance between old and new location (in meters)
            val distanceMovedInMeters = previousLocation.distanceTo(newLocation)

            // Add to total distance
            totalDistanceInMeters = totalDistanceInMeters + distanceMovedInMeters

            // Convert meters to kilometers (divide by 1000)
            val distanceInKilometers = totalDistanceInMeters / 1000.0

            // Update the LiveData so UI can see it
            _distanceInKm.value = distanceInKilometers
        }

        // Save this location for next time
        lastKnownLocation = newLocation

        // Recalculate the fare
        calculateAndUpdateFare()
    }


    // ===========================================
    // PRIVATE METHODS - Internal logic
    // ===========================================

    /**
     * START THE TIMER
     *
     * Creates a repeating task that updates the time every second
     *
     * How it works:
     * 1. Calculate how much time has passed
     * 2. Update the time LiveData
     * 3. Recalculate the fare
     * 4. Schedule to run again in 1 second
     */
    private fun startTimerUpdates() {
        // Create a task that repeats
        timerRunnable = object : Runnable {
            override fun run() {
                // Only run if ride is active
                if (_isRideActive.value == true) {
                    // Calculate elapsed time
                    updateElapsedTime()

                    // Recalculate fare
                    calculateAndUpdateFare()

                    // Schedule to run again in 1 second (1000 milliseconds)
                    timerHandler.postDelayed(this, 1000)
                }
            }
        }

        // Start the timer
        timerHandler.post(timerRunnable!!)
    }

    /**
     * STOP THE TIMER
     *
     * Cancels the repeating timer task
     */
    private fun stopTimerUpdates() {
        // Remove the repeating task
        timerRunnable?.let { runnable ->
            timerHandler.removeCallbacks(runnable)
        }
        timerRunnable = null
    }

    /**
     * UPDATE ELAPSED TIME
     *
     * Calculates how many seconds have passed since ride started
     */
    private fun updateElapsedTime() {
        // Get current time
        val currentTime = System.currentTimeMillis()

        // Calculate difference (how long since start)
        val elapsedMilliseconds = currentTime - rideStartTime

        // Convert to seconds
        val elapsedSeconds = elapsedMilliseconds / 1000

        // Update LiveData
        _timeInSeconds.value = elapsedSeconds
    }

    /**
     * CALCULATE FARE
     *
     * Formula:
     * Total Fare = Base Fare + (Distance × Price per KM) + (Time × Price per Minute)
     *
     * Example:
     * Base: 2.5 DH
     * Distance: 5 km × 1.5 DH = 7.5 DH
     * Time: 10 minutes × 0.5 DH = 5 DH
     * Total: 2.5 + 7.5 + 5 = 15 DH
     */
    private fun calculateAndUpdateFare() {
        // Get current distance in kilometers
        val distance = _distanceInKm.value ?: 0.0

        // Get current time and convert to minutes
        val timeSeconds = _timeInSeconds.value ?: 0L
        val timeMinutes = timeSeconds / 60.0

        // Calculate each part
        val baseFare = BASE_FARE
        val distanceCost = distance * PRICE_PER_KILOMETER
        val timeCost = timeMinutes * PRICE_PER_MINUTE

        // Add them all together
        val totalCost = baseFare + distanceCost + timeCost

        // Round to 2 decimal places
        val roundedCost = (totalCost * 100).roundToInt() / 100.0

        // Update LiveData
        _totalFare.value = roundedCost
    }


    // ===========================================
    // FORMATTING METHODS - Convert data to text
    // ===========================================

    /**
     * Format distance for display
     * Example: 1.23 km
     */
    fun getDistanceText(): String {
        val distance = _distanceInKm.value ?: 0.0
        return String.format("%.2f km", distance)
    }

    /**
     * Format time for display
     * Example: 05:30 (5 minutes, 30 seconds)
     */
    fun getTimeText(): String {
        val totalSeconds = _timeInSeconds.value ?: 0L
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Format fare for display
     * Example: 15.50 DH
     */
    fun getFareText(): String {
        val fare = _totalFare.value ?: 0.0
        return String.format("%.2f DH", fare)
    }

    /**
     * Get ride summary for notification
     * Returns a multi-line text with all ride info
     */
    fun getRideSummary(): String {
        return ("Distance" + ": ${getDistanceText()}\n" +
                "Time" + ": ${getTimeText()}\n" +
                "Total Fare" + ": ${getFareText()}")
    }


    // ===========================================
    // CLEANUP
    // ===========================================

    /**
     * Called when ViewModel is destroyed
     * Clean up the timer to prevent memory leaks
     */
    override fun onCleared() {
        super.onCleared()
        stopTimerUpdates()
    }
}