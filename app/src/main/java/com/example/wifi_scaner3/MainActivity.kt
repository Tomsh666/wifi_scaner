package com.example.wifi_scaner3

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
import android.util.Log
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var wifiManager: WifiManager
    private val wifiScanReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                handleScanResults()
            }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val button: Button = findViewById(R.id.scan_button)
        val br: BroadcastReceiver = broadcastReceiver()
        val filter = IntentFilter(SCAN_RESULTS_AVAILABLE_ACTION)
        val listenToBroadcastsFromOtherApps = false
        val receiverFlags = if (listenToBroadcastsFromOtherApps) {
            ContextCompat.RECEIVER_EXPORTED
        } else {
            ContextCompat.RECEIVER_NOT_EXPORTED
        }
        registerReceiver(br, filter, receiverFlags)
        registerReceiver(wifiScanReceiver,filter)
        Log.d("Tag", "Receiver registered successfully")
        button.setOnClickListener{
            requestPermissions()
            startWifiScan()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiScanReceiver)
    }

    private fun startWifiScan() {
        if (wifiManager.isWifiEnabled) {
            // Запуск сканирования Wi-Fi
            wifiManager.startScan()
        } else {
            // Если Wi-Fi выключен, вы можете попытаться включить его
            wifiManager.isWifiEnabled = true
        }
    }

    private fun handleScanResults() {
        // Получение результатов сканирования
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val scanResults: List<ScanResult> = wifiManager.scanResults
            Log.d("Tag","$scanResults")
        } else {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        }
    }

    private fun requestPermissions() {
        for (permission in permissionsToRequest) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            } else {
                currentPermission = permission
                requestPermissionLauncher.launch(permission)
            }
            showToast("$permission Permissions granted")
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