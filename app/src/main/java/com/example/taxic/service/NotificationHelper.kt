package com.example.taxic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.taxic.R
import com.example.taxic.ui.MainActivity

/**
 * ===========================================
 * NOTIFICATION HELPER
 * ===========================================
 *
 * This class helps us send notifications to the user.
 *
 * WHAT ARE NOTIFICATIONS?
 * - Those alerts that appear at the top of your phone
 * - Example: "Ride completed! Total: 25.50 DH"
 *
 * WHEN DO WE USE IT?
 * - When the taxi ride ends
 * - To show the final fare
 *
 * WHY USE "object"?
 * - Makes this a Singleton (only one instance exists)
 * - We can call methods directly: NotificationHelper.sendNotification()
 */
object NotificationHelper {

    // ===========================================
    // CONSTANTS
    // ===========================================

    // Channel ID - unique identifier for our notification channel
    private const val CHANNEL_ID = "taxi_meter_channel"

    // Channel name - shown in phone settings
    private const val CHANNEL_NAME = "Taxi Ride Notifications"

    // Channel description - explains what notifications are for
    private const val CHANNEL_DESCRIPTION = "Notifications when taxi ride ends"

    // Notification ID - unique number for this notification
    private const val NOTIFICATION_ID = 1001


    // ===========================================
    // PUBLIC METHODS
    // ===========================================

    /**
     * Send a notification when ride is completed
     *
     * @param context Application context (needed to access system services)
     * @param rideSummary Text to show in notification (distance, time, fare)
     */
    fun sendRideCompletedNotification(context: Context, rideSummary: String) {
        // Step 1: Create notification channel (required for Android 8.0+)
        createNotificationChannelIfNeeded(context)

        // Step 2: Create the notification
        val notification = buildNotification(context, rideSummary)

        // Step 3: Show the notification
        showNotification(context, notification)
    }


    // ===========================================
    // PRIVATE METHODS
    // ===========================================

    /**
     * Create notification channel
     *
     * Android 8.0+ requires channels to organize notifications
     * Users can turn channels on/off in phone settings
     */
    private fun createNotificationChannelIfNeeded(context: Context) {
        // Only needed on Android 8.0 (API 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Set importance level
            // IMPORTANCE_HIGH = makes sound and pops up
            val importance = NotificationManager.IMPORTANCE_HIGH

            // Create the channel
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)  // Vibrate when notification appears
                enableLights(true)     // Flash LED if phone has one
            }

            // Register the channel with the system
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Build the notification
     *
     * Creates the notification with title, text, icon, etc.
     *
     * @param context Application context
     * @param rideSummary Text to show
     * @return Built notification object
     */
    private fun buildNotification(context: Context, rideSummary: String): android.app.Notification {
        // Create an Intent to open MainActivity when notification is tapped
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            // Clear the back stack and create new task
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Wrap Intent in PendingIntent (required for notifications)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)  // Icon (use your own icon in production)
            .setContentTitle("Ride Completed")  // Title shown in bold
            .setContentText("Tap to see details")  // Short text
            .setStyle(
                // BigTextStyle allows showing more text
                NotificationCompat.BigTextStyle().bigText(rideSummary)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // High priority
            .setContentIntent(pendingIntent)  // What happens when tapped
            .setAutoCancel(true)  // Dismiss when tapped
            .setVibrate(longArrayOf(0, 500, 200, 500))  // Vibration pattern
            .setColor(context.getColor(android.R.color.holo_orange_light))  // Accent color
            .build()
    }

    /**
     * Show the notification
     *
     * @param context Application context
     * @param notification The built notification
     */
    private fun showNotification(context: Context, notification: android.app.Notification) {
        try {
            // Get notification manager
            val notificationManager = NotificationManagerCompat.from(context)

            // Show the notification
            notificationManager.notify(NOTIFICATION_ID, notification)

        } catch (error: SecurityException) {
            // On Android 13+, user might deny notification permission
            error.printStackTrace()
        }
    }
}