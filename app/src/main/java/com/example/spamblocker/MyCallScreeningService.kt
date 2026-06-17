package com.example.spamblocker

import android.telecom.Call
import android.telecom.CallScreeningService
import android.telephony.SubscriptionManager
import android.util.Log
import com.example.spamblocker.data.BlockRepository
import com.example.spamblocker.util.ContactsUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyCallScreeningService : CallScreeningService() {
    private val TAG = "MyCallScreeningService"

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart ?: ""
        val simSlot = resolveSimSlot(callDetails)
        Log.d(TAG, "onScreenCall: Incoming: $number, simSlot: $simSlot")

        CoroutineScope(Dispatchers.IO).launch {
            val repository = BlockRepository(applicationContext)

            val matchedPattern = repository.findMatchingPattern(number, simSlot)
            if (matchedPattern != null) {
                repository.logBlockedCall(number, matchedPattern, simSlot)
                respondToCall(callDetails, blockResponse())
                return@launch
            }

            if (repository.shouldBlockUnknown(simSlot)) {
                val isUnknown = ContactsUtil.isUnknownNumber(applicationContext, number)
                if (isUnknown) {
                    repository.logBlockedCall(number, null, simSlot, isUnknownBlock = true)
                    respondToCall(callDetails, blockResponse())
                    return@launch
                }
            }

            respondToCall(callDetails, CallResponse.Builder().build())
        }
    }

    private fun resolveSimSlot(callDetails: Call.Details): Int {
        return try {
            val accountHandle = callDetails.accountHandle
            val subscriptionManager = getSystemService(SubscriptionManager::class.java)
            val phoneAccountId = accountHandle?.id
            val subId = phoneAccountId?.toIntOrNull() ?: return -1
            val activeSubs = subscriptionManager?.activeSubscriptionInfoList
            activeSubs?.firstOrNull { it.subscriptionId == subId }?.simSlotIndex ?: -1
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving sim slot: ${e.message}")
            -1
        }
    }

    private fun blockResponse() = android.telecom.CallScreeningService.CallResponse.Builder()
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