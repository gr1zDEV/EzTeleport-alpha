# Troubleshooting

## Quick diagnostics

1. Confirm plugin is loaded at startup.
2. Confirm configured command exists.
3. Confirm user has `ezteleport.<command>` permission.
4. Confirm destination configuration is valid.
5. Confirm target world exists.

## Common issues

### Unknown command

**Cause**
- Command not defined in `config.yml`, or reload not applied.

**Fix**
- Add command under `commands:` and run `/ezteleport reload`.

### No permission

**Cause**
- Missing permission assignment.

**Fix**
- Grant `ezteleport.<command>` to intended group/player.

### Invalid world

**Cause**
- Destination world name mismatch or world not loaded.

**Fix**
- Correct world name and ensure world is available on server startup.

### Repeated cancellation reports

**Cause**
- Countdown policy too strict for server gameplay.

**Fix**
- Tune `countdown`, `cancel-on-move`, and `cancel-on-damage` per command.

## Debug mode guidance

Set `admin.debug: true` only during active diagnosis.

- Pros: better operational visibility.
- Cons: noisier logs.

Disable after issue resolution.
