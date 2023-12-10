package com.example.wifi_scaner3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button: Button = findViewById(R.id.scan_button)
        button.setOnClickListener(){
            showToast( "Button clicked")
            requestPermissions()
            showToast("Permissions requested")
        }
    }

    private val permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showToast("$currentPermission granted")
            } else {
                if (!shouldShowRequestPermissionRationale(currentPermission)) {
                    showToast("$currentPermission granted")
                    showPermissionRequiredDialog()
                }
            }
        }


    val PERMISSION_REQUEST_CODE=123
    private var currentPermission: String = ""


    private fun requestPermissions() {
        for (permission in permissionsToRequest) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            } else {
                currentPermission = permission
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                            showToast("Granted")
                } else {
                    showToast("Denied")
                }
                return
            }
            else -> {
            }
        }
    }

    private fun showPermissionRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Grant access to perform the task")
            .setPositiveButton("Settings") { _, _ -> openAppSettings() }
            .setCancelable(false)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}