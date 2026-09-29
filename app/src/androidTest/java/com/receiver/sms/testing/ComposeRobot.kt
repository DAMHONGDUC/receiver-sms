package com.receiver.sms.testing

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry

private const val WAIT_MILLIS = 10_000L

/** Small vocabulary for driving the app by what the user sees, in the device language. */
@OptIn(ExperimentalTestApi::class)
class ComposeRobot(private val rule: ComposeTestRule) {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    fun string(@StringRes res: Int, vararg args: Any): String = context.getString(res, *args)

    fun tab(@StringRes label: Int) {
        clickable(string(label)).performClick()
        rule.waitForIdle()
    }

    fun clickable(text: String, substring: Boolean = false): SemanticsNodeInteraction {
        rule.waitUntilAtLeastOneExists(hasText(text, substring = substring) and hasClickAction(), WAIT_MILLIS)
        return rule.onAllNodes(hasText(text, substring = substring) and hasClickAction())[0]
    }

    fun tap(@StringRes label: Int) {
        clickable(string(label)).performClick()
        rule.waitForIdle()
    }

    fun type(@StringRes fieldLabel: Int, value: String) {
        rule.onAllNodes(hasSetTextAction() and hasText(string(fieldLabel)))[0].performTextReplacement(value)
    }

    fun waitFor(text: String, substring: Boolean = false) {
        rule.waitUntilAtLeastOneExists(hasText(text, substring = substring), WAIT_MILLIS)
    }

    fun waitFor(@StringRes res: Int) = waitFor(string(res))

    fun assertAbsent(text: String) {
        rule.waitForIdle()
        check(rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isEmpty()) { "expected no node with text '$text'" }
    }
}
