package com.example.taxic.data

/**
 * ===========================================
 * DRIVER DATABASE
 * ===========================================
 *
 * This holds all our hardcoded drivers/users.
 * In a real app, this would come from a database or API.
 *
 * WHY USE "object"?
 * - It's a Singleton (only one instance exists)
 * - We can access it anywhere: DriverDatabase.validateLogin(...)
 *
 * WHAT IT DOES:
 * 1. Stores a list of all drivers
 * 2. Validates login credentials
 * 3. Returns the matching driver if login is successful
 */
object DriverDatabase {

    /**
     * List of all hardcoded drivers
     *
     * Each driver has:
     * - username: for login
     * - password: for login
     * - All other driver information
     */
    private val allDrivers = listOf(
        Driver(
            username = "mohalami",
            password = "pass123",
            firstName = "Mohammed",
            lastName = "Alami",
            age = 35,
            licenseType = "Professional Driver License",
            phoneNumber = "+212 611 223 344",
            carModel = "Toyota Corolla",
            carPlate = "A-12345",
            rating = 4.8f
        ),
        Driver(
            username = "ahmed",
            password = "pass456",
            firstName = "Ahmed",
            lastName = "Bennani",
            age = 28,
            licenseType = "Professional Driver License",
            phoneNumber = "+212 622 334 455",
            carModel = "Dacia Logan",
            carPlate = "B-67890",
            rating = 4.5f
        ),
        Driver(
            username = "fatima",
            password = "pass789",
            firstName = "Fatima",
            lastName = "Zahra",
            age = 32,
            licenseType = "Professional Driver License",
            phoneNumber = "+212 633 445 566",
            carModel = "Renault Clio",
            carPlate = "C-11223",
            rating = 4.9f
        ),
        Driver(
            username = "youssef",
            password = "pass000",
            firstName = "Youssef",
            lastName = "Idrissi",
            age = 40,
            licenseType = "Professional Driver License",
            phoneNumber = "+212 644 556 677",
            carModel = "Peugeot 208",
            carPlate = "D-33445",
            rating = 4.7f
        )
    )

    /**
     * Validate login credentials
     *
     * HOW IT WORKS:
     * 1. Takes username and password from login screen
     * 2. Searches through all drivers
     * 3. Finds one where username AND password match
     * 4. Returns the driver if found, or null if not found
     *
     * @param username The username entered by user
     * @param password The password entered by user
     * @return Driver object if login successful, null if failed
     */
    fun validateLogin(username: String, password: String): Driver? {
        // Search through all drivers
        // find() returns the first driver that matches, or null if none match
        return allDrivers.find { driver ->
            // Check if both username and password match
            driver.username == username && driver.password == password
        }
    }

    /**
     * Get a driver by username only
     *
     * Useful if you already know the username is valid
     *
     * @param username The username to search for
     * @return Driver object if found, null if not found
     */
    fun getDriverByUsername(username: String): Driver? {
        return allDrivers.find { driver ->
            driver.username == username
        }
    }

    /**
     * Get all drivers (for testing/debugging)
     *
     * @return List of all drivers
     */
    fun getAllDrivers(): List<Driver> {
        return allDrivers
    }
}