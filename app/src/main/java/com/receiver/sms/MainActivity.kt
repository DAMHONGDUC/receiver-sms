package com.receiver.sms

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.receiver.sms.core.navigation.AppRoot
import com.receiver.sms.core.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /** A call log opened from a failure notification, consumed once by the nav host. */
    private val pendingCallLogId: MutableStateFlow<Long?> = MutableStateFlow(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        readDeepLink(intent)
        setContent {
            AppTheme {
                AppRoot(pendingCallLogId = pendingCallLogId)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        readDeepLink(intent)
    }

    private fun readDeepLink(intent: Intent?) {
        val id: Long = intent?.getLongExtra(EXTRA_CALL_LOG_ID, NO_ID) ?: NO_ID

        if (id != NO_ID) pendingCallLogId.value = id
    }

    companion object {
        const val EXTRA_CALL_LOG_ID = "extra_call_log_id"
        private const val NO_ID = -1L
    }
}
