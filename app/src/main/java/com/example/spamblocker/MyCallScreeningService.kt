package com.example.spamblocker

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.example.spamblocker.data.BlockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyCallScreeningService : CallScreeningService() {
    private val TAG = "MyCallScreeningService"

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart ?: ""
        Log.d(TAG, "onScreenCall: Incoming: $number")

        CoroutineScope(Dispatchers.IO).launch {
            val repository = BlockRepository(applicationContext)

            val matchedPattern = repository.findMatchingPattern(number)
            if (matchedPattern != null) {
                repository.logBlockedCall(number, matchedPattern)
                respondToCall(callDetails, blockResponse())
                return@launch
            }

            respondToCall(callDetails, CallResponse.Builder().build())
        }
    }

    private fun blockResponse() = CallResponse.Builder()
        .setDisallowCall(true)
        .setRejectCall(true)
        .setSkipCallLog(false)
        .setSkipNotification(true)
        .build()

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "SERVICE CREATED")
    }
}