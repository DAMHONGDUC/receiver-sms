package com.receiver.sms.features.dispatch

import com.receiver.sms.features.apiconfig.domain.model.MatchMode
import com.receiver.sms.features.dispatch.domain.service.SmsMatcher
import com.receiver.sms.testing.Fixtures
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsMatcherTest {
    private val matcher = SmsMatcher()

    @Test
    fun `blank filter matches everything`() {
        assertTrue(matcher.matches(Fixtures.filter(), "anyone", "anything"))
    }

    @Test
    fun `sender list ignores spacing and dashes and matches any entry`() {
        val filter = Fixtures.filter(senders = "VCB, 901-234-567")

        assertTrue(matcher.matches(filter, "+84 901 234 567", "x"))
        assertTrue(matcher.matches(filter, "vcb", "x"))
        assertFalse(matcher.matches(filter, "TCB", "x"))
    }

    @Test
    fun `newline and semicolon also separate senders`() {
        assertTrue(matcher.matches(Fixtures.filter(senders = "A1\nB2;C3"), "C3", "x"))
    }

    @Test
    fun `keyword is case insensitive and both filters must match`() {
        val filter = Fixtures.filter(senders = "VCB", keyword = "otp")

        assertTrue(matcher.matches(filter, "VCB", "Your OTP is 1"))
        assertFalse(matcher.matches(filter, "VCB", "Balance changed"))
        assertFalse(matcher.matches(filter, "TCB", "Your OTP is 1"))
    }

    @Test
    fun `regex mode searches sender and body`() {
        val filter = Fixtures.filter(senders = "^(VCB|TCB)$", keyword = "otp\\s\\d{6}", mode = MatchMode.REGEX)

        assertTrue(matcher.matches(filter, "tcb", "OTP 123456"))
        assertFalse(matcher.matches(filter, "VCBX", "OTP 123456"))
        assertFalse(matcher.matches(filter, "VCB", "OTP 12345"))
    }

    @Test
    fun `invalid regex never matches`() {
        assertFalse(matcher.matches(Fixtures.filter(keyword = "([", mode = MatchMode.REGEX), "a", "(["))
    }
}
