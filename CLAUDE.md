# CLAUDE.md

| | |
|---|---|
| Project | Android app: incoming SMS matching a rule → call user-configured HTTP APIs. Kotlin, Compose, MVVM + clean architecture, Hilt, Room, WorkManager, OkHttp |
| Entry points | [App.kt](app/src/main/java/com/receiver/sms/App.kt), [MainActivity.kt](app/src/main/java/com/receiver/sms/MainActivity.kt), [SmsReceiver.kt](app/src/main/java/com/receiver/sms/features/dispatch/platform/SmsReceiver.kt) |

## Commands

| Task | Command |
|---|---|
| Build | `./gradlew :app:assembleDevDebug` |
| Install | `./gradlew :app:installDevDebug` |
| Test single class | `./gradlew :app:testDevDebugUnitTest --tests '*<ClassName>*'` |
| Instrumented single class | `./gradlew :app:connectedDevDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=<fqcn>` |
| Lint | `./gradlew :app:lintDevDebug` |
| Fake SMS on emulator | `adb emu sms send <sender> "<body>"` |

## Rules

- @docs/claude/architecture.md
- @docs/claude/code-style.md
- @docs/claude/testing.md
- @docs/claude/git.md
- @docs/claude/docs.md
