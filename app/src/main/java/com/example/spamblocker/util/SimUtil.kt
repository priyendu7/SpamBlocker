package com.example.spamblocker.util

import android.content.Context
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log

object SimUtil {
    private const val TAG = "SimUtil"

    /**
     * Returns 0 for SIM slot 1, 1 for SIM slot 2, -1 if unknown.
     * Requires READ_PHONE_STATE permission.
     */
    fun getSimSlotFromSubscriptionId(context: Context, subscriptionId: Int): Int {
        if (subscriptionId == -1 || subscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            return -1
        }
        return try {
            val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
            val activeSubs = subscriptionManager?.activeSubscriptionInfoList
            val matched = activeSubs?.firstOrNull { it.subscriptionId == subscriptionId }
            matched?.simSlotIndex ?: -1
        } catch (e: SecurityException) {
            Log.e(TAG, "No permission to read subscription info: ${e.message}")
            -1
        } catch (e: Exception) {
            Log.e(TAG, "Error getting sim slot: ${e.message}")
            -1
        }
    }

    /**
     * Best-effort extraction of subscriptionId from a PHONE_STATE intent.
     * Different OEMs and Android versions put this in different places.
     */
    fun extractSubscriptionId(context: Context, intentExtras: android.os.Bundle?): Int {
        // Try standard extra keys used across AOSP / common OEMs
        val candidates = listOf(
            "subscription",
            "android.telephony.extra.SUBSCRIPTION_ID",
            "extra_asus_dial_use_simid",
            "simId",
            "slot"
        )
        intentExtras?.let { bundle ->
            for (key in candidates) {
                if (bundle.containsKey(key)) {
                    val value = bundle.getInt(key, -1)
                    if (value != -1) return value
                }
            }
        }
        return -1
    }
}