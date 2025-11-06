package com.example.taxic.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.taxic.R
import com.example.taxic.data.Driver
import com.example.taxic.util.QRCodeHelper
import com.example.taxic.viewmodel.TaxiMeterViewModel

/**
 * ===========================================
 * DRIVER PROFILE FRAGMENT
 * ===========================================
 *
 * This screen shows:
 * 1. All driver information (name, age, license, etc.)
 * 2. QR code containing all driver info
 *
 * WHAT IT DOES:
 * - Gets driver data from ViewModel
 * - Displays all information
 * - Generates QR code from driver info
 * - Shows QR code image
 */
class DriverProfileFragment : Fragment() {

    // ===========================================
    // CONSTANTS
    // ===========================================

    companion object {
        private const val TAG = "DriverProfileFragment"
    }

    // ===========================================
    // VARIABLES
    // ===========================================

    // Shared ViewModel (same one used in MainActivity and TaxiMeterFragment)
    private val taxiViewModel: TaxiMeterViewModel by activityViewModels()

    // UI Elements
    private lateinit var textViewName: TextView
    private lateinit var textViewAge: TextView
    private lateinit var textViewLicense: TextView
    private lateinit var textViewPhone: TextView
    private lateinit var textViewCarModel: TextView
    private lateinit var textViewCarPlate: TextView
    private lateinit var textViewRating: TextView
    private lateinit var imageViewQRCode: ImageView
    private lateinit var buttonBack: Button

    // Layouts (for showing/hiding optional fields)
    private lateinit var layoutCarModel: LinearLayout
    private lateinit var layoutCarPlate: LinearLayout

    // ===========================================
    // LIFECYCLE
    // ===========================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "Creating view")
        return inflater.inflate(R.layout.fragment_driver_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "View created, setting up...")

        try {
            // Step 1: Find all views
            findAllViews(view)

            // Step 2: Get driver from ViewModel
            val driver = taxiViewModel.driverProfile.value

            if (driver != null) {
                // Step 3: Display driver information
                displayDriverInfo(driver)

                // Step 4: Generate and show QR code
                generateAndShowQRCode(driver)
            } else {
                Log.e(TAG, "No driver data available")
                Toast.makeText(context,
                    getString(R.string.no_driver_data_available),
                    Toast.LENGTH_SHORT).show()
            }

            // Step 5: Setup back button
            setupBackButton()

            Log.d(TAG, "Setup complete")

        } catch (error: Exception) {
            Log.e(TAG, "Error in onViewCreated", error)
            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ===========================================
    // SETUP METHODS
    // ===========================================

    /**
     * Find all UI elements
     */
    private fun findAllViews(view: View) {
        Log.d(TAG, "Finding all views...")

        textViewName = view.findViewById(R.id.tvDriverName)
        textViewAge = view.findViewById(R.id.tvDriverAge)
        textViewLicense = view.findViewById(R.id.tvDriverLicense)
        textViewPhone = view.findViewById(R.id.tvDriverPhone)
        textViewCarModel = view.findViewById(R.id.tvDriverCarModel)
        textViewCarPlate = view.findViewById(R.id.tvDriverCarPlate)
        textViewRating = view.findViewById(R.id.tvDriverRating)
        imageViewQRCode = view.findViewById(R.id.ivQRCode)
        buttonBack = view.findViewById(R.id.btnBack)

        // Find layouts (for showing/hiding)
        layoutCarModel = view.findViewById(R.id.layoutCarModel)
        layoutCarPlate = view.findViewById(R.id.layoutCarPlate)

        Log.d(TAG, "All views found")
    }

    /**
     * Display all driver information
     *
     * @param driver The driver to display
     */
    private fun displayDriverInfo(driver: Driver) {
        Log.d(TAG, "Displaying driver info for: ${driver.getFullName()}")

        // Display basic info
        textViewName.text = driver.getFullName()
        textViewAge.text = driver.age.toString()
        textViewLicense.text = driver.licenseType
        textViewPhone.text = driver.phoneNumber
        textViewRating.text = "${driver.rating} ⭐"

        // Display optional info (or hide if not available)
        if (driver.carModel != null) {
            textViewCarModel.text = driver.carModel
            layoutCarModel.visibility = View.VISIBLE
        } else {
            layoutCarModel.visibility = View.GONE
        }

        if (driver.carPlate != null) {
            textViewCarPlate.text = driver.carPlate
            layoutCarPlate.visibility = View.VISIBLE
        } else {
            layoutCarPlate.visibility = View.GONE
        }
    }

    /**
     * Generate QR code and display it
     *
     * STEPS:
     * 1. Get driver info as text string
     * 2. Generate QR code bitmap from text
     * 3. Display bitmap in ImageView
     *
     * @param driver The driver whose info to encode
     */
    private fun generateAndShowQRCode(driver: Driver) {
        Log.d(TAG, "Generating QR code...")

        try {
            // Step 1: Get driver info as text
            // This will be encoded into the QR code
            val driverInfoText = driver.getDriverInfoText()

            Log.d(TAG, "QR Code will contain:\n$driverInfoText")

            // Step 2: Generate QR code bitmap
            // Size = 512x512 pixels
            val qrCodeBitmap = QRCodeHelper.generateQRCode(driverInfoText, 512)

            // Step 3: Display in ImageView
            imageViewQRCode.setImageBitmap(qrCodeBitmap)

            Log.d(TAG, "QR code generated and displayed")

        } catch (error: Exception) {
            Log.e(TAG, "Error generating QR code", error)
            Toast.makeText(context, getString(R.string.error_generating_qr_code), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Setup back button click listener
     */
    private fun setupBackButton() {
        buttonBack.setOnClickListener {
            Log.d(TAG, "Back button clicked")

            // Go back to previous screen (TaxiMeterFragment)
            // popBackStack() removes this fragment and shows the previous one
            parentFragmentManager.popBackStack()
        }
    }
}