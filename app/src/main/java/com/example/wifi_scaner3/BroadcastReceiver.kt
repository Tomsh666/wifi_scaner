package com.example.wifi_scaner3

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.util.Log

val wifiScanReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
            Log.d("Tag", "Received SCAN_RESULTS_AVAILABLE_ACTION")
        }
        StringBuilder().apply {
            append("Action: ${intent.action}\n")
            append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
            toString().also { log ->
                Log.d(ContentValues.TAG, log)
            }
        }
    }

}