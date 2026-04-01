# Operations Runbook

Use this runbook for day-to-day admin operations.

## Admin commands

- `/ezteleport reload` — Reload configuration from disk.
- `/ezteleport metrics [command]` — View in-memory command usage counters.

## Routine operations

### After config changes

1. Validate YAML syntax.
2. Run `/ezteleport reload`.
3. Test one command as a normal player.
4. Test one command as an operator.

### Weekly health check

1. Run `/ezteleport metrics`.
2. Confirm high-traffic commands match expectations.
3. Spot unusual cancellation spikes.
4. Review whether cooldowns need adjustment.

## Incident playbook

### Symptom: players report teleports cancel unexpectedly

1. Enable `admin.debug: true` temporarily.
2. Reproduce using a staff test account.
3. Confirm whether movement or damage cancellation triggered.
4. Adjust `cancel-on-move` / `cancel-on-damage` policy.
5. Disable debug mode after resolution.

### Symptom: command exists but teleports fail

1. Verify destination world exists.
2. Validate coordinate bounds.
3. If using `destination.command`, test external plugin command manually.

## Change management

For production servers, apply teleport config changes using:

- Staging validation first
- Scheduled rollout window
- Rollback artifact retention (previous JAR + config)
