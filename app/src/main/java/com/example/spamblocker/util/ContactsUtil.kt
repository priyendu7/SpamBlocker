package com.example.spamblocker.util

import android.content.Context
import android.provider.ContactsContract
import android.util.Log

object ContactsUtil {
    private const val TAG = "ContactsUtil"

    /**
     * Returns true if the number is NOT found in the user's contacts.
     */
    fun isUnknownNumber(context: Context, number: String): Boolean {
        if (number.isBlank()) return true

        return try {
            val uri = android.net.Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                android.net.Uri.encode(number)
            )
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup._ID),
                null, null, null
            )
            cursor?.use {
                val found = it.moveToFirst()
                !found
            } ?: true
        } catch (e: SecurityException) {
            Log.e(TAG, "No permission to read contacts: ${e.message}")
            false // fail safe: don't block if we can't check
        } catch (e: Exception) {
            Log.e(TAG, "Error checking contacts: ${e.message}")
            false
        }
    }
}