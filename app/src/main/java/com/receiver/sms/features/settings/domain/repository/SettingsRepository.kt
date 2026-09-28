package com.receiver.sms.features.settings.domain.repository

import com.receiver.sms.features.settings.domain.model.AppSettings
import com.receiver.sms.features.settings.domain.model.RetentionPeriod
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun current(): AppSettings

    suspend fun setForwardingEnabled(enabled: Boolean)

    suspend fun setKeepAliveEnabled(enabled: Boolean)

    suspend fun setNotifyOnFailure(enabled: Boolean)

    suspend fun setRetention(retention: RetentionPeriod)
}
