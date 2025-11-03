package com.example.taxic.ui

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taxic.R
import com.example.taxic.viewmodel.TaxiMeterViewModel
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

/**
 * Main Activity - Entry point of the application
 * Handles permission requests and hosts fragments
 */
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    companion object {
        const val PERMISSION_REQUEST_CODE = 1
    }

    // ViewModel instance - survives configuration changes
    private val viewModel: TaxiMeterViewModel by viewModels()
    private var savedInstace: Bundle? = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        savedInstace = savedInstanceState

        // Setup edge-to-edge display
        setupEdgeToEdge()

        // Check permissions and setup UI
        if (hasLocationPermission()) {
            setupFragments()
        } else {
            requestLocationPermissions()
        }
    }

    /**
     * Setup edge-to-edge display for modern Android UI
     */
    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Setup the main fragments (only called once permissions are granted)
     */
    private fun setupFragments() {
        // Only add fragments if this is the first creation (not a configuration change)
        if (savedInstace == null) {
            val taxiMeterFragment = TaxiMeterFragment()

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, taxiMeterFragment)
                .commit()
        }
    }

    /**
     * Check if location permissions are granted
     */
    private fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    /**
     * Request location permissions
     */
    private fun requestLocationPermissions() {
        EasyPermissions.requestPermissions(
            this,
            getString(R.string.permission_rationale), // Add this string to strings.xml
            PERMISSION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    /**
     * Called when permissions are granted
     */
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(this, "Permissions accordées", Toast.LENGTH_SHORT).show()
        setupFragments()
    }

    /**
     * Called when permissions are denied
     */
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        // Check if user permanently denied permissions
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            // Show dialog to go to app settings
            AppSettingsDialog.Builder(this)
                .setTitle("Permissions requises")
                .setRationale("Cette application nécessite l'accès à votre localisation pour fonctionner. Veuillez activer les permissions dans les paramètres.")
                .setPositiveButton("Paramètres")
                .setNegativeButton("Annuler")
                .build()
                .show()
        } else {
            // User denied but didn't select "Don't ask again"
            Toast.makeText(
                this,
                "Les permissions de localisation sont nécessaires pour utiliser cette application",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Handle permission request results
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}