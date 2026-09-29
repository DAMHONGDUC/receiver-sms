package com.dd.sms.hook.features.dispatch

import com.dd.sms.hook.features.apiconfig.domain.repository.ApiConfigRepository
import com.dd.sms.hook.features.calllog.domain.model.CallLog
import com.dd.sms.hook.features.calllog.domain.model.CallStatus
import com.dd.sms.hook.features.calllog.domain.model.CallTrigger
import com.dd.sms.hook.features.calllog.domain.repository.CallLogRepository
import com.dd.sms.hook.features.dispatch.domain.model.DispatchOutcome
import com.dd.sms.hook.features.dispatch.domain.model.HttpResult
import com.dd.sms.hook.features.dispatch.domain.repository.ReceivedSmsRepository
import com.dd.sms.hook.features.dispatch.domain.service.FailureNotifier
import com.dd.sms.hook.features.dispatch.domain.service.HttpExecutor
import com.dd.sms.hook.features.dispatch.domain.service.RequestFactory
import com.dd.sms.hook.features.dispatch.domain.service.TemplateRenderer
import com.dd.sms.hook.features.dispatch.domain.usecase.CallExecution
import com.dd.sms.hook.features.dispatch.domain.usecase.ExecuteQueuedCallUseCase
import com.dd.sms.hook.features.settings.domain.model.AppSettings
import com.dd.sms.hook.features.settings.domain.repository.SettingsRepository
import com.dd.sms.hook.testing.Fixtures
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExecuteQueuedCallUseCaseTest {
    private val configRepository: ApiConfigRepository = mockk()
    private val smsRepository: ReceivedSmsRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val callLogRepository: CallLogRepository = mockk()
    private val httpExecutor: HttpExecutor = mockk()
    private val notifier: FailureNotifier = mockk(relaxed = true)
    private val savedLog = slot<CallLog>()
    private val useCase = ExecuteQueuedCallUseCase(
        configRepository,
        smsRepository,
        settingsRepository,
        CallExecution(RequestFactory(TemplateRenderer()), httpExecutor, callLogRepository),
        notifier,
    )

    private fun given(result: HttpResult, maxRetries: Int = 2) {
        coEvery { configRepository.getById(7L) } returns Fixtures.config(maxRetries = maxRetries)
        coEvery { smsRepository.getById(3L) } returns Fixtures.sms()
        coEvery { settingsRepository.current() } returns AppSettings.DEFAULT
        coEvery { httpExecutor.execute(any(), any()) } returns result
        coEvery { callLogRepository.insert(capture(savedLog)) } returns 99L
    }

    @Test
    fun `2xx is a success and is logged with the response`() = runTest {
        given(HttpResult.Response(code = 201, body = "ok", durationMs = 12))

        val outcome: DispatchOutcome = useCase(7L, 3L, attempt = 1, trigger = CallTrigger.SMS)

        assertTrue(outcome is DispatchOutcome.Success)
        assertEquals(CallStatus.SUCCESS, savedLog.captured.status)
        assertEquals(201, savedLog.captured.responseCode)
        assertEquals(99L, (outcome as DispatchOutcome.Success).log.id)
    }

    @Test
    fun `5xx retries while attempts remain and does not notify`() = runTest {
        given(HttpResult.Response(code = 503, body = "down", durationMs = 5), maxRetries = 2)

        val outcome: DispatchOutcome = useCase(7L, 3L, attempt = 2, trigger = CallTrigger.SMS)

        assertEquals(true, (outcome as DispatchOutcome.Failed).willRetry)
        verify(exactly = 0) { notifier.notifyFailure(any()) }
    }

    @Test
    fun `last attempt stops retrying and notifies`() = runTest {
        given(HttpResult.Failure(message = "timeout", durationMs = 15_000), maxRetries = 2)

        val outcome: DispatchOutcome = useCase(7L, 3L, attempt = 3, trigger = CallTrigger.SMS)

        assertEquals(false, (outcome as DispatchOutcome.Failed).willRetry)
        verify(exactly = 1) { notifier.notifyFailure(any()) }
    }

    @Test
    fun `4xx other than 408 and 429 is not retried`() = runTest {
        given(HttpResult.Response(code = 400, body = "bad", durationMs = 5))

        val outcome: DispatchOutcome = useCase(7L, 3L, attempt = 1, trigger = CallTrigger.SMS)

        assertEquals(false, (outcome as DispatchOutcome.Failed).willRetry)
    }

    @Test
    fun `429 is retried`() = runTest {
        given(HttpResult.Response(code = 429, body = "slow down", durationMs = 5))

        val outcome: DispatchOutcome = useCase(7L, 3L, attempt = 1, trigger = CallTrigger.SMS)

        assertEquals(true, (outcome as DispatchOutcome.Failed).willRetry)
    }

    @Test
    fun `disabled config is skipped for sms but still runs for manual retry`() = runTest {
        given(HttpResult.Response(code = 200, body = "", durationMs = 1))
        coEvery { configRepository.getById(7L) } returns Fixtures.config().copy(enabled = false)

        assertTrue(useCase(7L, 3L, attempt = 1, trigger = CallTrigger.SMS) is DispatchOutcome.Skipped)
        assertTrue(useCase(7L, 3L, attempt = 1, trigger = CallTrigger.RETRY) is DispatchOutcome.Success)
    }

    @Test
    fun `deleted config is skipped without calling the network`() = runTest {
        coEvery { configRepository.getById(7L) } returns null
        coEvery { smsRepository.getById(3L) } returns Fixtures.sms()

        assertTrue(useCase(7L, 3L, attempt = 1, trigger = CallTrigger.SMS) is DispatchOutcome.Skipped)
    }
}
