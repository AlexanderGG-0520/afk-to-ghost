# AFK to Ghost

AFK to Ghost is a server-side Fabric mod for Minecraft 26.1.2 that protects inactive players without changing their game mode.

## Why This Mod Exists

Players sometimes step away briefly or become unable to react. AFK to Ghost exists to prevent those players from dying simply because they are temporarily inactive, while allowing them to return to normal gameplay immediately.

## Features

- Server-side gameplay logic; joining players do not need to install the mod.
- Detects inactivity from server-side movement and look direction.
- Keeps the player's original game mode unchanged.
- Applies invisibility during AFK Ghost mode when enabled.
- Prevents incoming damage during AFK Ghost mode when enabled.
- Clears fire and resets fall distance while ghosted.
- Shows localized title and actionbar feedback.
- Removes AFK Ghost mode when the player moves, looks around, interacts, attacks, chats, or runs a command.
- Does not intentionally cancel the interaction that wakes the player.

## How It Works

After the configured timeout, an inactive player enters AFK Ghost mode. The player remains in the world with their existing game mode, but is treated as a passive protected presence.

When the player becomes active again, AFK Ghost mode is removed and normal gameplay resumes immediately.

This mod does not use Minecraft Spectator mode. It does not provide flight, wall clipping, spectator camera controls, or a custom game mode.

## Installation

1. Install Fabric Loader for Minecraft 26.1.2 on the server or hosting client.
2. Install the Fabric API and Fabric Language Kotlin versions used and tested by release `0.2.0`:
   - Fabric Loader `0.18.6`
   - Fabric API `0.151.0+26.1.2`
   - Fabric Language Kotlin `1.13.12+kotlin.2.4.0`
3. For a dedicated server, place the AFK to Ghost jar in the server's `mods` directory.
4. For e4mc or LAN hosting, place the jar in the hosting player's client `mods` directory.
5. Start the server or hosted world.
6. Joining players do not need to install the mod.

## Configuration

The config file is created at:

```text
config/afk-to-ghost.json
```

Fields:

- `timeoutSeconds`: AFK timeout in seconds, default `300`
- `debug`: enables diagnostic logging, default `true`
- `invisibility`: enables invisibility during AFK Ghost mode, default `true`
- `invulnerable`: enables damage protection during AFK Ghost mode, default `true`
- `actionbar`: enables title/actionbar feedback, default `true`

## Supported Languages

AFK to Ghost is fully server-side and sends resolved text using the locale reported by each player.

Supported UI languages:

- English
- Japanese
- Simplified Chinese
- Traditional Chinese
- Korean

English is used as the fallback for unsupported or unavailable locales.

## Compatibility

- Minecraft: `26.1.2`
- Mod loader: Fabric
- Environment: dedicated server or integrated server hosted from a Fabric client
- Implementation language: Kotlin
- Java target: `25`

This mod does not claim exhaustive compatibility with other mods.

## Building From Source

Build with:

```bash
./gradlew build
```

Release jars are produced under:

```text
build/libs/
```

## License

MIT License.
