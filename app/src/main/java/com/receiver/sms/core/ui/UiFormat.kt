package com.receiver.sms.core.ui

import java.util.Locale

/** Number formatting shared by screens. */
object UiFormat {
    private const val MILLIS_PER_SECOND = 1000.0
    private const val PERCENT = 100

    fun duration(ms: Long): String =
        if (ms < MILLIS_PER_SECOND) "$ms ms" else String.format(Locale.getDefault(), "%.1f s", ms / MILLIS_PER_SECOND)

    fun percent(ratio: Float): String = "${(ratio * PERCENT).toInt()}%"
}
