# SMS to API

| | |
|---|---|
| Overview | Android app that calls user-configured HTTP APIs whenever an incoming SMS matches a rule, built to run unattended in the background |
| Last edit | 2026-09-29 |
| Author | Dam Hong Duc |

## Store links

| Platform | Link |
|---|---|
| Google Play | Not published |

## App IDs

| Platform | Build type | ID type | Value |
|---|---|---|---|
| Android | release | applicationId | `com.receiver.sms` |
| Android | debug | applicationId | `com.receiver.sms.debug` |
| Android | - | namespace | `com.receiver.sms` |

## Tech stack

| Category | Technology | Version |
|---|---|---|
| Build | Gradle / Android Gradle Plugin (built-in Kotlin) | 9.8.0 / 9.4.1 |
| Language | Kotlin / KSP | 2.4.20 / 2.3.12 |
| SDK | compileSdk / targetSdk / minSdk | 37 / 37 / 26 |
| UI | Jetpack Compose BOM, Material 3 | 2026.09.00 |
| Navigation | Navigation Compose (type-safe routes) | 2.10.2 |
| Architecture | MVVM + clean architecture, AndroidX Lifecycle | 2.11.0 |
| DI | Hilt / androidx.hilt | 2.60.1 / 1.4.0 |
| Local DB | Room | 2.8.5 |
| Settings | DataStore Preferences | 1.2.1 |
| Background work | WorkManager | 2.12.0 |
| Networking | OkHttp | 5.5.0 |
| Serialization | kotlinx.serialization | 1.11.0 |
| Tests | JUnit 4 / MockK / Turbine / MockWebServer | 4.13.2 / 1.14.11 / 1.2.1 / 5.5.0 |

## Project architecture

```mermaid
flowchart TD
  SMS[Incoming SMS] --> Receiver[dispatch/platform<br/>SmsReceiver]
  UI[Compose screens<br/>features/*/presentation] --> VM[ViewModels]
  VM --> UC[Use cases<br/>features/*/domain]
  Receiver --> UC
  UC --> Repo[Repository interfaces<br/>features/*/domain]
  UC --> Sched[CallScheduler]
  Sched --> Worker[dispatch/data/work<br/>ApiCallWorker]
  Worker --> UC
  Repo --> Room[(Room<br/>core/db)]
  Repo --> Store[(DataStore)]
  UC --> Http[dispatch/data/remote<br/>OkHttpExecutor]
  Http --> API[User APIs]
```

## Local database

```mermaid
erDiagram
  api_configs ||--o{ call_logs : "config_id (no FK, kept after delete)"
  received_sms ||--o{ call_logs : "sms_id (no FK)"
  api_configs {
    long id PK
    string name
    string url
    string method
    string headers_json
    string body_template
    string sender_filter
    string keyword_filter
    string match_mode
    bool enabled
    int timeout_seconds
    int max_retries
    long created_at
    long updated_at
  }
  received_sms {
    long id PK
    string sender
    string body
    long received_at
    int subscription_id
    int matched_count
  }
  call_logs {
    long id PK
    long config_id
    long sms_id
    string config_name
    string sms_sender
    string url
    string method
    int response_code
    string status
    string trigger
    int attempt
    long duration_ms
    long created_at
  }
```
