package com.androidtracker.runningtracker.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.androidtracker.runningtracker.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog : DialogFragment() {

    private var yesListener:(() -> Unit)? = null

    fun setYesListener(listener:() -> Unit){
        yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel the current run and delete all its data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
               yesListener?.let { yes ->
                   yes
               }
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()

    }
}
/*
fun CheckingPermissionIsEnabledOrNot(): Boolean {
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
 */