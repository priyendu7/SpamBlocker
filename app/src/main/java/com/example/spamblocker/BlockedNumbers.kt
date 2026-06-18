package com.example.spamblocker

object BlockedNumbers {
    val numbers = emptyList<String>()

    fun shouldBlock(number: String?): Boolean {
        if (number == null) return false
        val cleaned = number.trimStart('+').replace(" ", "")
        return numbers.any { blocked ->
            cleaned.endsWith(blocked) || blocked.endsWith(cleaned)
        }
    }
}