package com.androidtracker.runningtracker.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.androidtracker.runningtracker.R
import com.androidtracker.runningtracker.extras.Constants
import com.androidtracker.runningtracker.extras.Constants.ACTION_PAUSE_SERVICE
import com.androidtracker.runningtracker.extras.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.androidtracker.runningtracker.extras.Constants.ACTION_START_OR_RESUME_SERVICE
import com.androidtracker.runningtracker.extras.Constants.ACTION_STOP_SERVICE
import com.androidtracker.runningtracker.extras.Constants.FASTEST_LOCATION_INTERVAL
import com.androidtracker.runningtracker.extras.Constants.LOCATION_UPDATE_INTERVAL
import com.androidtracker.runningtracker.extras.Constants.NOTIFICATION_ID
import com.androidtracker.runningtracker.extras.TrackingUtility
import com.androidtracker.runningtracker.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

typealias  Polyline = MutableList<LatLng>
typealias  Polylines = MutableList<Polyline>

class TrackingService : LifecycleService() {

    var isFirstRun = true
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints =
            MutableLiveData<Polylines>() // here one line is the line on our application
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf()) //empty as we don't have any access in the begining
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })//getting the updates whenever tracking state changes
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Started or resumed service")
                        startForegroundService()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stop service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    //function which will track the time and also trigger our observer on time changes
    private var isTimerEnabled = false
    private var lapTime = 0L //lap time
    private var timeRun = 0L //total run time
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer(){

    }



    //function to pause the service
    private fun pauseService() {
        isTracking.postValue(false)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hashLocationPermission(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } else {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            }
        }
    }


    //    here we are getting the updated location callback
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION : ${location.latitude},${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {         //location should not be equal to null
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf()) //adding empty list
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        addEmptyPolyline()
        isTracking.postValue(true)
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
 * to getting the location update's.
 *
 * Here we will creating 2 live data:
 * 1st -> current time run in seconds (in notification we don't want to update in millisecond that's why sec)
 * 2nd -> current time run in milliseconds ( in tracking frag we want accurate time)
 *
 */