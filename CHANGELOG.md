# Changelog

## v0.3.0+26.2

### Compatibility

- Added Minecraft 26.2 support.
- Updated to Java 25.
- Updated Fabric Loader baseline to 0.19.3.
- Updated Fabric API baseline to 0.156.0+26.2.
- Updated Fabric Language Kotlin baseline to 1.13.13+kotlin.2.4.10.
- Updated optional Cloth Config support to 26.2.155 and Mod Menu support to 19.0.0-alpha.1.
- Preserved the complete AFK to Ghost 0.3.0 gameplay behavior from the 26.1.2 build without requiring source-level packet or Mixin changes.
- Verified clean compilation, automated tests, remapping, and Minecraft 26.2 dedicated-server startup in GitHub Actions.

## v0.3.0

### Added

- Added `/afktoghost config`, `list`, `get`, `set`, and `reload` commands.
- Added runtime configuration inspection and modification.
- Added strict configuration validation for unknown keys, invalid types, and invalid ranges.
- Added immediate and persistent configuration updates.
- Added activity detection for movement input, looking, boat paddling, combat actions, item and block interaction, chat, commands, and inventory actions.
- Added automated tests for activity decisions and configuration parsing.

### Fixed

- Fixed AFK and ghost state being cancelled when another player pushed the AFK player.
- Fixed passive movement from collision, knockback, water, pistons, vehicles, server correction, or teleportation being treated as player activity.
- Fixed missing primitive configuration fields being loaded incorrectly through direct Gson data-class hydration.
- Fixed configuration reload accepting unknown keys.

### Permissions

- `/afktoghost config`
- `/afktoghost config list`
- `/afktoghost config get <key>`

These commands are available without administrative permission.

- `/afktoghost config set <key> <value>`
- `/afktoghost config reload`

These commands require administrative command permission and are hidden from unauthorized command suggestions through Brigadier permission predicates.

### Compatibility

- Minecraft 26.1.2
- Fabric Loader 0.18.4 or later
- Fabric API 0.149.1+26.1.2
- Java 25

## v0.2.0

- Added integrated server support for singleplayer worlds hosted from a Fabric client.
- Added support for e4mc and LAN-hosted worlds without adding an e4mc dependency.
- For e4mc or LAN hosting, the hosting player installs the mod locally.
- Joining players still do not need the mod.
- Dedicated server support remains unchanged.

## v0.1.0

- Initial release.
