package com.receiver.sms.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** All date/time arithmetic and formatting lives here. */
object TimeUtils {
    const val MILLIS_PER_DAY: Long = 86_400_000L

    private val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val dayLabelFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

    fun now(): Long = System.currentTimeMillis()

    fun toIso(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis).toString()

    fun formatDateTime(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        dateTimeFormatter.format(Instant.ofEpochMilli(epochMillis).atZone(zone))

    fun formatTime(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        timeFormatter.format(Instant.ofEpochMilli(epochMillis).atZone(zone))

    fun formatDayLabel(day: LocalDate): String = dayLabelFormatter.format(day)

    fun toLocalDate(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

    /** Start of the local day [days] - 1 days ago, so a range of 7 covers today plus the 6 before. */
    fun startOfRange(days: Int, nowMillis: Long = now(), zone: ZoneId = ZoneId.systemDefault()): Long =
        toLocalDate(nowMillis, zone).minusDays((days - 1).toLong())
            .atStartOfDay(zone).toInstant().toEpochMilli()

    /** Every local day in the range, oldest first. */
    fun daysOfRange(days: Int, nowMillis: Long = now(), zone: ZoneId = ZoneId.systemDefault()): List<LocalDate> {
        val today: LocalDate = toLocalDate(nowMillis, zone)

        return (days - 1 downTo 0).map { today.minusDays(it.toLong()) }
    }

    fun daysAgo(days: Int, nowMillis: Long = now()): Long = nowMillis - days * MILLIS_PER_DAY
}
