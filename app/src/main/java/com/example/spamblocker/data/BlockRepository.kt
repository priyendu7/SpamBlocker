package com.example.spamblocker.data

import android.content.Context

class BlockRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val patternDao = db.blockPatternDao()
    private val logDao = db.blockedCallLogDao()
    private val settingsDao = db.blockSettingsDao()

    fun getAllPatterns() = patternDao.getAllPatterns()
    fun getAllBlockedCalls() = logDao.getAllBlockedCalls()
    fun getSettings() = settingsDao.getSettings()

    suspend fun addPattern(pattern: String, matchType: MatchType, simTarget: SimTarget) {
        patternDao.insert(BlockPattern(pattern = pattern, matchType = matchType, simTarget = simTarget))
    }

    suspend fun updatePattern(pattern: BlockPattern) {
        patternDao.update(pattern)
    }

    suspend fun deletePattern(pattern: BlockPattern) {
        patternDao.delete(pattern)
    }

    suspend fun updateSettings(settings: BlockSettings) {
        settingsDao.upsert(settings)
    }

    /**
     * Returns the matched pattern if the number should be blocked on the given SIM slot, else null.
     * simSlot: 0 = SIM1, 1 = SIM2, -1 = unknown slot (treated as matching BOTH-targeted patterns only)
     */
    suspend fun findMatchingPattern(number: String, simSlot: Int): BlockPattern? {
        val cleaned = number.trimStart('+').replace(" ", "").replace("-", "")
        val patterns = patternDao.getEnabledPatterns()

        return patterns.firstOrNull { p ->
            val simMatches = when (p.simTarget) {
                SimTarget.BOTH -> true
                SimTarget.SIM_1 -> simSlot == 0
                SimTarget.SIM_2 -> simSlot == 1
            }
            if (!simMatches) return@firstOrNull false

            val cleanedPattern = p.pattern.trimStart('+').replace(" ", "").replace("-", "")
            when (p.matchType) {
                MatchType.STARTS_WITH -> cleaned.startsWith(cleanedPattern)
                MatchType.ENDS_WITH -> cleaned.endsWith(cleanedPattern)
                MatchType.CONTAINS -> cleaned.contains(cleanedPattern)
            }
        }
    }

    suspend fun shouldBlockUnknown(simSlot: Int): Boolean {
        val settings = settingsDao.getSettingsOnce()
        return when (simSlot) {
            0 -> settings?.blockUnknownSim1 ?: false
            1 -> settings?.blockUnknownSim2 ?: false
            else -> (settings?.blockUnknownSim1 ?: false) || (settings?.blockUnknownSim2 ?: false)
        }
    }

    suspend fun logBlockedCall(
        number: String,
        pattern: BlockPattern?,
        simSlot: Int,
        isUnknownBlock: Boolean = false
    ) {
        logDao.insert(
            BlockedCallLog(
                number = number,
                matchedPattern = pattern?.pattern ?: "Unknown number",
                matchType = pattern?.matchType ?: MatchType.CONTAINS,
                simSlot = simSlot,
                isUnknownNumberBlock = isUnknownBlock
            )
        )
    }
}