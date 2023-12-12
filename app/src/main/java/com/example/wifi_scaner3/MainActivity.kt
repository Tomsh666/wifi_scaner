@file:Suppress("DEPRECATION")

package com.example.wifi_scaner3

import android.Manifest
import android.annotation.SuppressLint
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
import android.widget.TextView


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
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }
        button.setOnClickListener{
            wifiManager.startScan()
            handleScanResults()
        }
    }

    private fun handleScanResults() {
        val wifiListView : ListView = findViewById(R.id.table)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            val scanResults: List<ScanResult> = wifiManager.scanResults
            Log.d("Tag", "$scanResults")
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, scanResults.map { it.SSID })
            wifiListView.adapter = adapter
            wifiListView.setOnItemClickListener { _, _, position, _ ->
                val selectedNetwork: ScanResult = wifiManager.scanResults[position]
                showNetworkDetails(selectedNetwork)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showNetworkDetails(selectedNetwork: ScanResult) {
        val dialogView = layoutInflater.inflate(R.layout.wifi_details_activity, null)

        val textSSID: TextView = dialogView.findViewById(R.id.textSSID)
        textSSID.text = "SSID: ${selectedNetwork.SSID}"

        val textBSSID: TextView = dialogView.findViewById(R.id.textBSSID)
        textBSSID.text = "BSSID: ${selectedNetwork.BSSID}"

        val signalStrengthText = when {
            selectedNetwork.level > -50 -> "Excellent"
            selectedNetwork.level in -60..-50 -> "Good"
            selectedNetwork.level in -70..-60 -> "Fair"
            selectedNetwork.level < -70 -> "Weak"
            else -> "No signal"
        }
        val textSignalStrength: TextView = dialogView.findViewById(R.id.textSignalStrength)
        textSignalStrength.text = "Signal Strength: $signalStrengthText"


        val textFrequency: TextView = dialogView.findViewById(R.id.Freqency)
        textFrequency.text = "Frequency: ${selectedNetwork.frequency} MHz"

        val textCapability: TextView = dialogView.findViewById(R.id.Capability)
        textCapability.text = "Capability: ${selectedNetwork.capabilities}"

        val alertDialogBuilder = AlertDialog.Builder(this)
            .setTitle("Network Details")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

        alertDialogBuilder.show()
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