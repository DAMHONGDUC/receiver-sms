package com.dd.sms.hook.features.apiconfig.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiConfigDao {
    @Query("SELECT * FROM api_configs ORDER BY created_at DESC")
    fun observeAll(): Flow<List<ApiConfigEntity>>

    @Query("SELECT COUNT(*) FROM api_configs WHERE enabled = 1")
    fun observeEnabledCount(): Flow<Int>

    @Query("SELECT * FROM api_configs WHERE id = :id")
    suspend fun getById(id: Long): ApiConfigEntity?

    @Query("SELECT * FROM api_configs WHERE enabled = 1")
    suspend fun getEnabled(): List<ApiConfigEntity>

    @Insert
    suspend fun insert(entity: ApiConfigEntity): Long

    @Update
    suspend fun update(entity: ApiConfigEntity)

    @Query("UPDATE api_configs SET enabled = :enabled, updated_at = :updatedAt WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean, updatedAt: Long)

    @Query("DELETE FROM api_configs WHERE id = :id")
    suspend fun delete(id: Long)
}
