package com.receiver.sms.features.dispatch.domain.service

import com.receiver.sms.features.calllog.domain.model.CallLog
import com.receiver.sms.features.calllog.domain.model.CallTrigger
import com.receiver.sms.features.dispatch.domain.model.HttpRequestSpec
import com.receiver.sms.features.dispatch.domain.model.HttpResult

/** Sends a rendered request. Never throws: transport errors come back as [HttpResult.Failure]. */
interface HttpExecutor {
    suspend fun execute(request: HttpRequestSpec, timeoutSeconds: Int): HttpResult
}

/** Queues a durable, retrying API call for one config and one received SMS. */
interface CallScheduler {
    fun enqueue(configId: Long, smsId: Long, trigger: CallTrigger)
}

interface FailureNotifier {
    fun notifyFailure(log: CallLog)
}

/** Starts or stops the foreground service that keeps the process alive. */
interface KeepAliveController {
    fun start()

    fun stop()
}
