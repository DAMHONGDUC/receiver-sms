package com.dd.sms.hook.features.apiconfig

import com.dd.sms.hook.features.apiconfig.data.local.ApiConfigEntity
import com.dd.sms.hook.features.apiconfig.data.local.ApiConfigMapper
import com.dd.sms.hook.features.apiconfig.domain.model.ApiConfig
import com.dd.sms.hook.features.apiconfig.domain.model.HeaderEntry
import com.dd.sms.hook.features.apiconfig.domain.model.HttpMethod
import com.dd.sms.hook.features.apiconfig.domain.model.MatchMode
import com.dd.sms.hook.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiConfigMapperTest {
    @Test
    fun `round trip keeps every field including headers with quotes`() {
        val config: ApiConfig = Fixtures.config(filter = Fixtures.filter("VCB", "otp", MatchMode.REGEX)).copy(
            method = HttpMethod.PATCH,
            headers = listOf(HeaderEntry("Authorization", "Bearer \"x\""), HeaderEntry("X-2", "")),
        )

        assertEquals(config, ApiConfigMapper.toDomain(ApiConfigMapper.toEntity(config)))
    }

    @Test
    fun `unknown enum values from storage fall back to safe defaults`() {
        val entity: ApiConfigEntity = ApiConfigMapper.toEntity(Fixtures.config()).copy(method = "TRACE", matchMode = "FUZZY", headersJson = "")
        val config: ApiConfig = ApiConfigMapper.toDomain(entity)

        assertEquals(HttpMethod.POST, config.method)
        assertEquals(MatchMode.CONTAINS, config.filter.mode)
        assertEquals(emptyList<HeaderEntry>(), config.headers)
    }
}
