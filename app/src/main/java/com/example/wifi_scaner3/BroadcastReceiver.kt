package com.example.wifi_scaner3

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.content.ContextCompat

class MyBroadcastReceiver(private val onScanResultsAvailable: (List<ScanResult>) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val wifiManager: WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        StringBuilder().apply {
            append("Action: ${intent.action}\n")
            append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
            toString().also { log ->
                Log.d(ContentValues.TAG, log)
            }
        }
        if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
            Log.d("Tag", "Received SCAN_RESULTS_AVAILABLE_ACTION")
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            ) {
                val scanResults: List<ScanResult> = wifiManager.scanResults
                onScanResultsAvailable.invoke(scanResults)
                Log.d("Tag", "$scanResults")
            }
        }
    }
}
