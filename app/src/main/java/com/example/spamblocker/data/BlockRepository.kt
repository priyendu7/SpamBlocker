package com.example.spamblocker.data

import android.content.Context

class BlockRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val patternDao = db.blockPatternDao()
    private val logDao = db.blockedCallLogDao()

    fun getAllPatterns() = patternDao.getAllPatterns()
    fun getAllBlockedCalls() = logDao.getAllBlockedCalls()

    suspend fun addPattern(pattern: String, matchType: MatchType) {
        patternDao.insert(BlockPattern(pattern = pattern, matchType = matchType))
    }

    suspend fun updatePattern(pattern: BlockPattern) {
        patternDao.update(pattern)
    }

    suspend fun deletePattern(pattern: BlockPattern) {
        patternDao.delete(pattern)
    }

    /**
     * Returns the matched pattern if the number should be blocked on the given SIM slot, else null.
     */
    suspend fun findMatchingPattern(number: String,): BlockPattern? {
        val cleaned = number.trimStart('+').replace(" ", "").replace("-", "")
        val patterns = patternDao.getEnabledPatterns()

        return patterns.firstOrNull { p ->
            val cleanedPattern = p.pattern.trimStart('+').replace(" ", "").replace("-", "")
            when (p.matchType) {
                MatchType.STARTS_WITH -> cleaned.startsWith(cleanedPattern)
                MatchType.ENDS_WITH -> cleaned.endsWith(cleanedPattern)
                MatchType.CONTAINS -> cleaned.contains(cleanedPattern)
            }
        }
    }

    suspend fun logBlockedCall(
        number: String,
        pattern: BlockPattern?,
        isUnknownBlock: Boolean = false
    ) {
        logDao.insert(
            BlockedCallLog(
                number = number,
                matchedPattern = pattern?.pattern ?: "Unknown number",
                matchType = pattern?.matchType ?: MatchType.CONTAINS,
                isUnknownNumberBlock = isUnknownBlock
            )
        )
    }
}