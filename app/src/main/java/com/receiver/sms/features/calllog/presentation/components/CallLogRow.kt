package com.receiver.sms.features.calllog.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.receiver.sms.R
import com.receiver.sms.core.theme.Dimens
import com.receiver.sms.core.time.TimeUtils
import com.receiver.sms.core.ui.PillTone
import com.receiver.sms.core.ui.StatusPill
import com.receiver.sms.core.ui.UiFormat
import com.receiver.sms.features.calllog.domain.model.CallLog
import com.receiver.sms.features.calllog.domain.model.CallStatus
import com.receiver.sms.features.calllog.domain.model.CallTrigger

/** Public row for a call; also used by the dashboard's recent list. */
@Composable
fun CallLogRow(
    log: CallLog,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = Dimens.screenGutter,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = Dimens.inlineGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.inlineGap),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.smallGap)) {
            Text(
                text = log.configName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.history_row_sms, log.smsSender, log.smsBody),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${TimeUtils.formatDateTime(log.createdAt)} · ${UiFormat.duration(log.durationMs)}" +
                    if (log.attempt > 1) " · " + stringResource(R.string.history_attempt, log.attempt) else "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(Dimens.smallGap)) {
            StatusPill(label = statusLabel(log), tone = if (log.status == CallStatus.SUCCESS) PillTone.SUCCESS else PillTone.FAILURE)
            if (log.trigger != CallTrigger.SMS) TriggerPill(log.trigger)
        }
    }
}

@Composable
fun statusLabel(log: CallLog): String =
    log.responseCode?.toString() ?: stringResource(
        if (log.status == CallStatus.SUCCESS) R.string.status_success else R.string.status_error
    )

@Composable
fun TriggerPill(trigger: CallTrigger) {
    val icon: ImageVector = when (trigger) {
        CallTrigger.SMS -> Icons.Filled.Sms
        CallTrigger.TEST -> Icons.Filled.Science
        CallTrigger.RETRY -> Icons.Filled.Refresh
    }

    StatusPill(label = triggerLabel(trigger), tone = PillTone.NEUTRAL, icon = icon)
}

@Composable
private fun triggerLabel(trigger: CallTrigger): String = stringResource(
    when (trigger) {
        CallTrigger.SMS -> R.string.trigger_sms
        CallTrigger.TEST -> R.string.trigger_test
        CallTrigger.RETRY -> R.string.trigger_retry
    }
)
