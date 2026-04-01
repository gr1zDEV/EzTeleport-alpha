# EzTeleport

EzTeleport is a lightweight **Paper/Folia** plugin that lets you define custom teleport-style commands (for example `/hub`, `/spawn`, `/afk`) with configurable countdowns, cooldowns, cancellation rules, messages, and sounds.

## Features

- Per-command settings in `config.yml`
- Optional aliases per command
- Countdown before teleport
- Cooldown between uses
- Cancel on movement and/or damage
- Teleport using coordinates **or** execute a command (e.g. BetterRTP)
- Configurable chat and action bar messages
- Per-message delivery toggles (chat/actionbar)
- Configurable sounds for tick/success/cancel
- `/ezteleport reload` admin command
- `/ezteleport metrics [command]` admin metrics command
- Folia supported
- Optional debug mode with structured operational logs
- In-memory per-command metrics hooks (attempted/succeeded/cancelled)

## Documentation

For production-ready server-owner and player-facing docs, see [`docs/`](./docs/index.md).

## Requirements

- Java **21**
- Paper/Folia API **1.21.x**

## Build

From the project root:

```bash
mvn clean package
```

Output jar:

- `target/EzTeleport-<version>.jar`

## Install

1. Build the plugin jar.
2. Copy it into your server's `plugins/` directory.
3. Start/restart the server.
4. Edit `plugins/EzTeleport/config.yml` as needed.
5. Run `/ezteleport reload` after config changes.

## Commands & Permissions

### Commands

- `/ezteleport reload` — Reloads the plugin configuration.
- `/ezteleport metrics [command]` — Shows in-memory teleport counters for all commands or one command.
- `/ezteleport <player> <teleport>` — Starts a configured teleport for an online player.

### Permission

- `ezteleport.admin` (default: op)
- `ezteleport.<command>` (default: true)

## Configuration Overview

The plugin is configured under:

```yaml
commands:
  <command-name>:
    aliases: []
    countdown: 5
    cooldown: 30
    destination:
      command: ""
      world: world
      x: 0.5
      y: 64.0
      z: 0.5
      yaw: 0
      pitch: 0
    cancel-on-move: true
    cancel-on-damage: true
    messages: ...
    actionbar-messages: ...
    message-delivery: ...
    sounds: ...
admin:
  reload-message: "<green>EzTeleport config reloaded."
  no-permission: "<red>You do not have permission."
  debug: false
```

### `destination.command` vs coordinates

- If `destination.command` is non-empty, the plugin runs that command as the player.
- If `destination.command` is empty, the plugin teleports to the configured world/coordinates.

### Message placeholders

- `{time}` — Remaining seconds for countdown/cooldown messaging.

### Observability

- Set `admin.debug: true` to enable structured debug logs for:
  - command/permission registration events
  - teleport cancel reasons
  - teleport failure reasons
- Metrics are stored in memory per command and exposed via `/ezteleport metrics`.

## Example commands included by default

- `hub` (alias: `spawn`)
- `afk`

## Notes

- If a configured world does not exist, players receive the `invalid-world` message.
- BetterRTP is listed as an optional soft dependency and can be used through `destination.command`.

## Development

The project uses Maven with `paper-api` as a provided dependency.

```bash
mvn test
```

(If you have no tests yet, this will still validate Maven project setup.)
