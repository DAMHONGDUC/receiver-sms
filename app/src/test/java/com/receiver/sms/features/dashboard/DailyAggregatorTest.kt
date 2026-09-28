package com.receiver.sms.features.dashboard

import com.receiver.sms.features.calllog.domain.model.CallPoint
import com.receiver.sms.features.calllog.domain.model.DailyCallCount
import com.receiver.sms.features.dashboard.domain.service.DailyAggregator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class DailyAggregatorTest {
    private val zone: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")
    private val now: Long = ZonedDateTime.of(2026, 9, 28, 10, 0, 0, 0, zone).toInstant().toEpochMilli()

    private fun at(day: Int, hour: Int): Long = ZonedDateTime.of(2026, 9, day, hour, 0, 0, 0, zone).toInstant().toEpochMilli()

    @Test
    fun `buckets by local day and fills empty days`() {
        val points = listOf(
            CallPoint(at(28, 0), success = true),
            CallPoint(at(27, 23), success = false),
            CallPoint(at(27, 1), success = true),
        )

        val result: List<DailyCallCount> = DailyAggregator().aggregate(points, days = 3, nowMillis = now, zone = zone)

        assertEquals(
            listOf(
                DailyCallCount(LocalDate.of(2026, 9, 26), success = 0, failed = 0),
                DailyCallCount(LocalDate.of(2026, 9, 27), success = 1, failed = 1),
                DailyCallCount(LocalDate.of(2026, 9, 28), success = 1, failed = 0),
            ),
            result,
        )
    }

    @Test
    fun `no calls gives all zero days`() {
        val result: List<DailyCallCount> = DailyAggregator().aggregate(emptyList(), days = 7, nowMillis = now, zone = zone)

        assertEquals(7, result.size)
        assertEquals(0, result.sumOf { it.total })
    }
}
