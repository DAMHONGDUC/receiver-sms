package com.dd.sms.hook.features.apiconfig.domain.repository

import com.dd.sms.hook.features.apiconfig.domain.model.ApiConfig
import kotlinx.coroutines.flow.Flow

interface ApiConfigRepository {
    fun observeAll(): Flow<List<ApiConfig>>

    fun observeEnabledCount(): Flow<Int>

    suspend fun getById(id: Long): ApiConfig?

    suspend fun getEnabled(): List<ApiConfig>

    /** Inserts when [ApiConfig.isNew], updates otherwise; returns the stored id. */
    suspend fun save(config: ApiConfig): Long

    suspend fun setEnabled(id: Long, enabled: Boolean)

    suspend fun delete(id: Long)
}
