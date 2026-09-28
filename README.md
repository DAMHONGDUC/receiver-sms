# SMS to API

Android app that listens for incoming SMS and calls your own HTTP APIs (webhooks) when a message matches a rule. Built to run unattended on a spare phone.

## Features

- **Configurable APIs** - URL, method (GET/POST/PUT/PATCH/DELETE), headers, body template, timeout, retries, enable switch.
- **Rules per API** - sender list and message keyword, in `Contains` or `Regex` mode.
- **Templates** - `{{sender}}`, `{{body}}`, `{{received_at}}`, `{{received_at_iso}}`, `{{sim}}`, `{{config_name}}`.
  - JSON bodies are escaped automatically; URL placeholders are URL-encoded.
- **Reliable delivery** - every call is a WorkManager job: survives process death and reboots, waits for network, retries 5xx/408/425/429/network errors with exponential backoff.
- **Keep-alive mode** - optional foreground service (restarts after boot) for OEMs that kill background apps.
- **History** - every attempt with request/response, status, duration; search, status filter, manual retry, copy.
- **Dashboard** - SMS received, calls, success rate, latency, calls-per-day chart, top APIs, recent calls (7/30 days).
- **Test button** - send a request with a sample SMS from the editor before saving.
- Failure notifications, history retention (7/30/90 days/forever), English + Vietnamese.

## Stack

| Area | Library |
| --- | --- |
| Build | Gradle 9.8, AGP 9.4 (built-in Kotlin), Kotlin 2.4.20, KSP 2.3 |
| SDK | compileSdk 37, targetSdk 37, minSdk 26 |
| UI | Jetpack Compose (BOM 2026.09.00), Material 3, Navigation Compose 2.10 (type-safe routes) |
| DI | Hilt 2.60, androidx.hilt 1.4 |
| Storage | Room 2.8 (exported schemas in `app/schemas`), DataStore 1.2 |
| Background | WorkManager 2.12 |
| Network | OkHttp 5.5, kotlinx.serialization 1.11 |
| Tests | JUnit 4, MockK, coroutines-test, MockWebServer |

## Architecture

MVVM + clean architecture, grouped by feature. Inside a feature: `presentation -> domain <- data`.

```
app/src/main/java/com/receiver/sms/
  core/                 cross-cutting only: bootstrap, constants, db, di, logging, navigation, permission, theme, time, ui
  features/
    apiconfig/          API configs: model, validator, CRUD, list + editor screens
    calllog/            call history: model, stats queries, history + detail screens
    dispatch/           SMS -> API pipeline: matcher, template renderer, request factory,
                        OkHttp executor, WorkManager worker, SmsReceiver, BootReceiver, KeepAliveService
    dashboard/          analytics use case + dashboard screen and charts
    settings/           DataStore settings + settings screen
```

Flow of one SMS:

1. `SmsReceiver` (manifest-declared, works when the app is dead) joins multipart PDUs.
2. `HandleIncomingSmsUseCase` stores the SMS and matches it against enabled configs.
3. One `ApiCallWorker` is queued per match (expedited, network constraint, backoff).
4. `ExecuteQueuedCallUseCase` renders the request, sends it, logs the attempt, decides retry vs. give up (and notifies).

## Setup

1. Open the project in Android Studio (JDK 17+).
2. Run the `app` configuration, or:
   ```bash
   ./gradlew :app:installDebug
   ```
3. In the app: grant **Receive SMS**, **Notifications**, **Unrestricted battery** from the setup card.
4. Add an API, press the test (flask) button, save.

Optional release signing: add `storeFile`, `storePassword`, `keyAlias`, `keyPassword` to `local.properties`.

## Checks

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug
```

## Notes

- Cleartext `http://` is allowed on purpose so LAN webhooks work.
- A retried call stores one history row per attempt.
- Try it on an emulator: `adb emu sms send 0901234567 "Your OTP is 123456"`.
