package com.androidtracker.runningtracker.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.androidtracker.runningtracker.R
import com.androidtracker.runningtracker.extras.Constants.ACTION_PAUSE_SERVICE
import com.androidtracker.runningtracker.extras.Constants.ACTION_START_OR_RESUME_SERVICE
import com.androidtracker.runningtracker.extras.Constants.MAP_ZOOM
import com.androidtracker.runningtracker.extras.Constants.POLYLINE_COLOR
import com.androidtracker.runningtracker.extras.Constants.POLYLINE_WIDTH
import com.androidtracker.runningtracker.extras.TrackingUtility
import com.androidtracker.runningtracker.services.Polyline
import com.androidtracker.runningtracker.services.TrackingService
import com.androidtracker.runningtracker.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()
    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var curTimeInMillis = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        btnToggleRun.setOnClickListener {
           toggleRun()
        }
        mapView.getMapAsync {
            map = it
            addAllPolyLines() //called only when fragment created
        }
        subscribeToObservers()
    }

    //function to subscribe live dataObject
    private fun subscribeToObservers() {
//        observable data for map
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyLine()
            moveCameraToUser()
        })
//        observable data for the stopwatch
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis,false)
            tvTimer.text = formattedTime
        })
    }

    //used to start to stop toggleRun or service
    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    //updating data from service and reacting from to those changes
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            btnToggleRun.text = "START"
            btnFinishRun.visibility = View.VISIBLE
        } else {
            btnToggleRun.text = "STOP"
            btnFinishRun.visibility = View.GONE
        }
    }

    //used to connect the 2 last polyLines (last point with the secondLast point)
    // but when we rotate the device it won't work
    private fun addLatestPolyLine() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polyLineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polyLineOptions)
        }
    }

    //move the camera to the user position whenever new position in the polyline list or pathpoint list
    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(), //coz this is the last position path we got
                    MAP_ZOOM
                )
            )
        }
    }

    // to prevent all the data after rotation we are creating this function
    private fun addAllPolyLines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

}