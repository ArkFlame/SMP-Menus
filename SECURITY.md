# Security Policy

## Supported versions

Security fixes target the latest maintained source version in this repository.

## Reporting a vulnerability

Do not open a public issue for vulnerabilities.

Report privately to the project owner or ArkFlame Development contact channel.
Include:

- affected version or commit,
- server platform and Minecraft version,
- reproduction steps,
- expected impact,
- logs or stack traces when available.

## Security expectations

- Do not commit secrets, tokens, private keys, database dumps, or server runtime folders.
- Treat all config, placeholder, command, and player-provided input as untrusted.
- Keep external plugin hooks optional and failure-safe.
