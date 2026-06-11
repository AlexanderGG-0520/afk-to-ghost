package dev.alex.afktoghost

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.slf4j.Logger
import java.util.UUID

class AfkTracker(
    private var config: AfkGhostConfig,
    private val ghostManager: GhostManager,
    private val logger: Logger,
) {
    private val lastActiveTicks = mutableMapOf<UUID, Long>()

    fun tick(server: MinecraftServer) {
        val tick = server.tickCount.toLong()

        for (player in server.playerList.players) {
            val lastActiveTick = lastActiveTicks[player.uuid]
            if (lastActiveTick == null) {
                lastActiveTicks[player.uuid] = tick
                continue
            }

            if (tick - lastActiveTick >= config.timeoutTicks) {
                ghostManager.enter(player, tick)
            }

            ghostManager.tick(player, tick)
        }
    }

    fun markActivity(player: ServerPlayer, reason: String) {
        val tick = player.level().server.tickCount.toLong()
        lastActiveTicks[player.uuid] = tick
        ghostManager.exit(player, tick, reason)

        if (config.debug) {
            logger.debug("Activity from {}: {}", player.gameProfile.name, reason)
        }
    }

    fun updateConfig(newConfig: AfkGhostConfig) {
        config = newConfig
    }

    fun remove(player: ServerPlayer) {
        lastActiveTicks.remove(player.uuid)
    }

    fun clear() {
        lastActiveTicks.clear()
    }
}
