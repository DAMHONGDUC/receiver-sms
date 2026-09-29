# CLAUDE.md

| | |
|---|---|
| Project | Android app: incoming SMS matching a rule → call user-configured HTTP APIs. Kotlin, Compose, MVVM + clean architecture, Hilt, Room, WorkManager, OkHttp |
| Entry points | [App.kt](app/src/main/java/com/dd/sms/hook/App.kt), [MainActivity.kt](app/src/main/java/com/dd/sms/hook/MainActivity.kt), [SmsReceiver.kt](app/src/main/java/com/dd/sms/hook/features/dispatch/platform/SmsReceiver.kt) |

## Commands

| Task | Command |
|---|---|
| List make targets | `make` (from `packages/script-tools`; `make setup` after clone) |
| Build | `make build` |
| Install | `make install` |
| Test single class | `make test TEST=<ClassName>` |
| Instrumented single class | `./gradlew :app:connectedDevDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=<fqcn>` |
| Lint | `make lint` |
| Signed release APK / AAB / both into `Release/` | `make apk` / `make aab` / `make release` (`FLAVOR=dev` for dev) |
| Fake SMS on emulator | `make sms SENDER=<sender> BODY="<body>"` |

## Rules

- @docs/claude/architecture.md
- @docs/claude/code-style.md
- @docs/claude/testing.md
- @docs/claude/git.md
- @docs/claude/docs.md
