package com.receiver.sms.features.dispatch.domain.usecase

import com.receiver.sms.core.logging.AppLogger
import com.receiver.sms.core.time.TimeUtils
import com.receiver.sms.features.apiconfig.domain.model.ApiConfig
import com.receiver.sms.features.apiconfig.domain.repository.ApiConfigRepository
import com.receiver.sms.features.calllog.domain.model.CallLog
import com.receiver.sms.features.calllog.domain.model.CallTrigger
import com.receiver.sms.features.calllog.domain.repository.CallLogRepository
import com.receiver.sms.features.dispatch.domain.model.ReceivedSms
import com.receiver.sms.features.dispatch.domain.repository.ReceivedSmsRepository
import com.receiver.sms.features.dispatch.domain.service.CallScheduler
import com.receiver.sms.features.settings.domain.model.RetentionPeriod
import com.receiver.sms.features.settings.domain.repository.SettingsRepository
import javax.inject.Inject

private const val TAG = "DispatchUseCases"

/** Sends a config (saved or still being edited) once with a sample SMS, logged as a TEST call. */
class TestApiCallUseCase @Inject constructor(private val execution: CallExecution) {
    suspend operator fun invoke(config: ApiConfig, sampleSender: String, sampleBody: String): CallLog {
        val sms: ReceivedSms = ReceivedSms(
            id = ReceivedSms.NEW_ID,
            sender = sampleSender,
            body = sampleBody,
            receivedAt = TimeUtils.now(),
            subscriptionId = 0,
            matchedCount = 0,
        )
        AppLogger.i(TAG, "test call - {config: ${config.id}, sender: $sampleSender}")

        return execution.run(config, sms, attempt = 1, trigger = CallTrigger.TEST).log
    }
}

enum class RetryResult { QUEUED, CONFIG_DELETED, NOT_RETRYABLE }

/** Re-queues the call behind a history entry with the same config and SMS. */
class RetryCallUseCase @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val configRepository: ApiConfigRepository,
    private val scheduler: CallScheduler,
) {
    suspend operator fun invoke(logId: Long): RetryResult {
        val log: CallLog? = callLogRepository.getById(logId)
        val configId: Long? = log?.configId
        val smsId: Long? = log?.smsId

        if (configId == null || smsId == null) {
            AppLogger.i(TAG, "retry refused - {log: $logId, configId: $configId, smsId: $smsId}")
            return RetryResult.NOT_RETRYABLE
        }
        if (configRepository.getById(configId) == null) {
            AppLogger.i(TAG, "retry refused, config deleted - {log: $logId, configId: $configId}")
            return RetryResult.CONFIG_DELETED
        }
        scheduler.enqueue(configId, smsId, CallTrigger.RETRY)
        AppLogger.i(TAG, "retry queued - {log: $logId, configId: $configId, smsId: $smsId}")

        return RetryResult.QUEUED
    }
}

/** Deletes history and stored SMS older than the retention setting. */
class PruneHistoryUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val callLogRepository: CallLogRepository,
    private val smsRepository: ReceivedSmsRepository,
) {
    suspend operator fun invoke() {
        val retention: RetentionPeriod = settingsRepository.current().retention
        val days: Int = retention.days ?: run {
            AppLogger.i(TAG, "prune skipped - retention is forever")
            return
        }
        val cutoff: Long = TimeUtils.daysAgo(days)
        val logs: Int = callLogRepository.deleteOlderThan(cutoff)
        val sms: Int = smsRepository.deleteOlderThan(cutoff)

        AppLogger.i(TAG, "pruned - {retentionDays: $days, logs: $logs, sms: $sms}")
    }
}
