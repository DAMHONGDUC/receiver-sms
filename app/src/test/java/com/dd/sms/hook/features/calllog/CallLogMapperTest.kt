package com.dd.sms.hook.features.calllog

import com.dd.sms.hook.features.apiconfig.domain.model.HeaderEntry
import com.dd.sms.hook.features.calllog.data.local.CallLogMapper
import com.dd.sms.hook.features.calllog.domain.model.CallLog
import com.dd.sms.hook.features.calllog.domain.model.CallStatus
import com.dd.sms.hook.features.calllog.domain.model.CallTrigger
import com.dd.sms.hook.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class CallLogMapperTest {
    @Test
    fun `round trip keeps snapshot fields and nullable ids`() {
        val log: CallLog = Fixtures.log(id = 4L, configId = null, smsId = null, status = CallStatus.FAILED, trigger = CallTrigger.TEST)
            .copy(requestHeaders = listOf(HeaderEntry("Content-Type", "application/json")), responseCode = null, errorMessage = "timeout")

        assertEquals(log, CallLogMapper.toDomain(CallLogMapper.toEntity(log)))
    }

    @Test
    fun `unknown status reads as failed and unknown trigger as sms`() {
        val log: CallLog = CallLogMapper.toDomain(CallLogMapper.toEntity(Fixtures.log()).copy(status = "?", trigger = "?"))

        assertEquals(CallStatus.FAILED, log.status)
        assertEquals(CallTrigger.SMS, log.trigger)
    }
}
