# Command Configuration

This guide describes production-safe configuration patterns for `plugins/EzTeleport/config.yml`.

## Core structure

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

## Destination modes

### 1) Coordinate teleport
Use when you want deterministic location-based teleports.

- Keep `destination.command` empty.
- Set `world`, `x`, `y`, `z`, `yaw`, and `pitch`.

### 2) Command execution
Use when integrating with other plugins (for example, random teleport plugins).

- Set `destination.command` to a command string.
- The command runs as the player.

## Production defaults by server type

### Survival/Economy server
- `countdown: 5-10`
- `cooldown: 30-120`
- `cancel-on-move: true`
- `cancel-on-damage: true`

### Network hub/lobby
- `countdown: 0-3`
- `cooldown: 0-10`
- `cancel-on-move: false`
- `cancel-on-damage: false`

## Message placeholder

- `{time}` → remaining seconds in countdown/cooldown messages.

## Example: robust `/hub` setup

```yaml
commands:
  hub:
    aliases: ["spawn"]
    countdown: 3
    cooldown: 20
    destination:
      command: ""
      world: world
      x: 0.5
      y: 80
      z: 0.5
      yaw: 180
      pitch: 0
    cancel-on-move: true
    cancel-on-damage: true
```

## Hardening tips

- Keep countdown above 0 for PvP and survival balance.
- Avoid very low cooldowns in high-population servers.
- Ensure destination world names are exact and case-correct.
- Use aliases sparingly to avoid command collisions.
