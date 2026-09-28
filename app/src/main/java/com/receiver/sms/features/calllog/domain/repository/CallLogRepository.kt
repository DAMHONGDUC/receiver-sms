package com.receiver.sms.features.calllog.domain.repository

import com.receiver.sms.features.calllog.domain.model.ApiCallBreakdown
import com.receiver.sms.features.calllog.domain.model.CallLog
import com.receiver.sms.features.calllog.domain.model.CallLogFilter
import com.receiver.sms.features.calllog.domain.model.CallPoint
import com.receiver.sms.features.calllog.domain.model.CallSummary
import kotlinx.coroutines.flow.Flow

interface CallLogRepository {
    fun observe(filter: CallLogFilter, limit: Int): Flow<List<CallLog>>

    fun observeById(id: Long): Flow<CallLog?>

    suspend fun getById(id: Long): CallLog?

    suspend fun insert(log: CallLog): Long

    suspend fun delete(id: Long)

    suspend fun clearAll()

    suspend fun deleteOlderThan(epochMillis: Long): Int

    /** Analytics below exclude TEST calls. */
    fun observeSummary(fromMillis: Long): Flow<CallSummary>

    fun observeTimeline(fromMillis: Long): Flow<List<CallPoint>>

    fun observeBreakdown(fromMillis: Long, limit: Int): Flow<List<ApiCallBreakdown>>
}
