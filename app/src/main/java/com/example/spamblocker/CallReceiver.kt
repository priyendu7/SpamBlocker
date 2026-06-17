package com.example.spamblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import com.example.spamblocker.data.BlockRepository
import com.example.spamblocker.util.ContactsUtil
import com.example.spamblocker.util.SimUtil
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

        // Try to get subscription id directly from intent extras (varies by OEM)
        val subId = SimUtil.extractSubscriptionId(appContext, intent.extras)
        val simSlot = SimUtil.getSimSlotFromSubscriptionId(appContext, subId)
        Log.d(TAG, "Detected subId=$subId, simSlot=$simSlot")

        val numberFromIntent = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        Log.d(TAG, "Number from intent: $numberFromIntent")

        if (numberFromIntent != null) {
            handleNumber(appContext, numberFromIntent, simSlot)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                val (numberFromLog, logSimSlot) = getLastIncomingNumberAndSimFromLog(appContext)
                Log.d(TAG, "Number from call log: $numberFromLog, simSlot from log: $logSimSlot")
                if (numberFromLog != null) {
                    // Prefer simSlot from intent if we got one, else fall back to call log's slot
                    val finalSimSlot = if (simSlot != -1) simSlot else logSimSlot
                    handleNumber(appContext, numberFromLog, finalSimSlot)
                }
            }, 500)
        }
    }

    private fun handleNumber(context: Context, number: String, simSlot: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val repository = BlockRepository(context)

            // 1. Check pattern match first
            val matchedPattern = repository.findMatchingPattern(number, simSlot)
            if (matchedPattern != null) {
                Log.d(TAG, "BLOCKING (pattern) call from: $number on SIM slot $simSlot")
                repository.logBlockedCall(number, matchedPattern, simSlot)
                endCall(context)
                return@launch
            }

            // 2. Check unknown-number blocking
            val blockUnknownEnabled = repository.shouldBlockUnknown(simSlot)
            if (blockUnknownEnabled) {
                val isUnknown = ContactsUtil.isUnknownNumber(context, number)
                if (isUnknown) {
                    Log.d(TAG, "BLOCKING (unknown number) call from: $number on SIM slot $simSlot")
                    repository.logBlockedCall(number, null, simSlot, isUnknownBlock = true)
                    endCall(context)
                    return@launch
                }
            }

            Log.d(TAG, "Allowing call from: $number on SIM slot $simSlot")
        }
    }

    /**
     * Returns Pair(number, simSlot). simSlot from call log via PHONE_ACCOUNT_ID, best-effort.
     */
    private fun getLastIncomingNumberAndSimFromLog(context: Context): Pair<String?, Int> {
        return try {
            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.PHONE_ACCOUNT_ID
                ),
                "${CallLog.Calls.TYPE} = ?",
                arrayOf(CallLog.Calls.INCOMING_TYPE.toString()),
                "${CallLog.Calls.DATE} DESC"
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                    val phoneAccountId = try {
                        it.getString(it.getColumnIndexOrThrow(CallLog.Calls.PHONE_ACCOUNT_ID))
                    } catch (e: Exception) {
                        null
                    }
                    val simSlot = resolveSimSlotFromPhoneAccountId(context, phoneAccountId)
                    Pair(number, simSlot)
                } else Pair(null, -1)
            } ?: Pair(null, -1)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading call log: ${e.message}")
            Pair(null, -1)
        }
    }

    private fun resolveSimSlotFromPhoneAccountId(context: Context, phoneAccountId: String?): Int {
        if (phoneAccountId == null) return -1
        return try {
            val subId = phoneAccountId.toIntOrNull() ?: return -1
            SimUtil.getSimSlotFromSubscriptionId(context, subId)
        } catch (e: Exception) {
            -1
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