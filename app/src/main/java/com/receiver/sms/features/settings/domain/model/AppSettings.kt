package com.receiver.sms.features.settings.domain.model

enum class RetentionPeriod(val days: Int?) {
    WEEK(7),
    MONTH(30),
    QUARTER(90),
    FOREVER(null),
}

/** Which colour scheme the app uses; SYSTEM follows the device setting. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val forwardingEnabled: Boolean,
    val keepAliveEnabled: Boolean,
    val notifyOnFailure: Boolean,
    val retention: RetentionPeriod,
    val themeMode: ThemeMode,
    val dynamicColor: Boolean,
) {
    companion object {
        val DEFAULT: AppSettings = AppSettings(
            forwardingEnabled = true,
            keepAliveEnabled = false,
            notifyOnFailure = true,
            retention = RetentionPeriod.MONTH,
            themeMode = ThemeMode.SYSTEM,
            dynamicColor = false,
        )
    }
}
