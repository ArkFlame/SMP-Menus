# Contributing

## Scope

Contributions must stay scoped to the requested issue or pull request. Do not include unrelated refactors.

## Local checks

Run before opening a pull request:

```bash
mvn clean package
```

If tests exist, run:

```bash
mvn test
```

## Code standards

- Keep Java code immutable where practical.
- Prefer constructor injection.
- Do not return `null` from new APIs; use `Optional`, empty collections, or throw.
- Do not silently catch exceptions. Log or rethrow, never both.
- Do not use raw threads. Use existing scheduler/executor utilities.
- Reuse existing material, sound, text, scheduler, and item/head utilities.
- Preserve Bukkit 1.8+ compatibility unless a task explicitly changes target versions.
- Preserve Folia ownership rules.

## Pull request rules

- Describe exact behavior changed.
- Include verification command output.
- Mention any config migration.
- Include screenshots for menu layout changes.
