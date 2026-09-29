package com.receiver.sms.features.dashboard

import com.receiver.sms.core.time.TimeUtils
import com.receiver.sms.features.dashboard.domain.model.DashboardRange
import com.receiver.sms.features.dashboard.domain.service.DailyAggregator
import com.receiver.sms.features.dashboard.domain.usecase.ObserveDashboardUseCase
import com.receiver.sms.features.dashboard.presentation.DashboardState
import com.receiver.sms.features.dashboard.presentation.DashboardViewModel
import com.receiver.sms.testing.FakeApiConfigRepository
import com.receiver.sms.testing.FakeCallLogRepository
import com.receiver.sms.testing.FakeReceivedSmsRepository
import com.receiver.sms.testing.FakeSettingsRepository
import com.receiver.sms.testing.Fixtures
import com.receiver.sms.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `switching the range reloads data for that range`() = runTest {
        val logs = FakeCallLogRepository(listOf(Fixtures.log(id = 1L, createdAt = TimeUtils.now() - 10 * TimeUtils.MILLIS_PER_DAY)))
        val vm = DashboardViewModel(
            ObserveDashboardUseCase(logs, FakeReceivedSmsRepository(), FakeApiConfigRepository(), FakeSettingsRepository(), DailyAggregator())
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect { } }

        assertEquals(0, (vm.state.value as DashboardState.Loaded).data.summary.total)
        vm.onRangeChange(DashboardRange.MONTH)
        val data = (vm.state.value as DashboardState.Loaded).data
        assertEquals(DashboardRange.MONTH, data.range)
        assertEquals(30, data.daily.size)
        assertEquals(1, data.summary.total)
    }
}
