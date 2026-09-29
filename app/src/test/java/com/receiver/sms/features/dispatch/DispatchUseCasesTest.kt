package com.receiver.sms.features.dispatch

import com.receiver.sms.core.time.TimeUtils
import com.receiver.sms.features.apiconfig.domain.model.ApiConfigDefaults
import com.receiver.sms.features.calllog.domain.model.CallLog
import com.receiver.sms.features.calllog.domain.model.CallStatus
import com.receiver.sms.features.calllog.domain.model.CallTrigger
import com.receiver.sms.features.dispatch.domain.model.HttpResult
import com.receiver.sms.features.dispatch.domain.service.RequestFactory
import com.receiver.sms.features.dispatch.domain.service.TemplateRenderer
import com.receiver.sms.features.dispatch.domain.usecase.CallExecution
import com.receiver.sms.features.dispatch.domain.usecase.PruneHistoryUseCase
import com.receiver.sms.features.dispatch.domain.usecase.RetryCallUseCase
import com.receiver.sms.features.dispatch.domain.usecase.RetryResult
import com.receiver.sms.features.dispatch.domain.usecase.TestApiCallUseCase
import com.receiver.sms.features.settings.domain.model.AppSettings
import com.receiver.sms.features.settings.domain.model.RetentionPeriod
import com.receiver.sms.testing.FakeApiConfigRepository
import com.receiver.sms.testing.FakeCallLogRepository
import com.receiver.sms.testing.FakeCallScheduler
import com.receiver.sms.testing.FakeHttpExecutor
import com.receiver.sms.testing.FakeReceivedSmsRepository
import com.receiver.sms.testing.FakeSettingsRepository
import com.receiver.sms.testing.Fixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DispatchUseCasesTest {
    private val logs = FakeCallLogRepository()
    private val http = FakeHttpExecutor()
    private val execution = CallExecution(RequestFactory(TemplateRenderer()), http, logs)

    @Test
    fun `test call of an unsaved config is logged as TEST without config or sms ids`() = runTest {
        val config = ApiConfigDefaults.newConfig().copy(name = "Draft", url = "https://x.io")

        val log: CallLog = TestApiCallUseCase(execution)(config, "VCB", "hello")

        assertEquals(CallTrigger.TEST, log.trigger)
        assertNull(log.configId)
        assertNull(log.smsId)
        assertEquals("VCB", log.smsSender)
        assertEquals(1, logs.logs.value.size)
        assertTrue(http.requests.single().body!!.contains("\"message\": \"hello\""))
    }

    @Test
    fun `execution snapshots the rendered request and response`() = runTest {
        http.result = HttpResult.Response(code = 201, body = "created", durationMs = 42)

        val result: CallExecution.Result = execution.run(Fixtures.config(), Fixtures.sms(), attempt = 2, trigger = CallTrigger.RETRY)

        assertEquals(CallStatus.SUCCESS, result.log.status)
        assertEquals(201, result.log.responseCode)
        assertEquals("created", result.log.responseBody)
        assertEquals(42L, result.log.durationMs)
        assertEquals(2, result.log.attempt)
        assertEquals(http.requests.single().url, result.log.url)
        assertEquals(false, result.retryable)
    }

    @Test
    fun `retryable classification covers network errors, 5xx and 408 425 429`() = runTest {
        val cases: Map<HttpResult, Boolean> = mapOf(
            HttpResult.Failure("timeout", 1) to true,
            HttpResult.Response(500, "", 1) to true,
            HttpResult.Response(503, "", 1) to true,
            HttpResult.Response(408, "", 1) to true,
            HttpResult.Response(425, "", 1) to true,
            HttpResult.Response(429, "", 1) to true,
            HttpResult.Response(400, "", 1) to false,
            HttpResult.Response(404, "", 1) to false,
        )

        cases.forEach { (response, expected) ->
            http.result = response
            assertEquals("for $response", expected, execution.run(Fixtures.config(), Fixtures.sms(), 1, CallTrigger.SMS).retryable)
        }
    }

    @Test
    fun `retry queues the same config and sms`() = runTest {
        val scheduler = FakeCallScheduler()
        val repo = FakeCallLogRepository(listOf(Fixtures.log(id = 1L, configId = 7L, smsId = 3L)))

        val result: RetryResult = RetryCallUseCase(repo, FakeApiConfigRepository(listOf(Fixtures.config(id = 7L))), scheduler)(1L)

        assertEquals(RetryResult.QUEUED, result)
        assertEquals(Triple(7L, 3L, CallTrigger.RETRY), scheduler.enqueued.single())
    }

    @Test
    fun `retry is refused for a deleted config or a call without sms`() = runTest {
        val scheduler = FakeCallScheduler()
        val repo = FakeCallLogRepository(listOf(Fixtures.log(id = 1L), Fixtures.log(id = 2L, smsId = null)))
        val retry = RetryCallUseCase(repo, FakeApiConfigRepository(), scheduler)

        assertEquals(RetryResult.CONFIG_DELETED, retry(1L))
        assertEquals(RetryResult.NOT_RETRYABLE, retry(2L))
        assertEquals(RetryResult.NOT_RETRYABLE, retry(99L))
        assertTrue(scheduler.enqueued.isEmpty())
    }

    @Test
    fun `prune deletes logs and sms older than the retention window`() = runTest {
        val sms = FakeReceivedSmsRepository()
        val now: Long = TimeUtils.now()
        val calls = FakeCallLogRepository(listOf(Fixtures.log(id = 1L, createdAt = now - 40 * TimeUtils.MILLIS_PER_DAY), Fixtures.log(id = 2L, createdAt = now)))

        PruneHistoryUseCase(FakeSettingsRepository(AppSettings.DEFAULT.copy(retention = RetentionPeriod.MONTH)), calls, sms)()

        assertEquals(listOf(2L), calls.logs.value.map { it.id })
        val cutoff: Long = calls.deletedBefore!!
        assertTrue(cutoff in (now - 31 * TimeUtils.MILLIS_PER_DAY)..(now - 29 * TimeUtils.MILLIS_PER_DAY))
        assertEquals(cutoff, sms.deletedBefore)
    }

    @Test
    fun `prune keeps everything when retention is forever`() = runTest {
        val calls = FakeCallLogRepository(listOf(Fixtures.log(id = 1L, createdAt = 0L)))
        val sms = FakeReceivedSmsRepository()

        PruneHistoryUseCase(FakeSettingsRepository(AppSettings.DEFAULT.copy(retention = RetentionPeriod.FOREVER)), calls, sms)()

        assertEquals(1, calls.logs.value.size)
        assertNull(calls.deletedBefore)
        assertNull(sms.deletedBefore)
    }
}
