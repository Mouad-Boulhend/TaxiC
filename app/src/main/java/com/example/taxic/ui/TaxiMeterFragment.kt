package com.example.taxic.ui

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.google.android.gms.maps.model.MarkerOptions
import pub.devrel.easypermissions.EasyPermissions

/**
 * Fragment that displays the taxi meter with Google Maps
 */
class TaxiMeterFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private const val TAG = "TaxiMeterFragment"
        private const val UPDATE_INTERVAL = 3000L // 3 seconds
        private const val FASTEST_INTERVAL = 1000L // 1 second
        private const val DEFAULT_ZOOM = 16f
    }

    // Shared ViewModel
    private val viewModel: TaxiMeterViewModel by activityViewModels()

    // Google Maps
    private var googleMap: GoogleMap? = null

    // Location services
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // UI Elements
    private lateinit var tvDistance: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvFare: TextView
    private lateinit var btnStartStop: Button
    private lateinit var btnProfile: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.fragment_taxi_meter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        try {
            // Initialize UI elements
            initializeViews(view)

            // Initialize location services
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

            // Setup Google Maps
            setupMap()

            // Setup location callback
            setupLocationCallback()

            // Observe ViewModel
            observeViewModel()

            // Setup buttons
            setupButtons()

            Log.d(TAG, "Fragment setup completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Initialize all view references
     */
    private fun initializeViews(view: View) {
        tvDistance = view.findViewById(R.id.tvDistance)
        tvTime = view.findViewById(R.id.tvTime)
        tvFare = view.findViewById(R.id.tvFare)
        btnStartStop = view.findViewById(R.id.btnStartStop)
        btnProfile = view.findViewById(R.id.btnProfile)
    }

    /**
     * Setup Google Maps
     */
    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    /**
     * Called when Google Map is ready
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG, "Map is ready")
        googleMap = map

        // Configure map settings
        googleMap?.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
            uiSettings.isCompassEnabled = true
        }

        if (hasLocationPermission()) {
            try {
                googleMap?.isMyLocationEnabled = true

                // Get last known location and move camera
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        googleMap?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM)
                        )
                        Log.d(TAG, "Camera moved to: ${it.latitude}, ${it.longitude}")
                    } ?: run {
                        // Default to Rabat, Morocco if no location
                        val rabat = LatLng(33.9716, -6.8498)
                        googleMap?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(rabat, 12f)
                        )
                        Log.d(TAG, "No location available, showing Rabat")
                    }
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Location permission error", e)
                Toast.makeText(context, "Erreur de permission", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "Location permission not granted")
            Toast.makeText(context, "Permission de localisation requise", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Setup location updates callback
     */
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d(TAG, "Location update: ${location.latitude}, ${location.longitude}")
                    updateMapLocation(location)
                    viewModel.updateLocation(location)
                }
            }
        }
    }

    /**
     * Update map with new location
     */
    private fun updateMapLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    /**
     * Observe ViewModel LiveData
     */
    private fun observeViewModel() {
        viewModel.distance.observe(viewLifecycleOwner) {
            tvDistance.text = viewModel.getFormattedDistance()
        }

        viewModel.elapsedTime.observe(viewLifecycleOwner) {
            tvTime.text = viewModel.getFormattedTime()
        }

        viewModel.totalFare.observe(viewLifecycleOwner) {
            tvFare.text = viewModel.getFormattedFare()
        }

        viewModel.isRideActive.observe(viewLifecycleOwner) { isActive ->
            updateButtonState(isActive)
            if (isActive) {
                startLocationUpdates()
            } else {
                stopLocationUpdates()
            }
        }
    }

    /**
     * Setup button listeners
     */
    private fun setupButtons() {
        btnStartStop.setOnClickListener {
            if (viewModel.isRideActive.value == true) {
                stopRide()
            } else {
                startRide()
            }
        }

        btnProfile.setOnClickListener {
            Toast.makeText(context, "Profil du chauffeur (à implémenter)", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Start the ride
     */
    private fun startRide() {
        if (!hasLocationPermission()) {
            Toast.makeText(context, "Permission de localisation requise", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.startRide()
        Toast.makeText(context, "Course démarrée!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Stop the ride
     */
    private fun stopRide() {
        viewModel.stopRide()

        // Send notification
        context?.let {
            NotificationHelper.sendRideCompletedNotification(it, viewModel.getRideSummary())
        }

        Toast.makeText(
            context,
            "Course terminée!\n${viewModel.getRideSummary()}",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Update button state
     */
    private fun updateButtonState(isActive: Boolean) {
        if (isActive) {
            btnStartStop.text = "Terminer la Course"
            btnStartStop.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark, null))
        } else {
            btnStartStop.text = "Démarrer la Course"
            btnStartStop.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark, null))
        }
    }

    /**
     * Start receiving location updates
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!hasLocationPermission()) return

        val locationRequest = LocationRequest.create().apply {
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Location updates started")
        } catch (e: SecurityException) {
            Log.e(TAG, "Error starting location updates", e)
        }
    }

    /**
     * Stop receiving location updates
     */
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(TAG, "Location updates stopped")
    }

    /**
     * Check if location permission is granted
     */
    private fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isRideActive.value == true && hasLocationPermission()) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        // Don't stop if ride is active
        if (viewModel.isRideActive.value != true) {
            stopLocationUpdates()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopLocationUpdates()
    }
}