package com.example.taxic.ui

import android.Manifest
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.example.taxic.R
import com.example.taxic.data.DriverDatabase
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

    // The logged-in driver's username (passed from LoginActivity)
    private var loggedInUsername: String? = null


    // ===========================================
    // LIFECYCLE METHODS
    // ===========================================

    /**
     * onCreate() is called when the Activity is created
     * This is where we set up everything
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Let your app draw behind the system bars (status & navigation)
        // Make app draw behind system bars (removes white bar)
        WindowCompat.setDecorFitsSystemWindows(window, false)




        hideSystemBarsIfLandscape()

        Log.d(TAG, "MainActivity created")

        // Get the username from LoginActivity
        loggedInUsername = intent.getStringExtra(LoginActivity.EXTRA_USERNAME)

        Log.d(TAG, "Logged in as: $loggedInUsername")

        // Load driver data into ViewModel
        loadDriverData()

        window.statusBarColor = android.graphics.Color.TRANSPARENT

        // Set the layout from XML file
        setContentView(R.layout.activity_main)

        // Adjust top padding to avoid overlapping status bar
        //val rootView = findViewById<View>(R.id.main) // your root layout
        //ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
        //    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        //    view.updatePadding(top = systemBars.top)
        //    insets
        //}




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

    /**
     * Load driver data from database into ViewModel
     *
     * This gets the logged-in driver's information
     * and saves it in the ViewModel so all screens can access it
     */
    private fun loadDriverData() {
        if (loggedInUsername != null) {
            val driver = DriverDatabase.getDriverByUsername(loggedInUsername!!)

            if (driver != null) {
                // Set driver in ViewModel
                taxiViewModel.setDriver(driver)
                Log.d(TAG, "Driver loaded: ${driver.getFullName()}")
            } else {
                Log.e(TAG, "Driver not found for username: $loggedInUsername")
            }
        } else {
            Log.e(TAG, "No username provided from LoginActivity")
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
     * Ask user for location permission AND notification permission
     *
     * This shows a system dialog asking the user to allow:
     * - Location access (for GPS tracking)
     * - Notifications (for ride completion alerts)
     */
    private fun askForLocationPermission() {
        // List of permissions we need
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Add notification permission for Android 13+ (API 33+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Request all permissions at once
        EasyPermissions.requestPermissions(
            this,
            getString(R.string.rationale),
            PERMISSION_REQUEST_CODE,
            *permissions.toTypedArray()  // Convert list to array
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
        Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()

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
                .setTitle(getString(R.string.permission_required))
                .setRationale(getString(R.string.rationale2))
                .setPositiveButton(getString(R.string.go_to_settings))
                .setNegativeButton(getString(R.string.cancel))
                .build()
                .show()
        } else {
            // User just denied, explain why we need it
            Toast.makeText(
                this,
                getString(R.string.location_permission_is_needed_to_track_the_ride),
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        hideSystemBarsIfLandscape()
    }

    private fun hideSystemBarsIfLandscape() {
        // Get current orientation
        val orientation = resources.configuration.orientation

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // LANDSCAPE - Hide bars
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController?.hide(WindowInsetsCompat.Type.statusBars())
            windowInsetsController?.hide(WindowInsetsCompat.Type.navigationBars())
            // Must be set
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            // PORTRAIT - Show bars
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController?.show(WindowInsetsCompat.Type.statusBars())
            windowInsetsController?.show(WindowInsetsCompat.Type.navigationBars())
        }
    }
}

