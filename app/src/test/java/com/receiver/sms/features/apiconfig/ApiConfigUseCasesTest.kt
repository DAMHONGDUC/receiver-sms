package com.receiver.sms.features.apiconfig

import com.receiver.sms.features.apiconfig.domain.model.ApiConfig
import com.receiver.sms.features.apiconfig.domain.model.ApiConfigDefaults
import com.receiver.sms.features.apiconfig.domain.model.HeaderEntry
import com.receiver.sms.features.apiconfig.domain.service.ApiConfigError
import com.receiver.sms.features.apiconfig.domain.service.ApiConfigValidator
import com.receiver.sms.features.apiconfig.domain.usecase.DeleteApiConfigUseCase
import com.receiver.sms.features.apiconfig.domain.usecase.DuplicateApiConfigUseCase
import com.receiver.sms.features.apiconfig.domain.usecase.SaveApiConfigResult
import com.receiver.sms.features.apiconfig.domain.usecase.SaveApiConfigUseCase
import com.receiver.sms.features.apiconfig.domain.usecase.SetApiConfigEnabledUseCase
import com.receiver.sms.testing.FakeApiConfigRepository
import com.receiver.sms.testing.Fixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiConfigUseCasesTest {
    private val repository = FakeApiConfigRepository()
    private val save = SaveApiConfigUseCase(repository, ApiConfigValidator())

    @Test
    fun `invalid config is rejected with its errors and nothing is stored`() = runTest {
        val result: SaveApiConfigResult = save(ApiConfigDefaults.newConfig())

        assertEquals(setOf(ApiConfigError.NAME_EMPTY, ApiConfigError.URL_INVALID), (result as SaveApiConfigResult.Invalid).errors)
        assertTrue(repository.configs.value.isEmpty())
    }

    @Test
    fun `new config is trimmed and stamped`() = runTest {
        val draft: ApiConfig = ApiConfigDefaults.newConfig().copy(
            name = "  Shop  ",
            url = " https://x.io/hook ",
            headers = listOf(HeaderEntry(" X-Key ", "v")),
        )

        val id: Long = (save(draft) as SaveApiConfigResult.Saved).id
        val stored: ApiConfig = repository.configs.value.single()

        assertEquals(id, stored.id)
        assertEquals("Shop", stored.name)
        assertEquals("https://x.io/hook", stored.url)
        assertEquals("X-Key", stored.headers.single().name)
        assertTrue(stored.createdAt > 0 && stored.createdAt == stored.updatedAt)
    }

    @Test
    fun `update keeps createdAt and bumps updatedAt`() = runTest {
        val existing: ApiConfig = Fixtures.config(id = 1L).copy(createdAt = 5L, updatedAt = 5L)
        val repo = FakeApiConfigRepository(listOf(existing))

        SaveApiConfigUseCase(repo, ApiConfigValidator())(existing.copy(name = "Renamed"))
        val stored: ApiConfig = repo.configs.value.single()

        assertEquals("Renamed", stored.name)
        assertEquals(5L, stored.createdAt)
        assertTrue(stored.updatedAt > 5L)
    }

    @Test
    fun `duplicate creates a new config with the suffix`() = runTest {
        val original: ApiConfig = Fixtures.config(id = 1L)
        val repo = FakeApiConfigRepository(listOf(original))

        val id: Long = DuplicateApiConfigUseCase(repo)(original, "(copy)")

        assertEquals(2, repo.configs.value.size)
        assertEquals("Bank hook (copy)", repo.configs.value.first { it.id == id }.name)
    }

    @Test
    fun `enable toggle and delete change only the target`() = runTest {
        val repo = FakeApiConfigRepository(listOf(Fixtures.config(id = 1L), Fixtures.config(id = 2L)))

        SetApiConfigEnabledUseCase(repo)(1L, false)
        DeleteApiConfigUseCase(repo)(2L)

        assertFalse(repo.configs.value.single().enabled)
        assertEquals(1L, repo.configs.value.single().id)
    }
}
