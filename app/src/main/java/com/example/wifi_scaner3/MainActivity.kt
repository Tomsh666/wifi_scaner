@file:Suppress("DEPRECATION")

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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView


class MainActivity : AppCompatActivity() {

    private val permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var wifiManager: WifiManager

    private var currentPermission: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val button: Button = findViewById(R.id.scan_button)
        val filter = IntentFilter(SCAN_RESULTS_AVAILABLE_ACTION)
        requestPermissions()
        registerReceiver(wifiScanReceiver,filter)
        startWifiScan()
        button.setOnClickListener{
            handleScanResults()
        }
    }

    private fun startWifiScan() {
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }
        wifiManager.startScan()

    }


    private fun handleScanResults() {
        val wifiListView : ListView = findViewById(R.id.table)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            val scanResults: List<ScanResult> = wifiManager.scanResults
            Log.d("Tag", "$scanResults")
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, scanResults.map { it.SSID })
            wifiListView.adapter = adapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiScanReceiver)
    }

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("Tag","$currentPermission granted")
            } else {
                if (!shouldShowRequestPermissionRationale(currentPermission)) {
                    Log.d("Tag","$currentPermission granted")
                    showPermissionRequiredDialog()
                }
            }
        }

    private fun requestPermissions() {
        for (permission in permissionsToRequest) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                currentPermission = permission
                requestPermissionLauncher.launch(permission)
            }
            Log.d("Tag","$permission Permissions granted")
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
}