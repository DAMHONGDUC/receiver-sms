# Testing rules

| Rule | Reason | Example |
|---|---|---|
| Unit tests live in `app/src/test/java/com/dd/sms/hook/features/<feature>/` | Mirrors source | `features/dispatch/SmsMatcherTest.kt` |
| Shared builders in `testing/Fixtures.kt` and in-memory fakes in `testing/Fakes.kt`, never redeclared per file | One setup helper | `Fixtures.log()`, `FakeCallLogRepository` |
| ViewModels that call `savedStateHandle.toRoute<>()` are tested under Robolectric with `SavedStateHandle(route = …)` | On plain JVM `toRoute` silently returns default args, so the test passes for the wrong reason | `ApiEditorViewModelTest` |
| Build ViewModels after `MainDispatcherRule` starts (inside the test or `by lazy`); collect `stateIn` flows with `backgroundScope.launch(UnconfinedTestDispatcher(testScheduler))` | Field initialisers run before rules; `WhileSubscribed` needs a collector | `ApiListViewModelTest` |
| Instrumented tests run on `HiltTestRunner`; `TestCoreModule` / `TestSettingsModule` swap in an in-memory DB and a DataStore per test | Fresh state per test, no "multiple DataStores" crash | `testing/TestModules.kt` |
| E2E rule order: `HiltAndroidRule` (0) → `WorkManagerTestRule` (1) → v2 `createAndroidComposeRule` (2); inject WorkManager users as `Provider<>` | WorkManager must exist before anything asks for it | `AppFlowTest` |
| E2E tests find UI by string resources through `ComposeRobot`, fake only the HTTP endpoint with MockWebServer | Works in any device language; tests the real stack | `AppFlowTest` |
| A UI element a test cannot reach the way a user would is a UX bug to fix in the app, not in the test | Tests double as accessibility checks | `SwitchRow` (whole row toggles) |
| Run only the changed test class | Fast feedback | `./gradlew :app:testDevDebugUnitTest --tests '*RequestFactoryTest*'` |
| Mocks with MockK, coroutines with `runTest`, HTTP with `mockwebserver3.MockWebServer` | Libraries already in the catalog | `OkHttpExecutorTest` |
| Pure domain logic (matching, templating, retry, aggregation) must have tests with edge cases | Core behaviour | `ExecuteQueuedCallUseCaseTest` |
| Break the code once to see a new test fail | Test must be able to fail | - |
| Lint must report zero errors before done | Release build uses lint | `./gradlew :app:lintDevDebug` |
