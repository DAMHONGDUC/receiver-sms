package com.receiver.sms.features.calllog.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.receiver.sms.R
import com.receiver.sms.core.theme.AppThemeExtras
import com.receiver.sms.core.theme.CodeFontFamily
import com.receiver.sms.core.theme.Dimens
import com.receiver.sms.core.time.TimeUtils
import com.receiver.sms.core.ui.CodeBlock
import com.receiver.sms.core.ui.ConfirmDialog
import com.receiver.sms.core.ui.EmptyState
import com.receiver.sms.core.ui.IconBadge
import com.receiver.sms.core.ui.currentLocale
import com.receiver.sms.core.ui.KeyValueRow
import com.receiver.sms.core.ui.LoadingState
import com.receiver.sms.core.ui.MethodTag
import com.receiver.sms.core.ui.ScreenLevel
import com.receiver.sms.core.ui.ScreenScaffold
import com.receiver.sms.core.ui.SectionCard
import com.receiver.sms.core.ui.UiFormat
import com.receiver.sms.core.ui.rememberCopyAction
import com.receiver.sms.features.calllog.domain.model.CallLog
import com.receiver.sms.features.calllog.domain.model.CallStatus
import com.receiver.sms.features.calllog.domain.model.CallTrigger
import com.receiver.sms.features.calllog.presentation.components.TriggerPill

@Composable
fun CallDetailScreen(
    onNavigateUp: () -> Unit,
    onEditApi: (Long) -> Unit,
    viewModel: CallDetailViewModel = hiltViewModel(),
) {
    val state: CallDetailState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    var confirmDelete: Boolean by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                CallDetailEvent.Deleted -> onNavigateUp()
                is CallDetailEvent.Message -> snackbarHostState.showSnackbar(resources.getString(event.message.res))
            }
        }
    }

    ScreenScaffold(
        title = stringResource(R.string.detail_title),
        level = ScreenLevel.DETAIL,
        onNavigateUp = onNavigateUp,
        snackbarHostState = snackbarHostState,
        actions = {
            if (state is CallDetailState.Loaded) {
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                }
            }
        },
    ) { padding ->
        when (val current: CallDetailState = state) {
            CallDetailState.Loading -> LoadingState(modifier = Modifier.padding(padding))
            CallDetailState.NotFound -> EmptyState(
                icon = Icons.Filled.SearchOff,
                title = stringResource(R.string.detail_not_found),
                message = stringResource(R.string.detail_not_found_message),
                modifier = Modifier.padding(padding),
            )
            is CallDetailState.Loaded -> CallDetailContent(
                log = current.log,
                onRetry = viewModel::onRetry,
                onEditApi = onEditApi,
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = stringResource(R.string.detail_delete_title),
            message = stringResource(R.string.detail_delete_message),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = viewModel::onDelete,
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun CallDetailContent(log: CallLog, onRetry: () -> Unit, onEditApi: (Long) -> Unit, modifier: Modifier) {
    val copy: (String) -> Unit = rememberCopyAction()
    val canRetry: Boolean = log.trigger != CallTrigger.TEST && log.configId != null && log.smsId != null
    val success: Boolean = log.status == CallStatus.SUCCESS
    val statusColor: Color = if (success) AppThemeExtras.statusColors.success else AppThemeExtras.statusColors.failure

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Dimens.screenGutter),
        verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
    ) {
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.cardPadding)) {
                IconBadge(
                    icon = if (success) Icons.Filled.Check else Icons.Filled.PriorityHigh,
                    size = Dimens.badgeLarge,
                    containerColor = statusColor,
                    contentColor = MaterialTheme.colorScheme.surface,
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.smallGap)) {
                    Text(
                        text = stringResource(if (success) R.string.status_success else R.string.status_failed) +
                            (log.responseCode?.let { " · HTTP $it" } ?: ""),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(text = log.configName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TriggerPill(log.trigger)
            }
            KeyValueRow(stringResource(R.string.detail_time), TimeUtils.formatDateTime(log.createdAt, currentLocale()))
            KeyValueRow(stringResource(R.string.detail_duration), UiFormat.duration(log.durationMs))
            KeyValueRow(stringResource(R.string.detail_attempt), log.attempt.toString())
            if (log.errorMessage != null) KeyValueRow(stringResource(R.string.detail_error), log.errorMessage)
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.inlineGap)) {
                if (canRetry) {
                    Button(onClick = onRetry) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Text(text = stringResource(R.string.detail_retry), style = MaterialTheme.typography.labelLarge)
                    }
                }
                val configId: Long? = log.configId
                if (configId != null) {
                    OutlinedButton(onClick = { onEditApi(configId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                        Text(text = stringResource(R.string.detail_edit_api), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        SectionCard(title = stringResource(R.string.detail_sms), icon = Icons.Filled.Sms) {
            KeyValueRow(stringResource(R.string.detail_sender), log.smsSender)
            CodeBlock(label = stringResource(R.string.detail_message), text = log.smsBody, onCopy = copy)
        }
        SectionCard(title = stringResource(R.string.detail_request), icon = Icons.Filled.Upload) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.inlineGap)) {
                MethodTag(log.method)
                SelectionContainer(modifier = Modifier.fillMaxWidth()) {
                    Text(text = log.url, style = MaterialTheme.typography.bodySmall.copy(fontFamily = CodeFontFamily))
                }
            }
            if (log.requestHeaders.isNotEmpty()) {
                CodeBlock(
                    label = stringResource(R.string.detail_request_headers),
                    text = log.requestHeaders.joinToString("\n") { "${it.name}: ${it.value}" },
                    onCopy = copy,
                )
            }
            CodeBlock(label = stringResource(R.string.detail_request_body), text = log.requestBody, onCopy = copy)
        }
        SectionCard(title = stringResource(R.string.detail_response), icon = Icons.Filled.Download) {
            CodeBlock(
                label = stringResource(R.string.detail_response_body),
                text = log.responseBody ?: log.errorMessage.orEmpty(),
                onCopy = copy,
            )
        }
    }
}
