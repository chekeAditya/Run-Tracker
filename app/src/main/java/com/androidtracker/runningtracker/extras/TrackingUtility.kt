package com.androidtracker.runningtracker.extras

import android.Manifest
import android.content.Context
import android.os.Build
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object TrackingUtility {

    fun hashLocationPermission(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            )
        }

    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        var milliseconds = ms
        //milli -> hours
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours) // we need the hours variable to display thats why we are doing this conversion

        //millis -> minutes
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)

        //millis -> seconds
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        if (!includeMillis) {
            return "${if (hours < 10) "0" else ""} $hours:" +
                    "${if (minutes < 10) "0" else ""} $minutes" +
                    "${if (seconds < 10) "0" else ""} $seconds :"
        }

        milliseconds -= TimeUnit.SECONDS.toSeconds(seconds)
        milliseconds /= 10 //coz we need only 2 digits for millis not 3 digit
            return "${if (hours < 10) "0" else ""} $hours:" +
                    "${if (minutes < 10) "0" else ""} $minutes" +
                    "${if (seconds < 10) "0" else ""} $seconds :" +
                    "${if (milliseconds < 10) "0" else ""} $milliseconds"


    }
}