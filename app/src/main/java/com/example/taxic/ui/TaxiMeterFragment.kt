package com.example.taxic.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.taxic.R
import com.example.taxic.service.NotificationHelper
import com.example.taxic.viewmodel.TaxiMeterViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import pub.devrel.easypermissions.EasyPermissions

/**
 * ===========================================
 * TAXI METER FRAGMENT
 * ===========================================
 *
 * This is the main screen of our app.
 * It shows:
 * - Google Map with current location
 * - Distance, Time, and Fare
 * - Start/Stop button
 * - Profile button
 *
 * WHAT IT DOES:
 * 1. Shows a map
 * 2. Gets GPS location updates
 * 3. Sends location to ViewModel
 * 4. Displays updated distance/time/fare
 */
class TaxiMeterFragment : Fragment(), OnMapReadyCallback {

    // ===========================================
    // CONSTANTS
    // ===========================================

    companion object {
        private const val TAG = "TaxiMeterFragment"

        // How often to request location updates (milliseconds)
        private const val LOCATION_UPDATE_INTERVAL = 2000L  // 2 seconds

        // Fastest we'll accept updates (milliseconds)
        private const val FASTEST_UPDATE_INTERVAL = 1000L  // 1 second

        // Only update if moved this many meters
        private const val MINIMUM_DISPLACEMENT = 5f  // 5 meters

        // Default zoom level for map
        private const val MAP_ZOOM_LEVEL = 16f
    }


    // ===========================================
    // VARIABLES
    // ===========================================

    // ViewModel (shared with MainActivity)
    // "by activityViewModels()" gets the SAME ViewModel as MainActivity
    private val taxiViewModel: TaxiMeterViewModel by activityViewModels()

    // Google Map object
    private var googleMap: GoogleMap? = null

    // Location client (provides GPS updates)
    private lateinit var locationClient: FusedLocationProviderClient

    // Location callback (receives GPS updates)
    private lateinit var locationUpdateCallback: LocationCallback

    // UI Elements (views from XML)
    private lateinit var textViewDistance: TextView
    private lateinit var textViewTime: TextView
    private lateinit var textViewFare: TextView
    private lateinit var buttonStartStop: Button
    private lateinit var buttonProfile: ImageView


    // ===========================================
    // LIFECYCLE METHODS
    // ===========================================

    /**
     * onCreateView() creates the view hierarchy
     *
     * Think of it like inflating a balloon:
     * - We have a blueprint (XML file)
     * - We inflate it to create the actual view
     *
     * @param inflater Tool to create views from XML
     * @param container Parent view that will hold our fragment
     * @param savedInstanceState Previous state (if any)
     * @return The created view
     */
    fun toEnglishDigits(input: String): String {
        return input
            .replace('٠', '0')
            .replace('١', '1')
            .replace('٢', '2')
            .replace('٣', '3')
            .replace('٤', '4')
            .replace('٥', '5')
            .replace('٦', '6')
            .replace('٧', '7')
            .replace('٨', '8')
            .replace('٩', '9')
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "Creating view from XML")

