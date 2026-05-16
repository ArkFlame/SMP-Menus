# SMPMenus

Configurable Bukkit / Spigot / Paper / Folia menu plugin for SMP servers.

## Features

- DeluxeMenus-style YAML menu files.
- Default `/help` menu included as the first feature.
- Multi-row inventories.
- Configurable materials, names, lore, slots, sounds, and click actions.
- Base64 and nickname player-head item support.
- PlaceholderAPI soft-hook support when available.
- Bukkit 1.8+ compatibility target.
- Folia support through the plugin scheduler bridge.

## Requirements

- Java 8+ runtime for legacy server compatibility unless the project `pom.xml` states a higher target.
- Bukkit-family server: Bukkit, Spigot, Paper, or Folia.
- Maven for building from source.

## Build

```bash
mvn clean package
```

The compiled plugin jar is generated under:

```text
target/
```

## Install

1. Build the plugin or download a release jar.
2. Put the jar in your server `plugins/` folder.
3. Restart the server.
4. Edit generated menu files under the plugin data folder.
5. Reload with the plugin reload command if supported.

## Development rules

- Do not commit compiled jars or local server folders.
- Keep compatibility logic centralized.
- Do not import modern-only Bukkit/Paper APIs into always-loaded legacy code unless guarded by the project compatibility layer.
- Use existing text, material, sound, scheduler, and head utilities instead of adding duplicate helpers.
- For Folia, player/entity mutations must run on entity-owned execution; world/location mutations must run on region-owned execution; I/O stays async.

## License

See `LICENSE`.
