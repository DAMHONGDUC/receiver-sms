package com.receiver.sms.features.settings.domain.model

enum class RetentionPeriod(val days: Int?) {
    WEEK(7),
    MONTH(30),
    QUARTER(90),
    FOREVER(null),
}

data class AppSettings(
    val forwardingEnabled: Boolean,
    val keepAliveEnabled: Boolean,
    val notifyOnFailure: Boolean,
    val retention: RetentionPeriod,
) {
    companion object {
        val DEFAULT: AppSettings = AppSettings(
            forwardingEnabled = true,
            keepAliveEnabled = false,
            notifyOnFailure = true,
            retention = RetentionPeriod.MONTH,
        )
    }
}
