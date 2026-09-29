package com.dd.sms.hook.features.dispatch.domain.model

import com.dd.sms.hook.features.apiconfig.domain.model.HeaderEntry
import com.dd.sms.hook.features.apiconfig.domain.model.HttpMethod
import com.dd.sms.hook.features.calllog.domain.model.CallLog

/** An SMS as received by the device; multipart messages are already joined. */
data class ReceivedSms(
    val id: Long,
    val sender: String,
    val body: String,
    val receivedAt: Long,
    val subscriptionId: Int,
    val matchedCount: Int,
) {
    companion object {
        const val NEW_ID: Long = 0L
    }
}

/** A fully rendered request, ready to send. */
data class HttpRequestSpec(
    val url: String,
    val method: HttpMethod,
    val headers: List<HeaderEntry>,
    val body: String?,
)

sealed interface HttpResult {
    val durationMs: Long

    data class Response(val code: Int, val body: String, override val durationMs: Long) : HttpResult {
        val isSuccessful: Boolean get() = code in 200..299
    }

    data class Failure(val message: String, override val durationMs: Long) : HttpResult
}

sealed interface DispatchOutcome {
    data class Success(val log: CallLog) : DispatchOutcome

    data class Failed(val log: CallLog, val willRetry: Boolean) : DispatchOutcome

    /** Nothing was sent, e.g. the config was deleted or disabled after the SMS was queued. */
    data class Skipped(val reason: String) : DispatchOutcome
}
