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

| ID | Value |
|---|---|
| Android applicationId | `com.receiver.sms` |
| Android namespace | `com.receiver.sms` |

## Tech stack

| Category | Technology | Version |
|---|---|---|
| Framework | Native Android, Jetpack Compose (BOM) + Material 3 | 2026.09.00 |
| Language | Kotlin | 2.4.20 |
| State management | ViewModel + StateFlow (AndroidX Lifecycle) | 2.11.0 |
| Backend | None (calls user-configured webhooks) | - |
| Local DB | Room | 2.8.5 |
| Notable libraries | WorkManager (durable, retrying API calls) | 2.12.0 |

## Project architecture

| | |
|---|---|
| Pattern | Clean Architecture + MVVM, feature-first |
| Encryption | No |

```mermaid
flowchart TD
  subgraph Presentation
    SmsReceiver
    ApiEditorViewModel
  end
  subgraph Domain
    HandleIncomingSmsUseCase
    SaveApiConfigUseCase
    ExecuteQueuedCallUseCase
    ApiConfigRepository
    CallScheduler
    HttpExecutor
  end
  subgraph Data
    ApiConfigRepositoryImpl
    WorkManagerCallScheduler
    ApiCallWorker
    OkHttpExecutor
    AppDatabase[(AppDatabase)]
  end
  SmsReceiver --> HandleIncomingSmsUseCase
  ApiEditorViewModel --> SaveApiConfigUseCase
  HandleIncomingSmsUseCase --> ApiConfigRepository
  HandleIncomingSmsUseCase --> CallScheduler
  SaveApiConfigUseCase --> ApiConfigRepository
  ExecuteQueuedCallUseCase --> HttpExecutor
  ApiConfigRepository -.implemented by.-> ApiConfigRepositoryImpl
  CallScheduler -.implemented by.-> WorkManagerCallScheduler
  HttpExecutor -.implemented by.-> OkHttpExecutor
  WorkManagerCallScheduler --> ApiCallWorker
  ApiCallWorker --> ExecuteQueuedCallUseCase
  ApiConfigRepositoryImpl --> AppDatabase
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
