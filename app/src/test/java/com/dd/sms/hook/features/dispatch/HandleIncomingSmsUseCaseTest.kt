package com.dd.sms.hook.features.dispatch

import com.dd.sms.hook.features.apiconfig.domain.repository.ApiConfigRepository
import com.dd.sms.hook.features.calllog.domain.model.CallTrigger
import com.dd.sms.hook.features.dispatch.domain.repository.ReceivedSmsRepository
import com.dd.sms.hook.features.dispatch.domain.service.CallScheduler
import com.dd.sms.hook.features.dispatch.domain.service.SmsMatcher
import com.dd.sms.hook.features.dispatch.domain.usecase.HandleIncomingSmsUseCase
import com.dd.sms.hook.features.settings.domain.model.AppSettings
import com.dd.sms.hook.features.settings.domain.repository.SettingsRepository
import com.dd.sms.hook.testing.Fixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HandleIncomingSmsUseCaseTest {
    private val smsRepository: ReceivedSmsRepository = mockk(relaxed = true)
    private val configRepository: ApiConfigRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val scheduler: CallScheduler = mockk(relaxed = true)
    private val useCase = HandleIncomingSmsUseCase(smsRepository, configRepository, settingsRepository, SmsMatcher(), scheduler)

    @Test
    fun `queues one call per matching enabled config`() = runTest {
        coEvery { settingsRepository.current() } returns AppSettings.DEFAULT
        coEvery { smsRepository.insert(any()) } returns 42L
        coEvery { configRepository.getEnabled() } returns listOf(
            Fixtures.config(id = 1L, filter = Fixtures.filter(senders = "VCB")),
            Fixtures.config(id = 2L, filter = Fixtures.filter(keyword = "otp")),
            Fixtures.config(id = 3L, filter = Fixtures.filter(senders = "TCB")),
        )

        val matched: Int = useCase("VCB", "Your OTP 1", Fixtures.RECEIVED_AT, subscriptionId = 0)

        assertEquals(2, matched)
        verify { scheduler.enqueue(1L, 42L, CallTrigger.SMS) }
        verify { scheduler.enqueue(2L, 42L, CallTrigger.SMS) }
        verify(exactly = 0) { scheduler.enqueue(3L, any(), any()) }
        coVerify { smsRepository.setMatchedCount(42L, 2) }
    }

    @Test
    fun `stores the sms but queues nothing when forwarding is off`() = runTest {
        coEvery { settingsRepository.current() } returns AppSettings.DEFAULT.copy(forwardingEnabled = false)
        coEvery { smsRepository.insert(any()) } returns 42L

        assertEquals(0, useCase("VCB", "x", Fixtures.RECEIVED_AT, subscriptionId = 0))
        coVerify(exactly = 1) { smsRepository.insert(any()) }
        verify(exactly = 0) { scheduler.enqueue(any(), any(), any()) }
    }
}
