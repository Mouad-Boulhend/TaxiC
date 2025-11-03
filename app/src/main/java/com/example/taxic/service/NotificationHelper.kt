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
 * Helper class to manage notifications
 * Handles ride completion notifications
 */
object NotificationHelper {

    private const val CHANNEL_ID = "taxi_meter_channel"
    private const val CHANNEL_NAME = "Taxi Meter Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for taxi ride completion"
    private const val NOTIFICATION_ID = 1001

    /**
     * Create notification channel (required for Android 8.0+)
     */
    private fun createNotificationChannel(context: Context) {
        // Only create channel on Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Send notification when ride is completed
     * @param context Application context
     * @param rideSummary Summary of the completed ride
     */
    fun sendRideCompletedNotification(context: Context, rideSummary: String) {
        // Create notification channel
        createNotificationChannel(context)

        // Create intent to open app when notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_taxi) // Make sure to add this icon
            .setContentTitle("Course Terminée")
            .setContentText("Consultez les détails de votre course")
            .setStyle(NotificationCompat.BigTextStyle().bigText(rideSummary))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss notification when clicked
            .setVibrate(longArrayOf(0, 500, 200, 500)) // Vibration pattern
            .setColor(context.getColor(R.color.taxi_yellow))
            .build()

        // Show the notification
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle case where notification permission is denied (Android 13+)
            e.printStackTrace()
        }
    }

    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            true // No permission needed for older versions
        }
    }
}