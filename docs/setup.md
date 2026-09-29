# Setup

## Requirements

| Tool | Version | Source |
|---|---|---|
| JDK | 17+ | [app/build.gradle.kts](../app/build.gradle.kts) `JvmTarget.JVM_17` |
| Android SDK platform | 37 | `compileSdk { version = release(37) }` |
| Gradle | 9.8.0 (wrapper) | [gradle-wrapper.properties](../gradle/wrapper/gradle-wrapper.properties) |

## Commands

| Task | Command |
|---|---|
| Build debug APK | `./gradlew :app:assembleDebug` |
| Install on device | `./gradlew :app:installDebug` |
| Build release APK (R8) | `./gradlew :app:assembleRelease` |
| Unit tests | `./gradlew :app:testDebugUnitTest` |
| One test class | `./gradlew :app:testDebugUnitTest --tests '*SmsMatcherTest*'` |
| Instrumented tests (emulator running) | `./gradlew :app:connectedDebugAndroidTest` |
| One instrumented class | `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.receiver.sms.e2e.AppFlowTest` |
| Lint | `./gradlew :app:lintDebug` |

## Test suites

| Suite | Location | Covers |
|---|---|---|
| Unit (JVM) | [app/src/test](../app/src/test/java/com/receiver/sms) | Domain logic, use cases, mappers, settings DataStore, ViewModels |
| Unit (Robolectric) | Same folder, `@RunWith(RobolectricTestRunner::class)` | ViewModels that read navigation route args |
| Room queries | [DaoTest.kt](../app/src/androidTest/java/com/receiver/sms/data/DaoTest.kt) | Every DAO query on real SQLite |
| Worker | [ApiCallWorkerTest.kt](../app/src/androidTest/java/com/receiver/sms/work/ApiCallWorkerTest.kt) | Outcome → WorkManager result, input data, attempt number |
| End to end | [AppFlowTest.kt](../app/src/androidTest/java/com/receiver/sms/e2e/AppFlowTest.kt) | Real app with Hilt: editor, test request, SMS → WorkManager → HTTP, retry timeline, filters, delete, settings |

## Local config (`local.properties`, gitignored)

| Key | Required | Purpose |
|---|---|---|
| `sdk.dir` | Yes (Android Studio writes it) | Android SDK path |
| `storeFile` | No | Release keystore path; release build is unsigned without it |
| `storePassword` | No | Keystore password |
| `keyAlias` | No | Key alias |
| `keyPassword` | No | Key password |

## First run on a device

| Step | Action |
|---|---|
| 1 | Install the debug build |
| 2 | On Dashboard, "Finish setup" card: allow **Receive SMS**, **Notifications**, **Unrestricted battery** |
| 3 | APIs tab → **New API**, fill URL and rules |
| 4 | Press the flask icon to send a test request, then save |
| 5 | Optional: Settings → **Keep running in background** |

## Test on an emulator

| Step | Command / value |
|---|---|
| Webhook URL for a server on the host machine | `http://10.0.2.2:<port>/...` |
| Send a fake SMS | `adb emu sms send 0901234567 "Your OTP is 123456"` |
| Follow app logs | `adb logcat \| grep SmsFwd/` |
