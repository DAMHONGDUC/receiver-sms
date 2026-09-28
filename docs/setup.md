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
| Lint | `./gradlew :app:lintDebug` |

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
