package com.dd.sms.hook.features.settings.domain.repository

import com.dd.sms.hook.features.settings.domain.model.AppSettings
import com.dd.sms.hook.features.settings.domain.model.RetentionPeriod
import com.dd.sms.hook.features.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun current(): AppSettings

    suspend fun setForwardingEnabled(enabled: Boolean)

    suspend fun setKeepAliveEnabled(enabled: Boolean)

    suspend fun setNotifyOnFailure(enabled: Boolean)

    suspend fun setRetention(retention: RetentionPeriod)

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setDynamicColor(enabled: Boolean)
}
