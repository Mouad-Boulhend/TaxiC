package com.example.taxic.ui

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.taxic.R
import com.example.taxic.viewmodel.TaxiMeterViewModel
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

/**
 * ===========================================
 * MAIN ACTIVITY
 * ===========================================
 *
 * This is the starting point of our app.
 * When you launch the app, this Activity opens first.
 *
 * WHAT IT DOES:
 * 1. Checks if we have permission to access location
 * 2. If yes: Shows the main screen (TaxiMeterFragment)
 * 3. If no: Asks user for permission
 *
 * PERMISSIONS NEEDED:
 * - ACCESS_FINE_LOCATION: Get precise GPS location
 * - ACCESS_COARSE_LOCATION: Get approximate location
 */
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    // ===========================================
    // CONSTANTS
    // ===========================================

    companion object {
        private const val TAG = "MainActivity"  // For logging
        private const val PERMISSION_REQUEST_CODE = 123  // Any unique number
    }


    // ===========================================
    // VARIABLES
    // ===========================================

    // The ViewModel that holds our data
    // "by viewModels()" creates it automatically
    private val taxiViewModel: TaxiMeterViewModel by viewModels()


    // ===========================================
    // LIFECYCLE METHODS
    // ===========================================

    /**
     * onCreate() is called when the Activity is created
     * This is where we set up everything
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "MainActivity created")

        // Set the layout from XML file
        setContentView(R.layout.activity_main)

        // Check if we have location permission
        if (hasLocationPermission()) {
            // We have permission! Show the main screen
            Log.d(TAG, "Permission already granted")
            showMainScreen()
        } else {
            // We don't have permission. Ask for it.
            Log.d(TAG, "Requesting permission")
            askForLocationPermission()
        }
    }


    // ===========================================
    // PERMISSION CHECKING
    // ===========================================

    /**
     * Check if we have location permission
     *
     * @return true if we have permission, false if we don't
     */
    private fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    /**
     * Ask user for location permission
     *
     * This shows a system dialog asking the user to allow location access
     */
    private fun askForLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This app needs your location to track the taxi ride",
            PERMISSION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }


    // ===========================================
    // PERMISSION CALLBACKS
    // ===========================================

    /**
     * Called when user GRANTS permission
     *
     * @param requestCode The code we used when asking
     * @param perms List of permissions that were granted
     */
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "Permissions granted: $perms")

        // Show success message
        Toast.makeText(this, "Permission granted! Starting app...", Toast.LENGTH_SHORT).show()

        // Show the main screen
        showMainScreen()
    }

    /**
     * Called when user DENIES permission
     *
     * @param requestCode The code we used when asking
     * @param perms List of permissions that were denied
     */
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "Permissions denied: $perms")

        // Check if user clicked "Don't ask again"
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            // User permanently denied - show dialog to go to settings
            AppSettingsDialog.Builder(this)
                .setTitle("Permission Required")
                .setRationale("This app needs location permission to work. Please enable it in settings.")
                .setPositiveButton("Go to Settings")
                .setNegativeButton("Cancel")
                .build()
                .show()
        } else {
            // User just denied, explain why we need it
            Toast.makeText(
                this,
                "Location permission is needed to track the ride",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Called by system after permission dialog is shown
     * We pass the result to EasyPermissions library
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Let EasyPermissions handle the result
        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            this
        )
    }


    // ===========================================
    // UI SETUP
    // ===========================================

    /**
     * Show the main taxi meter screen
     *
     * This loads the TaxiMeterFragment into our container
     *
     * How do we avoid duplicate fragments?
     * - We check if a fragment already exists in the container
     * - If it exists, don't create a new one
     * - This prevents duplicates when rotating the phone
     */
    private fun showMainScreen() {
        // Check if fragment already exists
        val existingFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)

        // Only create fragment if it doesn't exist yet
        if (existingFragment == null) {
            Log.d(TAG, "Creating TaxiMeterFragment")

            // Create the fragment
            val taxiFragment = TaxiMeterFragment()

            // Add it to our container
            // R.id.fragmentContainer is the ID from activity_main.xml
            supportFragmentManager
                .beginTransaction()  // Start a transaction
                .replace(R.id.fragmentContainer, taxiFragment)  // Put fragment in container
                .commit()  // Execute the transaction

            Log.d(TAG, "Fragment added successfully")
        } else {
            Log.d(TAG, "Fragment already exists, not creating duplicate")
        }
    }
}