package com.receiver.sms.features.apiconfig.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.receiver.sms.R
import com.receiver.sms.core.theme.Dimens
import com.receiver.sms.core.ui.LoadingState
import com.receiver.sms.core.ui.ScreenLevel
import com.receiver.sms.core.ui.ScreenScaffold

@Composable
fun ApiEditorScreen(
    onNavigateUp: () -> Unit,
    viewModel: ApiEditorViewModel = hiltViewModel(),
) {
    val state: ApiEditorState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    var showTestDialog: Boolean by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ApiEditorEvent.Saved -> onNavigateUp()
                is ApiEditorEvent.Message -> snackbarHostState.showSnackbar(
                    resources.getString(event.message.res, *event.message.args.toTypedArray())
                )
            }
        }
    }

    ScreenScaffold(
        title = stringResource(if (viewModel.isNew) R.string.editor_title_new else R.string.editor_title_edit),
        level = ScreenLevel.DETAIL,
        onNavigateUp = onNavigateUp,
        snackbarHostState = snackbarHostState,
        actions = {
            IconButton(onClick = { showTestDialog = true }, enabled = !state.loading) {
                Icon(Icons.Filled.Science, contentDescription = stringResource(R.string.editor_test))
            }
            IconButton(onClick = viewModel::onSave, enabled = !state.loading && !state.saving) {
                Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.action_save))
            }
        },
    ) { padding ->
        if (state.loading) {
            LoadingState(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.screenGutter),
                verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
            ) {
                GeneralSection(state, viewModel)
                RequestSection(state, viewModel)
                TriggerSection(state, viewModel)
                DeliverySection(state, viewModel)
            }
        }
    }

    if (showTestDialog) {
        TestCallDialog(
            testCall = state.testCall,
            onRun = viewModel::onRunTest,
            onDismiss = {
                showTestDialog = false
                viewModel.onDismissTest()
            },
        )
    }
}
