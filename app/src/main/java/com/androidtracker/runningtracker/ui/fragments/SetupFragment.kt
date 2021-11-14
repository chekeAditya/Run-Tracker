package com.androidtracker.runningtracker.ui.fragments

import android.Manifest.permission
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.androidtracker.runningtracker.R
import com.androidtracker.runningtracker.extras.Constants.KEY_FIRST_TIME_TOGGLE
import com.androidtracker.runningtracker.extras.Constants.KEY_NAME
import com.androidtracker.runningtracker.extras.Constants.KEY_WEIGHT
import com.androidtracker.runningtracker.extras.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var isFirstAppOpen = true

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFirstAppOpen) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }

        tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPref()
            if (success) {
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            } else {
                Snackbar.make(requireView(), "Please enter all the fields", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestPermission() {
        if (checkingPermissionIsEnabledOrNot()) {
            Toast.makeText(
                context,
                "Permissions Granted",
                Toast.LENGTH_LONG
            ).show()
        } else {
            requestMultiplePermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun checkingPermissionIsEnabledOrNot(): Boolean {
        val FirstPermissionResult =
            ContextCompat.checkSelfPermission(requireContext(), permission.ACCESS_COARSE_LOCATION)
        val SecondPermissionResult = ContextCompat.checkSelfPermission(
            requireContext(),
            permission.ACCESS_FINE_LOCATION
        )
        val ThirdPermissionResult = ContextCompat.checkSelfPermission(
            requireContext(),
            permission.ACCESS_BACKGROUND_LOCATION
        )
        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED && SecondPermissionResult == PackageManager.PERMISSION_GRANTED && ThirdPermissionResult == PackageManager.PERMISSION_GRANTED
    }

    private fun requestMultiplePermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                permission.CAMERA,
                permission.ACCESS_COARSE_LOCATION,
                permission.READ_EXTERNAL_STORAGE
            ), REQUEST_CODE_LOCATION_PERMISSION
        )
    }

    private fun writePersonalDataToSharedPref(): Boolean {
        val name = etName.text.toString()
        val weight = etWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()
        val toolbarText = "Let's go, $name!"
        requireActivity().tvToolbarTitle.text = toolbarText
        return true
    }

}