package com.dd.sms.hook.features.apiconfig

import app.cash.turbine.test
import com.dd.sms.hook.R
import com.dd.sms.hook.features.apiconfig.domain.usecase.DeleteApiConfigUseCase
import com.dd.sms.hook.features.apiconfig.domain.usecase.DuplicateApiConfigUseCase
import com.dd.sms.hook.features.apiconfig.domain.usecase.ObserveApiConfigsUseCase
import com.dd.sms.hook.features.apiconfig.domain.usecase.SetApiConfigEnabledUseCase
import com.dd.sms.hook.features.apiconfig.presentation.list.ApiListState
import com.dd.sms.hook.features.apiconfig.presentation.list.ApiListViewModel
import com.dd.sms.hook.testing.FakeApiConfigRepository
import com.dd.sms.hook.testing.Fixtures
import com.dd.sms.hook.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ApiListViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repository = FakeApiConfigRepository(listOf(Fixtures.config(id = 1L)))
    private val vm by lazy {
        ApiListViewModel(
            ObserveApiConfigsUseCase(repository),
            SetApiConfigEnabledUseCase(repository),
            DeleteApiConfigUseCase(repository),
            DuplicateApiConfigUseCase(repository),
        )
    }

    @Test
    fun `list reflects the repository`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect { } }

        assertEquals(listOf(1L), (vm.state.value as ApiListState.Loaded).configs.map { it.id })
    }

    @Test
    fun `toggle disables the config`() = runTest {
        vm.onToggle(repository.configs.value.single(), false)

        assertFalse(repository.configs.value.single().enabled)
    }

    @Test
    fun `duplicate and delete report a message with the name`() = runTest {
        val config = repository.configs.value.single()

        vm.messages.test {
            vm.onDuplicate(config, "(copy)")
            assertEquals(R.string.api_list_duplicated, awaitItem().res)
            vm.onDelete(config)
            val deleted = awaitItem()
            assertEquals(R.string.api_list_deleted, deleted.res)
            assertEquals(listOf<Any>("Bank hook"), deleted.args)
        }
        assertEquals(listOf("Bank hook (copy)"), repository.configs.value.map { it.name })
    }
}
