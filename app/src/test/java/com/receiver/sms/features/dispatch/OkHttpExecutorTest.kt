package com.receiver.sms.features.dispatch

import com.receiver.sms.features.apiconfig.domain.model.HeaderEntry
import com.receiver.sms.features.apiconfig.domain.model.HttpMethod
import com.receiver.sms.features.dispatch.data.remote.OkHttpExecutor
import com.receiver.sms.features.dispatch.domain.model.HttpRequestSpec
import com.receiver.sms.features.dispatch.domain.model.HttpResult
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OkHttpExecutorTest {
    private val server = MockWebServer()
    private val executor = OkHttpExecutor(OkHttpClient())

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `sends method, headers and body and returns the response`() = runTest {
        server.enqueue(MockResponse.Builder().code(202).body("accepted").build())
        val spec = HttpRequestSpec(
            url = server.url("/hook").toString(),
            method = HttpMethod.PUT,
            headers = listOf(HeaderEntry("X-Key", "abc"), HeaderEntry("Content-Type", "application/json")),
            body = "{\"a\":1}",
        )

        val result: HttpResult = executor.execute(spec, timeoutSeconds = 5)
        val recorded: RecordedRequest = server.takeRequest()

        assertEquals(202, (result as HttpResult.Response).code)
        assertEquals("accepted", result.body)
        assertEquals("PUT", recorded.method)
        assertEquals("abc", recorded.headers["X-Key"])
        assertEquals("{\"a\":1}", recorded.body?.utf8())
    }

    @Test
    fun `invalid header becomes a failure instead of throwing`() = runTest {
        val spec = HttpRequestSpec(server.url("/").toString(), HttpMethod.GET, listOf(HeaderEntry("Bad Name", "v")), null)

        assertTrue(executor.execute(spec, timeoutSeconds = 5) is HttpResult.Failure)
    }

    @Test
    fun `unreachable host becomes a failure`() = runTest {
        val spec = HttpRequestSpec("http://127.0.0.1:1/", HttpMethod.GET, emptyList(), null)

        assertTrue(executor.execute(spec, timeoutSeconds = 2) is HttpResult.Failure)
    }
}
