package com.receiver.sms.testing

import com.receiver.sms.features.apiconfig.domain.model.ApiConfig
import com.receiver.sms.features.apiconfig.domain.model.ApiConfigDefaults
import com.receiver.sms.features.apiconfig.domain.model.MatchMode
import com.receiver.sms.features.apiconfig.domain.model.SmsFilter
import com.receiver.sms.features.dispatch.domain.model.ReceivedSms

/** Shared builders for unit tests. */
object Fixtures {
    const val RECEIVED_AT: Long = 1_700_000_000_000L

    fun config(
        id: Long = 7L,
        url: String = "https://example.com/hook",
        filter: SmsFilter = SmsFilter.ANY,
        maxRetries: Int = 2,
    ): ApiConfig = ApiConfigDefaults.newConfig().copy(
        id = id,
        name = "Bank hook",
        url = url,
        filter = filter,
        maxRetries = maxRetries,
    )

    fun filter(senders: String = "", keyword: String = "", mode: MatchMode = MatchMode.CONTAINS): SmsFilter =
        SmsFilter(senders = senders, keyword = keyword, mode = mode)

    fun sms(id: Long = 3L, sender: String = "+84 901 234 567", body: String = "Your OTP is 123456"): ReceivedSms =
        ReceivedSms(id, sender, body, RECEIVED_AT, subscriptionId = 1, matchedCount = 0)
}
