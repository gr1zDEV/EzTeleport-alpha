# Commands & Permissions

## Commands

- `/ezteleport reload` — Reloads plugin configuration from disk.
- `/ezteleport metrics [command]` — Shows in-memory teleport counters for all commands or one command.
- `/ezteleport <player> <teleport>` — Starts a configured teleport for an online player.

## Permissions

- `ezteleport.admin` *(default: op)*
  - Allows use of EzTeleport administrative commands.
- `ezteleport.<command>` *(default: true)*
  - Allows players to use a configured teleport command.

## Permission Examples

Grant access to `/hub` command:

- `ezteleport.hub`

Grant admin control:

- `ezteleport.admin`
