package com.androidtracker.runningtracker.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.androidtracker.runningtracker.R
import com.androidtracker.runningtracker.extras.Constants
import com.androidtracker.runningtracker.extras.Constants.ACTION_PAUSE_SERVICE
import com.androidtracker.runningtracker.extras.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.androidtracker.runningtracker.extras.Constants.ACTION_START_OR_RESUME_SERVICE
import com.androidtracker.runningtracker.extras.Constants.ACTION_STOP_SERVICE
import com.androidtracker.runningtracker.extras.Constants.NOTIFICATION_ID
import com.androidtracker.runningtracker.ui.MainActivity
import timber.log.Timber


class TrackingService : LifecycleService() {


    var isFirstRun = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Started or resumed service")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stop service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        )
            .setAutoCancel(false) //if user click on the notification the will that be disappear (no) -> false
            .setOngoing(true) // notification can't be swiped away
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp) //icon
            .setContentTitle("Running App")//notification title of application
            .setContentText("00:00:00") // content inside the notification
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this, 0, Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT //whenever we launch the pendingIntent and if it already exist then it will update it instead of recreating it.
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW //if it's high then notification will always come with sound
        )
        notificationManager.createNotificationChannel(channel)
    }

}
/**
 * Here we will simply be using Intent for service to activity communication.
 * onStartCommand-> it will call whenever we send the command to the service.
 */