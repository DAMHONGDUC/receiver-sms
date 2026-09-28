package com.receiver.sms.features.apiconfig

import com.receiver.sms.features.apiconfig.domain.model.HeaderEntry
import com.receiver.sms.features.apiconfig.domain.model.MatchMode
import com.receiver.sms.features.apiconfig.domain.service.ApiConfigError
import com.receiver.sms.features.apiconfig.domain.service.ApiConfigValidator
import com.receiver.sms.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiConfigValidatorTest {
    private val validator = ApiConfigValidator()

    @Test
    fun `valid config has no errors, templated url included`() {
        assertEquals(emptySet<ApiConfigError>(), validator.validate(Fixtures.config(url = "https://x.io/{{sender}}?b={{body}}")))
    }

    @Test
    fun `reports every broken field`() {
        val config = Fixtures.config(
            url = "ftp://x.io",
            filter = Fixtures.filter(senders = "(", keyword = "[", mode = MatchMode.REGEX),
        ).copy(name = " ", headers = listOf(HeaderEntry("", "v")), timeoutSeconds = 0, maxRetries = 11)

        assertEquals(ApiConfigError.entries.toSet(), validator.validate(config))
    }

    @Test
    fun `regex syntax is not checked in contains mode`() {
        assertEquals(emptySet<ApiConfigError>(), validator.validate(Fixtures.config(filter = Fixtures.filter(senders = "("))))
    }

    @Test
    fun `url without host is invalid`() {
        assertEquals(setOf(ApiConfigError.URL_INVALID), validator.validate(Fixtures.config(url = "https://")))
    }
}
