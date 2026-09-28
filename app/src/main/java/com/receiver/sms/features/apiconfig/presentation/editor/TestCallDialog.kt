package com.receiver.sms.features.apiconfig.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.receiver.sms.R
import com.receiver.sms.core.theme.Dimens
import com.receiver.sms.core.ui.CodeBlock
import com.receiver.sms.core.ui.PillTone
import com.receiver.sms.core.ui.StatusPill
import com.receiver.sms.core.ui.UiFormat
import com.receiver.sms.core.ui.rememberCopyAction
import com.receiver.sms.features.calllog.domain.model.CallLog
import com.receiver.sms.features.calllog.domain.model.CallStatus

@Composable
internal fun TestCallDialog(
    testCall: TestCallState,
    onRun: (sender: String, body: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val defaultSender: String = stringResource(R.string.test_default_sender)
    val defaultBody: String = stringResource(R.string.test_default_body)
    var sender: String by rememberSaveable { mutableStateOf(defaultSender) }
    var body: String by rememberSaveable { mutableStateOf(defaultBody) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.test_title), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.inlineGap),
            ) {
                OutlinedTextField(
                    value = sender,
                    onValueChange = { sender = it },
                    label = { Text(text = stringResource(R.string.test_sender), style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text(text = stringResource(R.string.test_body), style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.fillMaxWidth(),
                )
                when (testCall) {
                    TestCallState.Idle -> Unit
                    TestCallState.Running -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    is TestCallState.Done -> TestResult(testCall.log)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onRun(sender, body) }, enabled = testCall != TestCallState.Running) {
                Text(text = stringResource(R.string.test_send), style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_close), style = MaterialTheme.typography.labelLarge)
            }
        },
    )
}

@Composable
private fun TestResult(log: CallLog) {
    val success: Boolean = log.status == CallStatus.SUCCESS
    val copy: (String) -> Unit = rememberCopyAction()

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.inlineGap)) {
        StatusPill(
            label = log.responseCode?.let { "HTTP $it" } ?: stringResource(R.string.status_failed),
            tone = if (success) PillTone.SUCCESS else PillTone.FAILURE,
        )
        Text(text = UiFormat.duration(log.durationMs), style = MaterialTheme.typography.bodyMedium)
    }
    CodeBlock(label = stringResource(R.string.detail_request_body), text = log.requestBody, onCopy = copy)
    CodeBlock(
        label = stringResource(R.string.detail_response_body),
        text = log.responseBody ?: log.errorMessage.orEmpty(),
        onCopy = copy,
    )
}
