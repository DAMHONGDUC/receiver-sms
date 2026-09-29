package com.dd.sms.hook.features.settings

import com.dd.sms.hook.features.settings.domain.model.RetentionPeriod
import com.dd.sms.hook.features.settings.domain.model.ThemeMode
import com.dd.sms.hook.features.settings.domain.usecase.ObserveSettingsUseCase
import com.dd.sms.hook.features.settings.domain.usecase.UpdateSettingsUseCase
import com.dd.sms.hook.features.settings.presentation.SettingsViewModel
import com.dd.sms.hook.testing.FakeKeepAliveController
import com.dd.sms.hook.testing.FakeSettingsRepository
import com.dd.sms.hook.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repository = FakeSettingsRepository()
    private val keepAlive = FakeKeepAliveController()
    private val vm by lazy { SettingsViewModel(ObserveSettingsUseCase(repository), UpdateSettingsUseCase(repository), keepAlive) }

    @Test
    fun `keep alive switch persists and starts or stops the service`() = runTest {
        vm.onKeepAliveChange(true)
        assertTrue(repository.current().keepAliveEnabled)
        assertTrue(keepAlive.running)

        vm.onKeepAliveChange(false)
        assertFalse(repository.current().keepAliveEnabled)
        assertFalse(keepAlive.running)
    }

    @Test
    fun `other switches are stored`() = runTest {
        vm.onForwardingChange(false)
        vm.onNotifyOnFailureChange(false)
        vm.onRetentionChange(RetentionPeriod.WEEK)
        vm.onThemeModeChange(ThemeMode.LIGHT)
        vm.onDynamicColorChange(true)

        val settings = repository.current()
        assertFalse(settings.forwardingEnabled)
        assertFalse(settings.notifyOnFailure)
        assertEquals(RetentionPeriod.WEEK, settings.retention)
        assertEquals(ThemeMode.LIGHT, settings.themeMode)
        assertTrue(settings.dynamicColor)
    }
}
