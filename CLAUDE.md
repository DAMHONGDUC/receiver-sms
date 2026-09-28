# CLAUDE.md

| | |
|---|---|
| Project | Android app: incoming SMS matching a rule → call user-configured HTTP APIs. Kotlin, Compose, MVVM + clean architecture, Hilt, Room, WorkManager, OkHttp |
| Entry points | [App.kt](app/src/main/java/com/receiver/sms/App.kt), [MainActivity.kt](app/src/main/java/com/receiver/sms/MainActivity.kt), [SmsReceiver.kt](app/src/main/java/com/receiver/sms/features/dispatch/platform/SmsReceiver.kt) |

## Commands

| Task | Command |
|---|---|
| Build | `./gradlew :app:assembleDebug` |
| Install | `./gradlew :app:installDebug` |
| Test single class | `./gradlew :app:testDebugUnitTest --tests '*<ClassName>*'` |
| Lint | `./gradlew :app:lintDebug` |
| Fake SMS on emulator | `adb emu sms send <sender> "<body>"` |

## Rules

- @docs/claude/architecture.md
- @docs/claude/code-style.md
- @docs/claude/testing.md
- @docs/claude/git.md
- @docs/claude/docs.md
