# Installation & Upgrades

## Requirements

- Java **21**
- Paper/Folia API **1.21.x** compatible server

## Fresh install

1. Build the plugin JAR:
   ```bash
   mvn clean package
   ```
2. Copy the generated JAR (`target/EzTeleport-<version>.jar`) into your server `plugins/` directory.
3. Start or restart the server.
4. Open `plugins/EzTeleport/config.yml` and update command definitions.
5. Run:
   ```
   /ezteleport reload
   ```

## Upgrade process (recommended)

Use this sequence to minimize player-facing risk:

1. **Announce maintenance window** to players.
2. Back up:
   - Existing EzTeleport JAR
   - `plugins/EzTeleport/config.yml`
3. Replace old JAR with new version.
4. Start server and validate startup logs.
5. Verify:
   - `/ezteleport reload`
   - one configured command (`/hub`, `/spawn`, etc.)
6. Keep old JAR for rollback until validation completes.

## Rollback plan

If a release causes issues:

1. Stop server.
2. Restore previous JAR and config backup.
3. Restart server.
4. Confirm command behavior and permissions.

## Post-install checklist

- [ ] Admin permission `ezteleport.admin` works.
- [ ] Custom teleport command permissions (`ezteleport.<command>`) are assigned as intended.
- [ ] Countdown/cooldown values align with server gameplay rules.
- [ ] Destination worlds exist.
- [ ] Debug mode (`admin.debug`) is disabled unless troubleshooting.
