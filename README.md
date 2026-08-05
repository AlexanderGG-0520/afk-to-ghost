# AFK to Ghost for Minecraft 1.21.1

AFK to Ghost is a server-side Fabric mod that protects inactive players without changing their game mode. Version `0.3.0+1.21.1` is dedicated to Minecraft 1.21.1 and requires Java 21.

## Features

- Runs on dedicated servers and Fabric integrated servers, including singleplayer, LAN, and e4mc-style hosting.
- Joining players do not need the mod.
- Detects activity from player-originated keyboard input, camera rotation, interactions, attacks, chat, commands, boat paddling, and inventory actions.
- Ignores externally caused position changes, including knockback, collisions, currents, pistons, vehicle movement, server corrections, and teleportation. Position-only movement packets never reset AFK state.
- Keeps the player's game mode unchanged; it does not add spectator mode, flight, noclip, teleportation, or a custom game mode.
- Optionally applies invisibility and blocks incoming damage while ghosted.
- Clears fire and resets fall distance while ghosted.
- Shows localized enter titles, repeated actionbar status, and an exit message, with English fallback.

## Exact Dependencies

Required server dependencies:

- Minecraft `1.21.1`
- Java `21`
- Fabric Loader `0.19.3`
- Fabric API `0.116.15+1.21.1`
- Fabric Language Kotlin `1.13.13+kotlin.2.4.10`

Optional client configuration dependencies:

- Mod Menu `11.0.4`
- Cloth Config `15.0.140`

The build uses Fabric Loom `1.14.1`, Kotlin Gradle plugin `2.4.10`, Mojang official mappings, and Gradle `9.2.0`.

For a dedicated server, install the mod and its three required Fabric dependencies in the server's `mods` directory. For singleplayer, LAN, or e4mc-style hosting, install them in the hosting client's `mods` directory. Other connecting clients need no installation.

Mod Menu and Cloth Config are optional and client-only. Install both on a hosting client to edit the configuration through Mod Menu. They are not needed on a dedicated server.

## Configuration and Commands

The validated configuration is stored at `config/afk-to-ghost.json`:

- `timeoutSeconds` (default `300`, whole number at least `1`)
- `debug` (default `true`)
- `invisibility` (default `true`)
- `invulnerable` (default `true`)
- `actionbar` (default `true`)

Unknown keys, invalid types, fractional timeouts, and out-of-range timeouts are rejected. A failed runtime reload leaves the active configuration unchanged.

Runtime commands:

- `/afktoghost config`
- `/afktoghost config list`
- `/afktoghost config get <key>`
- `/afktoghost config set <key> <value>` — operator permission level 2 required
- `/afktoghost config reload` — operator permission level 2 required

The restricted command nodes are hidden from suggestions for users without permission.

## Version-specific Protocol Notes

Minecraft 1.21.1 reports movement intent with forward/strafe values plus jump and sneak flags. AFK to Ghost reads those inputs directly and does not infer activity from coordinates or velocity. Rotation counts only when a rotation-bearing packet contains an actual yaw or pitch change.

Inventory activity coverage includes container clicks and buttons, closing containers, recipe placement, creative slots, item renaming, pick-block, book editing, trade selection, and beacon selection.

## Building

Use Java 21 and run:

```bash
./gradlew clean test build
```

The release jar is written to `build/libs/afk-to-ghost-0.3.0+1.21.1.jar`.

## Manual Verification Checklist

1. A stationary player enters Ghost mode after the timeout.
2. Knockback, currents, pistons, collisions, vehicles, teleports, corrections, and position-only movement packets do not wake the player.
3. Keyboard movement, jump, sneak, actual camera rotation, boat paddling, interaction, attacks, swings, chat, commands, and inventory actions wake the player without cancelling the action.
4. Incoming damage is blocked only for a ghost when `invulnerable` is enabled; it works normally after exit and for all non-ghost players.
5. Fire and fall distance are cleared while ghosted.
6. Mod-introduced invisibility is removed on exit, while invisibility present before entry is preserved.
7. The player's game mode never changes.
8. Non-operators cannot see or execute `config set` or `config reload`.
9. Invalid reloads fail without replacing the active config.
10. An unmodded client can connect and play normally.

## License

MIT License.
