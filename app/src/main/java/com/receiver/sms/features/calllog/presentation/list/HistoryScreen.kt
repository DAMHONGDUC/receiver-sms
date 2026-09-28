package com.receiver.sms.features.calllog.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.receiver.sms.R
import com.receiver.sms.core.theme.Dimens
import com.receiver.sms.core.ui.ConfirmDialog
import com.receiver.sms.core.ui.EmptyState
import com.receiver.sms.core.ui.LoadingState
import com.receiver.sms.core.ui.MessageEffect
import com.receiver.sms.core.ui.ScreenLevel
import com.receiver.sms.core.ui.ScreenScaffold
import com.receiver.sms.features.calllog.domain.model.CallLogFilter
import com.receiver.sms.features.calllog.domain.model.CallStatus
import com.receiver.sms.features.calllog.presentation.components.CallLogRow

@Composable
fun HistoryScreen(
    onOpenCall: (Long) -> Unit,
    onNavigateUp: (() -> Unit)?,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state: HistoryState by viewModel.state.collectAsStateWithLifecycle()
    val filter: CallLogFilter by viewModel.filter.collectAsStateWithLifecycle()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    var confirmClear: Boolean by rememberSaveable { mutableStateOf(false) }

    MessageEffect(viewModel.messages, snackbarHostState)

    ScreenScaffold(
        title = stringResource(if (viewModel.isScopedToApi) R.string.history_title_api else R.string.history_title),
        level = ScreenLevel.TOP,
        onNavigateUp = onNavigateUp,
        snackbarHostState = snackbarHostState,
        actions = {
            IconButton(onClick = { confirmClear = true }) {
                Icon(Icons.Filled.DeleteSweep, contentDescription = stringResource(R.string.history_clear))
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            HistoryFilters(filter, viewModel)
            when (val current: HistoryState = state) {
                HistoryState.Loading -> LoadingState()
                is HistoryState.Loaded -> if (current.logs.isEmpty()) {
                    EmptyState(
                        icon = Icons.Filled.History,
                        title = stringResource(R.string.history_empty_title),
                        message = stringResource(R.string.history_empty_message),
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = Dimens.listItemGap)) {
                        items(current.logs, key = { it.id }) { log ->
                            CallLogRow(log = log, onClick = { onOpenCall(log.id) })
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    if (confirmClear) {
        ConfirmDialog(
            title = stringResource(R.string.history_clear),
            message = stringResource(R.string.history_clear_message),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = viewModel::onClearAll,
            onDismiss = { confirmClear = false },
        )
    }
}

@Composable
private fun HistoryFilters(filter: CallLogFilter, viewModel: HistoryViewModel) {
    Column(
        modifier = Modifier.padding(horizontal = Dimens.screenGutter),
        verticalArrangement = Arrangement.spacedBy(Dimens.smallGap),
    ) {
        OutlinedTextField(
            value = filter.query,
            onValueChange = viewModel::onQueryChange,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            placeholder = { Text(text = stringResource(R.string.history_search), style = MaterialTheme.typography.bodyMedium) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.inlineGap)) {
            StatusChip(R.string.history_filter_all, filter.status == null) { viewModel.onStatusFilter(null) }
            StatusChip(R.string.status_success, filter.status == CallStatus.SUCCESS) {
                viewModel.onStatusFilter(CallStatus.SUCCESS)
            }
            StatusChip(R.string.status_failed, filter.status == CallStatus.FAILED) {
                viewModel.onStatusFilter(CallStatus.FAILED)
            }
        }
    }
}

@Composable
private fun StatusChip(label: Int, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = stringResource(label), style = MaterialTheme.typography.labelLarge) },
    )
}
