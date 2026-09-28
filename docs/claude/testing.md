# Testing rules

| Rule | Reason | Example |
|---|---|---|
| Unit tests live in `app/src/test/java/com/receiver/sms/features/<feature>/` | Mirrors source | `features/dispatch/SmsMatcherTest.kt` |
| Shared builders in `testing/Fixtures.kt`, never redeclared per file | One setup helper | `Fixtures.config()`, `Fixtures.sms()` |
| Run only the changed test class | Fast feedback | `./gradlew :app:testDebugUnitTest --tests '*RequestFactoryTest*'` |
| Mocks with MockK, coroutines with `runTest`, HTTP with `mockwebserver3.MockWebServer` | Libraries already in the catalog | `OkHttpExecutorTest` |
| Pure domain logic (matching, templating, retry, aggregation) must have tests with edge cases | Core behaviour | `ExecuteQueuedCallUseCaseTest` |
| Break the code once to see a new test fail | Test must be able to fail | - |
| Lint must report zero errors before done | Release build uses lint | `./gradlew :app:lintDebug` |
