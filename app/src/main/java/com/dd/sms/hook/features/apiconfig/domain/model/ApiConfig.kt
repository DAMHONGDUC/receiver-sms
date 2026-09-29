package com.dd.sms.hook.features.apiconfig.domain.model

enum class HttpMethod {
    GET, POST, PUT, PATCH, DELETE;

    val allowsBody: Boolean get() = this != GET
}

/** How the sender and keyword filters are compared against an incoming SMS. */
enum class MatchMode { CONTAINS, REGEX }

data class HeaderEntry(val name: String, val value: String)

/**
 * Which SMS trigger a config.
 * - [senders]: comma or newline separated list; blank means any sender.
 * - [keyword]: text the body must contain (or a regex in [MatchMode.REGEX]); blank means any body.
 */
data class SmsFilter(
    val senders: String,
    val keyword: String,
    val mode: MatchMode,
) {
    companion object {
        val ANY: SmsFilter = SmsFilter(senders = "", keyword = "", mode = MatchMode.CONTAINS)
    }
}

data class ApiConfig(
    val id: Long,
    val name: String,
    val url: String,
    val method: HttpMethod,
    val headers: List<HeaderEntry>,
    val bodyTemplate: String,
    val filter: SmsFilter,
    val enabled: Boolean,
    val timeoutSeconds: Int,
    val maxRetries: Int,
    val createdAt: Long,
    val updatedAt: Long,
) {
    val isNew: Boolean get() = id == NEW_ID

    companion object {
        const val NEW_ID: Long = 0L
    }
}
