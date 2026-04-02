# FAQ

## Why did my teleport cancel?

Your teleport was likely interrupted by movement or damage during the countdown.

## Why can’t I use /spawn immediately again?

A cooldown is active for that command. Wait for the cooldown to expire and try again.

## I ran a teleport command and nothing happened. What should I check?

1. Confirm the command exists in `config.yml`.
2. Confirm your permission (`ezteleport.<command>`) is assigned.
3. Confirm destination settings are valid.
4. Confirm destination world exists and is loaded.

## How do I troubleshoot with logs?

Set `admin.debug: true` temporarily to get additional structured logs. Disable it after diagnosis to reduce log noise.