        // Inflate the XML layout into a View object
        return inflater.inflate(R.layout.fragment_taxi_meter, container, false)
    }

    /**
     * onViewCreated() is called after the view is created
     * This is where we set up everything
     *
     * @param view The view we created
     * @param savedInstanceState Previous state (if any)
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "View created, setting up...")

        try {
            // Step 1: Find all UI elements
            findAllViews(view)

            // Step 2: Set up Google Maps
            setupGoogleMap()

            // Step 3: Set up location client
            setupLocationClient()

            // Step 4: Set up location callback
            setupLocationCallback()

            // Step 5: Watch ViewModel for changes
            observeViewModel()

            // Step 6: Set up button click listeners
            setupButtonClickListeners()

            Log.d(TAG, "Setup complete!")

        } catch (error: Exception) {
            Log.e(TAG, "Error during setup", error)
            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }



    // ===========================================
    // SETUP METHODS
    // ===========================================

    /**
     * Find all UI elements by their IDs
     *
     * findViewById() looks up views by ID from XML
     * We save references so we can update them later
     */
    private fun findAllViews(view: View) {
        Log.d(TAG, "Finding all views...")

        textViewDistance = view.findViewById(R.id.tvDistance)
        textViewTime = view.findViewById(R.id.tvTime)
        textViewFare = view.findViewById(R.id.tvFare)
        buttonStartStop = view.findViewById(R.id.btnStartStop)
        buttonProfile = view.findViewById(R.id.btnProfile)

        Log.d(TAG, "All views found")
    }

    /**
     * Set up Google Maps
     *
     * Maps are loaded in a separate Fragment (SupportMapFragment)
     * We find it and tell it to notify us when the map is ready
     */
    private fun setupGoogleMap() {
        Log.d(TAG, "Setting up Google Maps...")

        // Find the map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment

        // Request map asynchronously
        // When ready, onMapReady() will be called
        mapFragment?.getMapAsync(this)

    }

    /**
     * Set up location client
     *
     * This client provides GPS location updates
     */
    private fun setupLocationClient() {
        Log.d(TAG, "Setting up location client...")

        locationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        Log.d(TAG, "Location client ready")
    }

    /**
     * Set up location callback
     *
     * This callback is called whenever we receive a new GPS location
     */
    private fun setupLocationCallback() {
        Log.d(TAG, "Setting up location callback...")

        locationUpdateCallback = object : LocationCallback() {
            /**
             * Called when new location is available
             */
            override fun onLocationResult(locationResult: LocationResult) {
                // Get the latest location
                val newLocation = locationResult.lastLocation

                if (newLocation != null) {
                    Log.d(TAG, "New location: ${newLocation.latitude}, ${newLocation.longitude}")

                    // Update map camera
                    updateMapCamera(newLocation)

                    // Send location to ViewModel
                    taxiViewModel.updateLocation(newLocation)
                } else {
                    Log.w(TAG, "Received null location")
                }
            }
        }

        Log.d(TAG, "Location callback ready")
    }

    /**
     * Observe ViewModel changes
     *
     * "Observe" means "watch for changes"
     * Whenever ViewModel data changes, update the UI
     *
     * This is the "LiveData Observer Pattern"
     */
    private fun observeViewModel() {
        Log.d(TAG, "Setting up observers...")

        // Watch distance changes
        taxiViewModel.distanceInKm.observe(viewLifecycleOwner) { distance ->
            // When distance changes, update the text
            textViewDistance.text = toEnglishDigits(taxiViewModel.getDistanceText())
        }

        // Watch time changes
        taxiViewModel.timeInSeconds.observe(viewLifecycleOwner) { time ->
            // When time changes, update the text
            textViewTime.text = toEnglishDigits(taxiViewModel.getTimeText())
        }

        // Watch fare changes
        taxiViewModel.totalFare.observe(viewLifecycleOwner) { fare ->
            // When fare changes, update the text
            textViewFare.text = toEnglishDigits(taxiViewModel.getFareText())
        }

        // Watch ride active status
        taxiViewModel.isRideActive.observe(viewLifecycleOwner) { isActive ->
            // Update button appearance
            updateButtonAppearance(isActive)

            // Start or stop location updates
            if (isActive) {
                startReceivingLocationUpdates()
            } else {
                stopReceivingLocationUpdates()
            }
        }

        Log.d(TAG, "Observers set up")
    }

    /**
     * Set up button click listeners
     *
     * Define what happens when user clicks buttons
     */
    private fun setupButtonClickListeners() {
        Log.d(TAG, "Setting up button listeners...")

        // Start/Stop button
        buttonStartStop.setOnClickListener {
            // Check if ride is currently active
            val isRideActive = taxiViewModel.isRideActive.value ?: false

            if (isRideActive) {
                // Ride is active -> Stop it
                handleStopButtonClick()
            } else {
                // Ride is not active -> Start it
                handleStartButtonClick()
            }
        }

        // Profile button
        buttonProfile.setOnClickListener {
            handleProfileButtonClick()
        }

        Log.d(TAG, "Button listeners set up")
    }

    private fun isNightMode(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }


    // ===========================================
    // GOOGLE MAPS CALLBACK
    // ===========================================

    /**
     * Called when Google Map is ready to use
     *
     * This is where we configure the map settings
     *
     * @SuppressLint("MissingPermission") tells Android Studio
     * "I know I need permission, but I already checked it elsewhere"
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG, "Google Map is ready!")

        // Save reference to map
        googleMap = map

        try {
            val styleRes = if (isNightMode()) R.raw.map_dark else R.raw.map_light
            val success = map.setMapStyle(context?.let { MapStyleOptions.loadRawResourceStyle(it, styleRes) })

            if (!success) {
                Log.e("MapStyle", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MapStyle", "Can't find style. Error: ", e)
        }

        //googleMap?.setMapStyle(context?.let { MapStyleOptions.loadRawResourceStyle(it, R.raw.map_style) })

        // Configure map UI
        googleMap?.apply {
            uiSettings.isZoomControlsEnabled = false        // remove zoom +/- buttons
            uiSettings.isMyLocationButtonEnabled = false    // remove the blue "my location" button
            uiSettings.isCompassEnabled = false             // remove compass
            uiSettings.isMapToolbarEnabled = false          // remove Google map toolbar (navigation icons)
            uiSettings.isIndoorLevelPickerEnabled = false   // remove indoor level picker
            uiSettings.isScrollGesturesEnabled = true       // still allow scrolling
            uiSettings.isRotateGesturesEnabled = true       // still allow rotation
            uiSettings.isTiltGesturesEnabled = true         // still allow tilt
            setPadding(150, 0, 0, 120)
        }

        // Check if we have permission
        if (hasLocationPermission()) {
            try {
                // Enable the blue dot showing user location
                googleMap?.isMyLocationEnabled = true

                // Move camera to user's last known location
                moveToLastKnownLocation()

            } catch (error: SecurityException) {
                Log.e(TAG, "Security exception", error)
                Toast.makeText(context, "Permission error", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "No location permission")
            Toast.makeText(context,
                getString(R.string.location_permission_needed), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val styleRes = if ((newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        ) R.raw.map_dark else R.raw.map_light

        googleMap?.setMapStyle(context?.let { MapStyleOptions.loadRawResourceStyle(it, styleRes) })



        //if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
        //    googleMap?.setPadding(1000, 0, 0, 500)
        //}else{
        //    googleMap?.setPadding(60, 0, 0, 22)
        //}
    }

    /**
     * Move map camera to last known location
     */
    @SuppressLint("MissingPermission")
    private fun moveToLastKnownLocation() {
        locationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // We have a location - move camera there
                val position = LatLng(location.latitude, location.longitude)
                googleMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(position, MAP_ZOOM_LEVEL)
                )
                Log.d(TAG, "Moved camera to: ${location.latitude}, ${location.longitude}")
            } else {
                // No location available - show Rabat, Morocco as default
                val rabat = LatLng(33.9716, -6.8498)
                googleMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(rabat, 12f)
                )
                Log.d(TAG, "No location available, showing Rabat")
            }
        }
    }

    /**
     * Update map camera to follow new location
     */
    private fun updateMapCamera(location: Location) {
        val position = LatLng(location.latitude, location.longitude)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(position))
    }


    // ===========================================
    // BUTTON CLICK HANDLERS
    // ===========================================

    /**
     * Handle Start button click
     */
    private fun handleStartButtonClick() {
        Log.d(TAG, "Start button clicked")

        // Check permission first
        if (!hasLocationPermission()) {
            Toast.makeText(context, getString(R.string.location_permission_needed), Toast.LENGTH_SHORT).show()
            return
        }

        // Start the ride in ViewModel
        taxiViewModel.startRide()

    }

    /**
     * Handle Stop button click
     */
    private fun handleStopButtonClick() {
        Log.d(TAG, "Stop button clicked")

        // Stop the ride in ViewModel
        taxiViewModel.stopRide()

        // Send notification
        context?.let { ctx ->
            NotificationHelper.sendRideCompletedNotification(
                ctx,
                taxiViewModel.getRideSummary()
            )
        }

        // Show summary
        //Toast.makeText(
        //    context,
        //    getString(R.string.ride_ended)+"\n${taxiViewModel.getRideSummary()}",
        //    Toast.LENGTH_LONG
        //).show()
    }

    /**
     * Handle Profile button click
     */
    private fun handleProfileButtonClick() {
        Log.d(TAG, "Profile button clicked")

        // Create the profile fragment
        val profileFragment = DriverProfileFragment()

        // Navigate to profile screen
        // addToBackStack() lets user press back button to return here
        parentFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,    // Profile enters from left
                R.anim.fade_out,         // Map exits (fades)
                R.anim.fade_in,          // Map enters (fades in) when back
                R.anim.slide_out_right   // Profile exits to right when back
            )
            .replace(R.id.fragmentContainer, profileFragment)
            .addToBackStack(null)  // Add to back stack
            .commit()
    }


    // ===========================================
    // LOCATION UPDATES
    // ===========================================

    /**
     * Start receiving location updates from GPS
     *
     * @SuppressLint("MissingPermission") - we already checked permission
     */
    @SuppressLint("MissingPermission")
    private fun startReceivingLocationUpdates() {
        // Check permission
        if (!hasLocationPermission()) {
            return
        }

        Log.d(TAG, "Starting location updates...")

        // Create location request settings
        val locationRequest = LocationRequest.create().apply {
            interval = LOCATION_UPDATE_INTERVAL  // How often to request
            fastestInterval = FASTEST_UPDATE_INTERVAL  // Max speed to accept
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // Use GPS
            smallestDisplacement = MINIMUM_DISPLACEMENT  // Min distance to update
        }

        try {
            // Start receiving updates
            locationClient.requestLocationUpdates(
                locationRequest,
                locationUpdateCallback,
                Looper.getMainLooper()  // Receive on main thread
            )

            Log.d(TAG, "Location updates started")

        } catch (error: SecurityException) {
            Log.e(TAG, "Error starting location updates", error)
        }
    }

    /**
     * Stop receiving location updates
     */
    private fun stopReceivingLocationUpdates() {
        Log.d(TAG, "Stopping location updates...")

        locationClient.removeLocationUpdates(locationUpdateCallback)

        Log.d(TAG, "Location updates stopped")
    }


    // ===========================================
    // UI UPDATES
    // ===========================================

    /**
     * Update button appearance based on ride status
     *
     * @param isRideActive true if ride is ongoing, false if stopped
     */
    private fun updateButtonAppearance(isRideActive: Boolean) {
        if (isRideActive) {
            // Ride is active - show "Stop" button in red
            buttonStartStop.text = getString(R.string.stop_ride)
            buttonStartStop.setBackgroundColor(
                resources.getColor(android.R.color.holo_red_dark, null)
            )
        } else {
            // Ride is not active - show "Start" button in yellow
            buttonStartStop.text = getString(R.string.start_ride)
            buttonStartStop.setBackgroundColor(
                resources.getColor(R.color.taxi_yellow, null)
            )
        }
    }


    // ===========================================
    // PERMISSION CHECK
    // ===========================================

    /**
     * Check if we have location permission
     *
     * @return true if we have permission, false otherwise
     */
    private fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }


    // ===========================================
    // LIFECYCLE - CLEANUP
    // ===========================================

    /**
     * Called when fragment becomes visible
     */
    override fun onResume() {
        super.onResume()

        // If ride is active and we have permission, resume updates
        val isRideActive = taxiViewModel.isRideActive.value ?: false
        if (isRideActive && hasLocationPermission()) {
            startReceivingLocationUpdates()
        }
    }

    /**
     * Called when fragment is no longer visible
     */
    override fun onPause() {
        super.onPause()

        // Don't stop updates if ride is active
        // (user might just switch apps temporarily)
        val isRideActive = taxiViewModel.isRideActive.value ?: false
        if (!isRideActive) {
            stopReceivingLocationUpdates()
        }
    }

    /**
     * Called when view is destroyed
     */
    override fun onDestroyView() {
        super.onDestroyView()
        stopReceivingLocationUpdates()
    }


}


