package com.receiver.sms.features.settings.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.receiver.sms.BuildConfig
import com.receiver.sms.R
import com.receiver.sms.core.permission.rememberPermissionStatus
import com.receiver.sms.core.theme.CodeFontFamily
import com.receiver.sms.core.theme.Dimens
import com.receiver.sms.core.ui.KeyValueRow
import com.receiver.sms.core.ui.LoadingState
import com.receiver.sms.core.ui.PermissionSetupCard
import com.receiver.sms.core.ui.ScreenLevel
import com.receiver.sms.core.ui.ScreenScaffold
import com.receiver.sms.core.ui.SectionCard
import com.receiver.sms.features.dispatch.domain.service.TemplateVariables
import com.receiver.sms.features.settings.domain.model.AppSettings
import com.receiver.sms.features.settings.domain.model.RetentionPeriod

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val settings: AppSettings? by viewModel.settings.collectAsStateWithLifecycle()

    ScreenScaffold(title = stringResource(R.string.settings_title), level = ScreenLevel.TOP) { padding ->
        val current: AppSettings = settings ?: run {
            LoadingState(modifier = Modifier.padding(padding))
            return@ScreenScaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.screenGutter),
            verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
        ) {
            SectionCard(title = stringResource(R.string.settings_section_forwarding)) {
                SwitchRow(R.string.settings_forwarding, R.string.settings_forwarding_desc, current.forwardingEnabled, viewModel::onForwardingChange)
                SwitchRow(R.string.settings_keep_alive, R.string.settings_keep_alive_desc, current.keepAliveEnabled, viewModel::onKeepAliveChange)
                SwitchRow(R.string.settings_notify, R.string.settings_notify_desc, current.notifyOnFailure, viewModel::onNotifyOnFailureChange)
            }
            PermissionSetupCard(rememberPermissionStatus())
            SectionCard(title = stringResource(R.string.settings_retention)) {
                Text(
                    text = stringResource(R.string.settings_retention_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    RetentionPeriod.entries.forEachIndexed { index, period ->
                        SegmentedButton(
                            selected = current.retention == period,
                            onClick = { viewModel.onRetentionChange(period) },
                            shape = SegmentedButtonDefaults.itemShape(index, RetentionPeriod.entries.size),
                            icon = {},
                        ) {
                            Text(text = retentionLabel(period), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            PlaceholderHelpCard()
            SectionCard(title = stringResource(R.string.settings_about)) {
                KeyValueRow(stringResource(R.string.settings_version), BuildConfig.VERSION_NAME)
            }
        }
    }
}

@Composable
private fun SwitchRow(@StringRes title: Int, @StringRes description: Int, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.inlineGap)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(title), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun PlaceholderHelpCard() {
    SectionCard(title = stringResource(R.string.settings_placeholders)) {
        TemplateVariables.ALL.forEach { name ->
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.inlineGap)) {
                Text(
                    text = TemplateVariables.token(name),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = CodeFontFamily),
                    modifier = Modifier.weight(0.45f),
                )
                Text(
                    text = stringResource(placeholderDescription(name)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.55f),
                )
            }
        }
    }
}

@StringRes
private fun placeholderDescription(name: String): Int = when (name) {
    TemplateVariables.SENDER -> R.string.placeholder_sender
    TemplateVariables.BODY -> R.string.placeholder_body
    TemplateVariables.RECEIVED_AT -> R.string.placeholder_received_at
    TemplateVariables.RECEIVED_AT_ISO -> R.string.placeholder_received_at_iso
    TemplateVariables.SIM -> R.string.placeholder_sim
    else -> R.string.placeholder_config_name
}

@Composable
private fun retentionLabel(period: RetentionPeriod): String =
    period.days?.let { pluralStringResource(R.plurals.settings_retention_days, it, it) } ?: stringResource(R.string.settings_retention_forever)
