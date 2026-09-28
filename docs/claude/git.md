# Git rules

| Rule | Reason | Example |
|---|---|---|
| Conventional commits, scope inline after colon, no parentheses | Owner's style | `feat: history - search by URL` |
| No trailers or tool attribution in commits or PR descriptions | Owner's rule | no `Co-Authored-By` |
| Commit locally; never `git push` unless asked | Owner reviews and pushes | - |
| Work on a feature branch, not `main` | Clean history | `feature/sms-api-trigger` |
| Changes to `CLAUDE.md` or `docs/claude/*` get their own commit | Rules stay traceable | `docs: update docs - detail is <what changed>` |
| PR description: one-line summary + short bullets | Owner's style | - |
