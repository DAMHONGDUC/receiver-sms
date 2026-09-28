package com.receiver.sms.features.calllog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    @Query(
        """
        SELECT * FROM call_logs
        WHERE (:status IS NULL OR status = :status)
          AND (:configId IS NULL OR config_id = :configId)
          AND (:query = '' OR config_name LIKE '%' || :query || '%' OR sms_sender LIKE '%' || :query || '%'
               OR sms_body LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%')
        ORDER BY created_at DESC
        LIMIT :limit
        """
    )
    fun observe(status: String?, configId: Long?, query: String, limit: Int): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE id = :id")
    fun observeById(id: Long): Flow<CallLogEntity?>

    @Query("SELECT * FROM call_logs WHERE id = :id")
    suspend fun getById(id: Long): CallLogEntity?

    @Insert
    suspend fun insert(entity: CallLogEntity): Long

    @Query("DELETE FROM call_logs WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM call_logs")
    suspend fun clearAll()

    @Query("DELETE FROM call_logs WHERE created_at < :epochMillis")
    suspend fun deleteOlderThan(epochMillis: Long): Int

    @Query(
        """
        SELECT COUNT(*) AS total,
               COALESCE(SUM(CASE WHEN status = :successStatus THEN 1 ELSE 0 END), 0) AS success,
               AVG(duration_ms) AS avg_duration
        FROM call_logs
        WHERE created_at >= :fromMillis AND `trigger` != :excludedTrigger
        """
    )
    fun observeSummary(fromMillis: Long, successStatus: String, excludedTrigger: String): Flow<CallSummaryRow>

    @Query(
        """
        SELECT created_at, status FROM call_logs
        WHERE created_at >= :fromMillis AND `trigger` != :excludedTrigger
        """
    )
    fun observeTimeline(fromMillis: Long, excludedTrigger: String): Flow<List<CallPointRow>>

    @Query(
        """
        SELECT config_id, MAX(config_name) AS config_name, COUNT(*) AS total,
               SUM(CASE WHEN status = :successStatus THEN 1 ELSE 0 END) AS success
        FROM call_logs
        WHERE created_at >= :fromMillis AND `trigger` != :excludedTrigger
        GROUP BY config_id
        ORDER BY total DESC
        LIMIT :limit
        """
    )
    fun observeBreakdown(
        fromMillis: Long,
        successStatus: String,
        excludedTrigger: String,
        limit: Int,
    ): Flow<List<ApiBreakdownRow>>
}
