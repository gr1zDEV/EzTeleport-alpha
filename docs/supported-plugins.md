# Supported Plugins

EzTeleport is designed to work out-of-the-box on:

- **Paper** (1.21.x API compatible)
- **Folia** (supported)

## Soft Dependencies

EzTeleport currently includes the following soft dependency:

- **BetterRTP**

Soft dependency means EzTeleport does **not** require BetterRTP to start. If BetterRTP is installed, you can use `destination.command` to execute plugin commands as part of teleport workflows.

## Integration Pattern

Use command-mode destinations to integrate with other plugins:

```yaml
destination:
  command: "rtp"
```

If `destination.command` is set, EzTeleport runs that command as the player instead of teleporting to static coordinates.
