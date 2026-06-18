package com.example.spamblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import com.example.spamblocker.data.BlockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallReceiver : BroadcastReceiver() {

    private val TAG = "CallReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.intent.action.PHONE_STATE") return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        if (state != TelephonyManager.EXTRA_STATE_RINGING) return

        val appContext = context.applicationContext

        val numberFromIntent = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        Log.d(TAG, "Number from intent: $numberFromIntent")

        if (numberFromIntent != null) {
            handleNumber(appContext, numberFromIntent)
        }
    }

    private fun handleNumber(context: Context, number: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val repository = BlockRepository(context)

            // 1. Check pattern match first
            val matchedPattern = repository.findMatchingPattern(number)
            if (matchedPattern != null) {
                Log.d(TAG, "BLOCKING (pattern) call from: $number")
                repository.logBlockedCall(number, matchedPattern)
                endCall(context)
                return@launch
            }

            Log.d(TAG, "Allowing call from: $number")
        }
    }


    private fun endCall(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val telecomManager = context.getSystemService(TelecomManager::class.java)
                val result = telecomManager?.endCall()
                Log.d(TAG, "endCall() result: $result")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "No permission to end call: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error ending call: ${e.message}")
        }
    }
}