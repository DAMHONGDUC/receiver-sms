package com.dd.sms.hook.features.dashboard.domain.model

import com.dd.sms.hook.features.calllog.domain.model.ApiCallBreakdown
import com.dd.sms.hook.features.calllog.domain.model.CallLog
import com.dd.sms.hook.features.calllog.domain.model.CallSummary
import com.dd.sms.hook.features.calllog.domain.model.DailyCallCount

enum class DashboardRange(val days: Int) {
    WEEK(7),
    MONTH(30),
}

data class DashboardData(
    val range: DashboardRange,
    val summary: CallSummary,
    val smsReceived: Int,
    val daily: List<DailyCallCount>,
    val topApis: List<ApiCallBreakdown>,
    val recent: List<CallLog>,
    val enabledApis: Int,
    val forwardingEnabled: Boolean,
    val keepAliveEnabled: Boolean,
    val hasForwardedSms: Boolean,
)
