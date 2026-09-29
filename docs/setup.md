# Setup

## Requirements

| Tool | Version | Source |
|---|---|---|
| JDK | 17+ | [app/build.gradle.kts](../app/build.gradle.kts) `JvmTarget.JVM_17` |
| Android SDK platform | 37 | `compileSdk { version = release(37) }` |
| Gradle | 9.8.0 (wrapper) | [gradle-wrapper.properties](../gradle/wrapper/gradle-wrapper.properties) |

## Commands

`make` wraps the common commands (targets from [packages/script-tools](../packages/script-tools/android/android.mk), settings in [script-tools.properties](../script-tools.properties)); run `make` alone to list them.

| Task | Command |
|---|---|
| Fetch the script-tools submodule (after clone) | `make setup` |
| Pull the latest script-tools, then commit the new pointer | `make tools-update` |
| Build debug APK | `make build` |
| Install on device | `make install` |
| Build prod release APK (R8) | `./gradlew :app:assembleProdRelease` |
| Signed release APK into `Release/` (clean, unit tests, build, verify) | `make apk` (`FLAVOR=dev` for dev) |
| Signed release AAB into `Release/` (clean, unit tests, build, verify) | `make aab` (`FLAVOR=dev` for dev) |
| Signed release AAB and APK into `Release/` | `make release` (`FLAVOR=dev` for dev) |
| Unit tests | `make test` |
| One test class | `make test TEST=SmsMatcherTest` |
| Instrumented tests (emulator running) | `./gradlew :app:connectedDevDebugAndroidTest` |
| One instrumented class | `./gradlew :app:connectedDevDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.dd.sms.hook.e2e.AppFlowTest` |
| Lint | `make lint` |
| Fake SMS on emulator | `make sms SENDER=<sender> BODY="<body>"` |

## Test suites

| Suite | Location | Covers |
|---|---|---|
| Unit (JVM) | [app/src/test](../app/src/test/java/com/dd/sms/hook) | Domain logic, use cases, mappers, settings DataStore, ViewModels |
| Unit (Robolectric) | Same folder, `@RunWith(RobolectricTestRunner::class)` | ViewModels that read navigation route args |
| Room queries | [DaoTest.kt](../app/src/androidTest/java/com/dd/sms/hook/data/DaoTest.kt) | Every DAO query on real SQLite |
| Worker | [ApiCallWorkerTest.kt](../app/src/androidTest/java/com/dd/sms/hook/work/ApiCallWorkerTest.kt) | Outcome → WorkManager result, input data, attempt number |
| End to end | [AppFlowTest.kt](../app/src/androidTest/java/com/dd/sms/hook/e2e/AppFlowTest.kt) | Real app with Hilt: editor, test request, SMS → WorkManager → HTTP, retry timeline, filters, delete, settings |

## Flavors

| Flavor | applicationId | Launcher label | Env file | In-app tag |
|---|---|---|---|---|
| `dev` | `com.dd.sms.hook.dev` (+ `.debug` for debug) | SMS Hook Dev | `env/env.dev.properties` | `DEV` pill next to every screen title |
| `prod` | `com.dd.sms.hook` (+ `.debug` for debug) | SMS Hook | `env/env.prod.properties` | None |

## Build config (`env/`)

All build config lives in `env/`, split by sensitivity; only `env/version.properties` is committed, the tables below are the key reference.

| File | Holds |
|---|---|
| `env/version.properties` | App version for every build, committed |
| `env/env.dev.properties` | Dev flavor values, not secret |
| `env/env.prod.properties` | Prod flavor values, not secret |
| `env/key.properties` | Debug and release signing keys, shared by both flavors |
| `env/debug.keystore` | Debug keystore (copy of `~/.android/debug.keystore`) |
| `env/release.jks` | Release keystore |

| Step | Action |
|---|---|
| 1 | Create `env/env.dev.properties` and `env/env.prod.properties` with the `ENV` key |
| 2 | Debug keystore: `cp ~/.android/debug.keystore env/debug.keystore` |
| 3 | Release keystore: `keytool -genkeypair -v -storetype PKCS12 -keystore env/release.jks -alias sms-hook -keyalg RSA -keysize 4096 -validity 10000` |
| 4 | Create `env/key.properties` with the `DEBUG_*` and `RELEASE_*` keys below |
| 5 | Bump `versionCode` (and `versionName`) in `env/version.properties` |
| 6 | Run `make apk` or `make aab` → signed `sms-hook-<flavor>-<versionName>-<versionCode>.<apk\|aab>` in `Release/` |

| Key | File | Required | Purpose |
|---|---|---|---|
| `versionName` | `version.properties` | Yes | Version label users see |
| `versionCode` | `version.properties` | Yes | Integer; must increase on every Play upload |
| `ENV` | `env.dev.properties` / `env.prod.properties` | No | Environment label (defaults to the flavor name); shown next to the version in Settings → About, and as the tag on every screen in dev |
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
| `env/*` except `version.properties` | Real values and keystores never reach git |
| `Release/*.aab` | AABs are uploaded to the store, only APKs are kept in git |
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
| Follow app logs | `adb logcat \| grep SmsHook/` |
