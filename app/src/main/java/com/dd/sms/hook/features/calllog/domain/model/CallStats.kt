package com.dd.sms.hook.features.calllog.domain.model

import java.time.LocalDate

data class CallSummary(
    val total: Int,
    val success: Int,
    val avgDurationMs: Long,
) {
    val failed: Int get() = total - success
    val successRate: Float get() = if (total == 0) 0f else success.toFloat() / total

    companion object {
        val EMPTY: CallSummary = CallSummary(total = 0, success = 0, avgDurationMs = 0L)
    }
}

/** A single call reduced to what the timeline chart needs. */
data class CallPoint(val createdAt: Long, val success: Boolean)

data class DailyCallCount(val day: LocalDate, val success: Int, val failed: Int) {
    val total: Int get() = success + failed
}

data class ApiCallBreakdown(
    val configId: Long?,
    val configName: String,
    val total: Int,
    val success: Int,
)
