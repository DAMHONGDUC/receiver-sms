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
| Build release APK into `Release/` | `./gradlew :app:exportReleaseApk` |
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

## Build config (`env/`)

All build config lives in `env/`, split by sensitivity; only the `*.sample.properties` templates are committed.

| File | Holds | Template |
|---|---|---|
| `env/env.properties` | Environment values, not secret | [env.sample.properties](../env/env.sample.properties) |
| `env/key.properties` | Debug and release signing keys | [key.sample.properties](../env/key.sample.properties) |
| `env/debug.keystore` | Debug keystore (copy of `~/.android/debug.keystore`) | - |
| `env/release.jks` | Release keystore | - |

| Step | Action |
|---|---|
| 1 | `cp env/env.sample.properties env/env.properties` and fill it |
| 2 | Debug keystore: `cp ~/.android/debug.keystore env/debug.keystore` |
| 3 | Release keystore: `keytool -genkeypair -v -storetype PKCS12 -keystore env/release.jks -alias sms-hook -keyalg RSA -keysize 4096 -validity 10000` |
| 4 | `cp env/key.sample.properties env/key.properties` and fill it |
| 5 | Run `./gradlew :app:exportReleaseApk` → signed APK in `Release/` |

| Key | File | Required | Purpose |
|---|---|---|---|
| `ENV` | `env.properties` | No | Build label (e.g. `dev`, `prod`), shown next to the version in Settings → About |
| `DEBUG_STORE_FILE` | `key.properties` | No | Debug keystore relative to `env/`; without it debug builds use `~/.android/debug.keystore` |
| `DEBUG_STORE_PASSWORD` | `key.properties` | With `DEBUG_STORE_FILE` | `android` for the standard debug key |
| `DEBUG_KEY_ALIAS` | `key.properties` | With `DEBUG_STORE_FILE` | `androiddebugkey` for the standard debug key |
| `DEBUG_KEY_PASSWORD` | `key.properties` | With `DEBUG_STORE_FILE` | `android` for the standard debug key |
| `RELEASE_STORE_FILE` | `key.properties` | For signed release | Keystore file name, relative to `env/`; release build is unsigned without it |
| `RELEASE_STORE_PASSWORD` | `key.properties` | For signed release | Keystore password |
| `RELEASE_KEY_ALIAS` | `key.properties` | For signed release | Key alias |
| `RELEASE_KEY_PASSWORD` | `key.properties` | For signed release | Key password (same as the store password for PKCS12) |

| Gitignored | Why |
|---|---|
| `env/*` except `*.sample.properties` | Real values and keystores never reach git |
| `Release/` | Exported APKs are build output |
| `local.properties` | Only `sdk.dir`, written by Android Studio |

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
