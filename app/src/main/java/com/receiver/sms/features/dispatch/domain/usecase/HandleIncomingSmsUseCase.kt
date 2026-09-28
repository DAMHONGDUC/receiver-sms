package com.receiver.sms.features.dispatch.domain.usecase

import com.receiver.sms.core.logging.AppLogger
import com.receiver.sms.features.apiconfig.domain.model.ApiConfig
import com.receiver.sms.features.apiconfig.domain.repository.ApiConfigRepository
import com.receiver.sms.features.calllog.domain.model.CallTrigger
import com.receiver.sms.features.dispatch.domain.model.ReceivedSms
import com.receiver.sms.features.dispatch.domain.repository.ReceivedSmsRepository
import com.receiver.sms.features.dispatch.domain.service.CallScheduler
import com.receiver.sms.features.dispatch.domain.service.SmsMatcher
import com.receiver.sms.features.settings.domain.model.AppSettings
import com.receiver.sms.features.settings.domain.repository.SettingsRepository
import javax.inject.Inject

private const val TAG = "HandleIncomingSms"

/** Stores an incoming SMS and queues one API call per enabled config it matches. Returns the match count. */
class HandleIncomingSmsUseCase @Inject constructor(
    private val smsRepository: ReceivedSmsRepository,
    private val configRepository: ApiConfigRepository,
    private val settingsRepository: SettingsRepository,
    private val matcher: SmsMatcher,
    private val scheduler: CallScheduler,
) {
    suspend operator fun invoke(sender: String, body: String, receivedAt: Long, subscriptionId: Int): Int {
        val settings: AppSettings = settingsRepository.current()
        val smsId: Long = smsRepository.insert(
            ReceivedSms(ReceivedSms.NEW_ID, sender, body, receivedAt, subscriptionId, matchedCount = 0)
        )
        AppLogger.i(TAG, "sms stored - {id: $smsId, sender: $sender, length: ${body.length}, sim: $subscriptionId}")

        if (!settings.forwardingEnabled) {
            AppLogger.i(TAG, "forwarding disabled - no calls queued {smsId: $smsId}")
            return 0
        }
        val matched: List<ApiConfig> = configRepository.getEnabled()
            .filter { matcher.matches(it.filter, sender, body) }

        matched.forEach { scheduler.enqueue(it.id, smsId, CallTrigger.SMS) }
        smsRepository.setMatchedCount(smsId, matched.size)
        AppLogger.i(TAG, "calls queued - {smsId: $smsId, configs: ${matched.map { it.id }}}")

        return matched.size
    }
}
