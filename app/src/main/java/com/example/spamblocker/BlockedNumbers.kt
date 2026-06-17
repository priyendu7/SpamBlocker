package com.example.spamblocker

object BlockedNumbers {
    val numbers = listOf(
        "9450843436",
        "1234567890"
    )

    fun shouldBlock(number: String?): Boolean {
        if (number == null) return false
        val cleaned = number.trimStart('+').replace(" ", "")
        return numbers.any { blocked ->
            cleaned.endsWith(blocked) || blocked.endsWith(cleaned)
        }
    }
}