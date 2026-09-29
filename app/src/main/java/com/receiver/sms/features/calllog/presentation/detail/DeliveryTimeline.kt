package com.receiver.sms.features.calllog.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.receiver.sms.R
import com.receiver.sms.core.theme.AppThemeExtras
import com.receiver.sms.core.theme.Dimens
import com.receiver.sms.core.theme.tabularNumbers
import com.receiver.sms.core.time.TimeUtils
import com.receiver.sms.core.ui.SectionCard
import com.receiver.sms.core.ui.UiFormat
import com.receiver.sms.core.ui.currentLocale
import com.receiver.sms.features.calllog.domain.model.CallLog
import com.receiver.sms.features.calllog.domain.model.CallStatus
import com.receiver.sms.features.calllog.presentation.components.statusLabel
import java.util.Locale

private const val CURRENT_RING_ALPHA = 0.24f

/** Every attempt for this SMS and API as a vertical timeline, the opened one highlighted. */
@Composable
internal fun DeliveryTimeline(attempts: List<CallLog>, currentId: Long) {
    val locale: Locale = currentLocale()

    SectionCard(title = stringResource(R.string.detail_timeline), icon = Icons.Filled.Timeline) {
        Column {
            attempts.forEachIndexed { index, attempt ->
                val color: Color = if (attempt.status == CallStatus.SUCCESS) {
                    AppThemeExtras.statusColors.success
                } else {
                    AppThemeExtras.statusColors.failure
                }
                val current: Boolean = attempt.id == currentId

                Row(modifier = Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Dimens.rowGap)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxHeight()) {
                        Box(
                            modifier = Modifier
                                .size(Dimens.timelineDot * 2)
                                .background(if (current) color.copy(alpha = CURRENT_RING_ALPHA) else Color.Transparent, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(modifier = Modifier.size(Dimens.timelineDot).background(color, CircleShape))
                        }
                        if (index < attempts.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(Dimens.timelineLine)
                                    .weight(1f)
                                    .background(MaterialTheme.colorScheme.outlineVariant),
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.padding(bottom = if (index < attempts.lastIndex) Dimens.rowGap else Dimens.smallGap),
                        verticalArrangement = Arrangement.spacedBy(Dimens.smallGap),
                    ) {
                        Text(
                            text = stringResource(R.string.detail_timeline_attempt, attempt.attempt) + " · " + statusLabel(attempt),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = TimeUtils.formatTime(attempt.createdAt, locale) + " · " + UiFormat.duration(attempt.durationMs),
                            style = MaterialTheme.typography.bodySmall.tabularNumbers(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
