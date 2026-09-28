package com.receiver.sms.features.dispatch.domain.service

import java.net.URLEncoder
import javax.inject.Inject

/** Placeholder names usable as {{name}} in URL, headers and body. */
object TemplateVariables {
    const val SENDER = "sender"
    const val BODY = "body"
    const val RECEIVED_AT = "received_at"
    const val RECEIVED_AT_ISO = "received_at_iso"
    const val SIM = "sim"
    const val CONFIG_NAME = "config_name"

    val ALL: List<String> = listOf(SENDER, BODY, RECEIVED_AT, RECEIVED_AT_ISO, SIM, CONFIG_NAME)

    fun token(name: String): String = "{{$name}}"
}

/** How substituted values are escaped for the place they land in. */
enum class Escaping { NONE, JSON, URL }

class TemplateRenderer @Inject constructor() {
    private val placeholder: Regex = Regex("\\{\\{\\s*(\\w+)\\s*\\}\\}")

    /** Unknown placeholders are left untouched so typos stay visible in the request log. */
    fun render(template: String, values: Map<String, String>, escaping: Escaping): String =
        placeholder.replace(template) { match ->
            val value: String? = values[match.groupValues[1]]
            if (value == null) match.value else escape(value, escaping)
        }

    private fun escape(value: String, escaping: Escaping): String = when (escaping) {
        Escaping.NONE -> value
        Escaping.URL -> URLEncoder.encode(value, Charsets.UTF_8.name())
        Escaping.JSON -> escapeJson(value)
    }

    private fun escapeJson(value: String): String {
        val out: StringBuilder = StringBuilder(value.length + 8)

        for (c in value) {
            when {
                c == '"' -> out.append("\\\"")
                c == '\\' -> out.append("\\\\")
                c == '\n' -> out.append("\\n")
                c == '\r' -> out.append("\\r")
                c == '\t' -> out.append("\\t")
                c == '\b' -> out.append("\\b")
                c == '\u000C' -> out.append("\\f")
                c < ' ' -> out.append(String.format("\\u%04x", c.code))
                else -> out.append(c)
            }
        }
        return out.toString()
    }
}
