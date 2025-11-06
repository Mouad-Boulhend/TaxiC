package com.example.taxic.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taxic.R
import com.example.taxic.data.DriverDatabase
import com.example.taxic.viewmodel.TaxiMeterViewModel

/**
 * ===========================================
 * LOGIN ACTIVITY
 * ===========================================
 *
 * This is the first screen users see.
 * They must login before accessing the taxi meter.
 *
 * WHAT IT DOES:
 * 1. Shows username and password fields
 * 2. Validates credentials when login button is clicked
 * 3. If correct: Opens MainActivity
 * 4. If wrong: Shows error message
 */
class LoginActivity : AppCompatActivity() {

    // ===========================================
    // CONSTANTS
    // ===========================================

    companion object {
        private const val TAG = "LoginActivity"

        // Key for passing username to MainActivity
        const val EXTRA_USERNAME = "username"
    }

    // ===========================================
    // UI ELEMENTS
    // ===========================================

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button

    // ===========================================
    // LIFECYCLE
    // ===========================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "LoginActivity created")

        // Set the layout
        setContentView(R.layout.activity_login)

        // Find all views
        findViews()

        // Setup button click
        setupLoginButton()
    }

    // ===========================================
    // SETUP METHODS
    // ===========================================

    /**
     * Find all UI elements by their IDs
     */
    private fun findViews() {
        Log.d(TAG, "Finding views...")

        editTextUsername = findViewById(R.id.etUsername)
        editTextPassword = findViewById(R.id.etPassword)
        buttonLogin = findViewById(R.id.btnLogin)

        Log.d(TAG, "All views found")
    }

    /**
     * Setup login button click listener
     */
    private fun setupLoginButton() {
        buttonLogin.setOnClickListener {
            Log.d(TAG, "Login button clicked")
            handleLogin()
        }
    }

    // ===========================================
    // LOGIN LOGIC
    // ===========================================

    /**
     * Handle login button click
     *
     * STEPS:
     * 1. Get username and password from EditTexts
     * 2. Check if fields are empty
     * 3. Validate credentials with database
     * 4. Navigate to MainActivity if successful
     * 5. Show error if failed
     */
    private fun handleLogin() {
        // Step 1: Get text from input fields
        val username = editTextUsername.text.toString()
        val password = editTextPassword.text.toString()

        Log.d(TAG, "Attempting login for username: $username")

        // Step 2: Check if fields are empty
        if (username.isEmpty()) {
            showError("Please enter username")
            return
        }

        if (password.isEmpty()) {
            showError("Please enter password")
            return
        }

        // Step 3: Validate with database
        val driver = DriverDatabase.validateLogin(username, password)

        if (driver != null) {
            // Login successful!
            Log.d(TAG, "Login successful for: ${driver.getFullName()}")

            // Show success message
            Toast.makeText(
                this,
                getString(R.string.welcome) + ", ${driver.getFullName()}!",
                Toast.LENGTH_SHORT
            ).show()

            // Navigate to MainActivity
            navigateToMainActivity(username)


        } else {
            // Login failed
            Log.d(TAG, "Login failed - invalid credentials")
            showError(getString(R.string.invalid_username_or_password))
        }
    }

    /**
     * Show error message to user
     *
     * @param message The error message to display
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Navigate to MainActivity
     *
     * What happens:
     * 1. Create Intent (like a message to open MainActivity)
     * 2. Put username in the Intent (so MainActivity knows who logged in)
     * 3. Start MainActivity
     * 4. Close LoginActivity (user can't go back to login)
     *
     * @param username The username of logged-in driver
     */
    private fun navigateToMainActivity(username: String) {
        Log.d(TAG, "Navigating to MainActivity...")

        // Create Intent to open MainActivity
        val intent = Intent(this, MainActivity::class.java)

        // Add username to Intent
        // MainActivity will use this to load the correct driver
        intent.putExtra(EXTRA_USERNAME, username)

        // Start MainActivity
        startActivity(intent)

        // Close LoginActivity
        // User won't be able to press back button to return to login
        finish()

        Log.d(TAG, "Navigation complete")
    }
}