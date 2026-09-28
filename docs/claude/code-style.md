# Code style rules

| Rule | Reason | Example |
|---|---|---|
| Explicit types on properties and locals when not obvious | Readable without jumping files | `val errors: Set<ApiConfigError> = ...` |
| Declarations first, blank line, then logic | Scan-friendly functions | `CallExecution.run` |
| Every action logs with its data via `AppLogger` | Logs answer "what was sent/returned" | `AppLogger.i(TAG, "saved - {id: $id, url: $url}")` |
| Every `catch` logs the throwable before returning a fallback | Caught errors are otherwise invisible | `AppLogger.e(TAG, "delete failed - {id: $id}", e)` |
| No `println`, no raw `Log.*` | One logging entry point | `core/logging/AppLogger.kt` |
| No magic numbers: spacing in `Dimens`, colours in `Palette`/`StatusColors`, limits in constants | One owner per value | `Dimens.screenGutter`, `HttpConstants.MAX_RETRIES_LIMIT` |
| Every `Text` has an explicit `style =` from `MaterialTheme.typography` | No invisible defaults | `Text(text, style = MaterialTheme.typography.bodySmall)` |
| All user-facing strings in `values/strings.xml` **and** `values-vi/strings.xml`; counts use `<plurals>` | Two locales, lint `PluralsCandidate` | `R.plurals.dashboard_range_days` |
| VM messages are `UiMessage(@StringRes)`, resolved in UI via `LocalResources` | Config-aware strings | `MessageEffect` |
| Status is never colour-only: icon + label | Accessibility | `StatusPill` |
| Date/time logic only in `core/time/TimeUtils.kt` | One place for time math | `TimeUtils.startOfRange` |
| Comments ≤ 3 lines, explain why | Short, useful comments | - |
