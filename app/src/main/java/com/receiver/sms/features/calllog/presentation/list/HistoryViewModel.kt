package com.receiver.sms.features.calllog.presentation.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.receiver.sms.R
import com.receiver.sms.core.logging.AppLogger
import com.receiver.sms.core.navigation.HistoryRoute
import com.receiver.sms.core.ui.UiMessage
import com.receiver.sms.features.calllog.domain.model.CallLog
import com.receiver.sms.features.calllog.domain.model.CallLogFilter
import com.receiver.sms.features.calllog.domain.model.CallStatus
import com.receiver.sms.features.calllog.domain.usecase.ClearCallLogsUseCase
import com.receiver.sms.features.calllog.domain.usecase.ObserveCallLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HistoryViewModel"
private const val STOP_TIMEOUT_MILLIS = 5_000L
private const val SEARCH_DEBOUNCE_MILLIS = 250L

sealed interface HistoryState {
    data object Loading : HistoryState
    data class Loaded(val logs: List<CallLog>) : HistoryState
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCallLogs: ObserveCallLogsUseCase,
    private val clearCallLogs: ClearCallLogsUseCase,
) : ViewModel() {
    private val configId: Long? = savedStateHandle.toRoute<HistoryRoute>().configId
        .takeIf { it != HistoryRoute.ALL_CONFIGS }
    private val mutableFilter: MutableStateFlow<CallLogFilter> =
        MutableStateFlow(CallLogFilter.ALL.copy(configId = configId))
    private val messageChannel: Channel<UiMessage> = Channel(Channel.BUFFERED)

    val filter: StateFlow<CallLogFilter> = mutableFilter.asStateFlow()
    val messages: Flow<UiMessage> = messageChannel.receiveAsFlow()
    val isScopedToApi: Boolean = configId != null
    val state: StateFlow<HistoryState> = mutableFilter
        .debounce(SEARCH_DEBOUNCE_MILLIS)
        .flatMapLatest { observeCallLogs(it) }
        .map<List<CallLog>, HistoryState> { HistoryState.Loaded(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), HistoryState.Loading)

    fun onStatusFilter(status: CallStatus?) {
        AppLogger.i(TAG, "status filter - {status: $status}")
        mutableFilter.update { it.copy(status = status) }
    }

    fun onQueryChange(query: String) {
        mutableFilter.update { it.copy(query = query) }
    }

    fun onClearAll() {
        AppLogger.i(TAG, "clear all confirmed")
        viewModelScope.launch {
            try {
                clearCallLogs()
                messageChannel.send(UiMessage(R.string.history_cleared))
            } catch (e: Exception) {
                AppLogger.e(TAG, "clear failed", e)
                messageChannel.send(UiMessage(R.string.error_generic))
            }
        }
    }
}
