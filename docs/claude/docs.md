# Docs rules

| Rule | Reason |
|---|---|
| Only `README.md` and `CLAUDE.md` at repo root; every other `.md` under `docs/` | Predictable location |
| `README.md` sections only: overview table, store links, app IDs, tech stack, architecture diagram, local DB diagram | README stays a summary |
| `CLAUDE.md` is an index; rules live in `docs/claude/<topic>.md` | Short always-loaded context |
| Topic with 2+ files becomes a subfolder (`docs/modules/`, `docs/guides/`) | Grouping |
| Tables and Mermaid diagrams first; at most one short sentence under a heading | Scannable |
| English only | One language |
| Values (versions, ids, commands, paths) copied from the repo; unknown → `TODO: confirm` | Accuracy |
| Update docs in the same change as the code they describe (new table, route, dependency, setting) | No stale docs |

| Doc | Update when |
|---|---|
| [README.md](../../README.md) | Version bump, app id change, new table/column |
| [docs/architecture.md](../architecture.md) | New feature, layer rule, key decision |
| [docs/features.md](../features.md) | Behaviour, placeholder or retry policy change |
| [docs/setup.md](../setup.md) | New command, SDK/JDK requirement, local config key |
