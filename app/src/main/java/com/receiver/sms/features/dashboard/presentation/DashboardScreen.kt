package com.receiver.sms.features.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.receiver.sms.R
import com.receiver.sms.core.permission.PermissionStatus
import com.receiver.sms.core.permission.rememberPermissionStatus
import com.receiver.sms.core.theme.AppThemeExtras
import com.receiver.sms.core.theme.Dimens
import com.receiver.sms.core.ui.EmptyState
import com.receiver.sms.core.ui.LoadingState
import com.receiver.sms.core.ui.PermissionSetupCard
import com.receiver.sms.core.ui.ScreenLevel
import com.receiver.sms.core.ui.ScreenScaffold
import com.receiver.sms.core.ui.SectionCard
import com.receiver.sms.core.ui.UiFormat
import com.receiver.sms.features.calllog.presentation.components.CallLogRow
import com.receiver.sms.features.dashboard.domain.model.DashboardData
import com.receiver.sms.features.dashboard.domain.model.DashboardRange
import com.receiver.sms.features.dashboard.presentation.components.ApiBreakdownRow
import com.receiver.sms.features.dashboard.presentation.components.CallsBarChart
import com.receiver.sms.features.dashboard.presentation.components.StatTile

@Composable
fun DashboardScreen(
    onOpenCall: (Long) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onCreateApi: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state: DashboardState by viewModel.state.collectAsStateWithLifecycle()
    val range: DashboardRange by viewModel.range.collectAsStateWithLifecycle()
    val permissions: PermissionStatus = rememberPermissionStatus()

    ScreenScaffold(title = stringResource(R.string.dashboard_title), level = ScreenLevel.TOP) { padding ->
        when (val current: DashboardState = state) {
            DashboardState.Loading -> LoadingState(modifier = Modifier.padding(padding))
            DashboardState.Error -> EmptyState(
                icon = Icons.Filled.CloudOff,
                title = stringResource(R.string.dashboard_error_title),
                message = stringResource(R.string.error_generic),
                modifier = Modifier.padding(padding),
            )
            is DashboardState.Loaded -> Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.screenGutter),
                verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
            ) {
                if (!permissions.allGranted) PermissionSetupCard(permissions)
                ServiceStatusCard(current.data, onOpenSettings, onCreateApi)
                RangeSelector(range, viewModel::onRangeChange)
                StatGrid(current.data)
                SectionCard(title = stringResource(R.string.dashboard_calls_per_day)) {
                    CallsBarChart(days = current.data.daily)
                }
                TopApisCard(current.data)
                RecentCallsCard(current.data, onOpenCall, onOpenHistory)
            }
        }
    }
}

@Composable
private fun ServiceStatusCard(data: DashboardData, onOpenSettings: () -> Unit, onCreateApi: () -> Unit) {
    val active: Boolean = data.forwardingEnabled && data.enabledApis > 0
    val colors = AppThemeExtras.statusColors

    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.inlineGap)) {
            Icon(
                imageVector = if (active) Icons.Filled.PlayCircle else Icons.Filled.PauseCircle,
                contentDescription = null,
                tint = if (active) colors.success else colors.failure,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(if (active) R.string.dashboard_status_active else R.string.dashboard_status_paused),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = when {
                        !data.forwardingEnabled -> stringResource(R.string.dashboard_status_forwarding_off)
                        data.enabledApis == 0 -> stringResource(R.string.dashboard_status_no_apis)
                        else -> pluralStringResource(R.plurals.dashboard_status_detail, data.enabledApis, data.enabledApis) +
                            if (data.keepAliveEnabled) " · " + stringResource(R.string.dashboard_keep_alive_on) else ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        when {
            !data.forwardingEnabled -> TextButton(onClick = onOpenSettings) {
                Text(text = stringResource(R.string.dashboard_open_settings), style = MaterialTheme.typography.labelLarge)
            }
            data.enabledApis == 0 -> FilledTonalButton(onClick = onCreateApi) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text(text = stringResource(R.string.api_list_new), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun RangeSelector(range: DashboardRange, onChange: (DashboardRange) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        DashboardRange.entries.forEachIndexed { index, item ->
            SegmentedButton(
                selected = item == range,
                onClick = { onChange(item) },
                shape = SegmentedButtonDefaults.itemShape(index, DashboardRange.entries.size),
            ) {
                Text(text = pluralStringResource(R.plurals.dashboard_range_days, item.days, item.days), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun StatGrid(data: DashboardData) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.listItemGap)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.listItemGap)) {
            StatTile(stringResource(R.string.stat_sms_received), data.smsReceived.toString(), Modifier.weight(1f))
            StatTile(stringResource(R.string.stat_api_calls), data.summary.total.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.listItemGap)) {
            StatTile(
                stringResource(R.string.stat_success_rate),
                if (data.summary.total == 0) "—" else UiFormat.percent(data.summary.successRate),
                Modifier.weight(1f),
            )
            StatTile(stringResource(R.string.stat_avg_latency), UiFormat.duration(data.summary.avgDurationMs), Modifier.weight(1f))
        }
    }
}

@Composable
private fun TopApisCard(data: DashboardData) {
    val maxTotal: Int = data.topApis.maxOfOrNull { it.total } ?: 0
    val deletedName: String = stringResource(R.string.dashboard_deleted_api)

    SectionCard(title = stringResource(R.string.dashboard_top_apis)) {
        if (data.topApis.isEmpty()) {
            Text(
                text = stringResource(R.string.dashboard_no_calls),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        data.topApis.forEach { ApiBreakdownRow(item = it, maxTotal = maxTotal, fallbackName = deletedName) }
    }
}

@Composable
private fun RecentCallsCard(data: DashboardData, onOpenCall: (Long) -> Unit, onOpenHistory: () -> Unit) {
    SectionCard(title = stringResource(R.string.dashboard_recent)) {
        if (data.recent.isEmpty()) {
            Text(
                text = stringResource(R.string.dashboard_no_calls),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        data.recent.forEach { CallLogRow(log = it, onClick = { onOpenCall(it.id) }, horizontalPadding = 0.dp) }
        if (data.recent.isNotEmpty()) {
            TextButton(onClick = onOpenHistory) {
                Text(text = stringResource(R.string.dashboard_see_all), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
