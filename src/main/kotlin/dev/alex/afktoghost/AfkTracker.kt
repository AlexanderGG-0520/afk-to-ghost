package dev.alex.afktoghost

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.slf4j.Logger
import java.util.UUID
import kotlin.math.abs

class AfkTracker(
    private val config: AfkGhostConfig,
    private val ghostManager: GhostManager,
    private val logger: Logger,
) {
    private val snapshots = mutableMapOf<UUID, Snapshot>()

    fun tick(server: MinecraftServer) {
        val tick = server.tickCount.toLong()

        for (player in server.playerList.players) {
            val previous = snapshots[player.uuid]
            val current = Snapshot.from(player, previous?.lastActiveTick ?: tick)

            if (previous == null) {
                snapshots[player.uuid] = current
                continue
            }

            if (previous.isActivity(current)) {
                snapshots[player.uuid] = current.copy(lastActiveTick = tick)
                ghostManager.exit(player, tick, "movement/look")
                continue
            }

            snapshots[player.uuid] = current.copy(lastActiveTick = previous.lastActiveTick)

            if (tick - previous.lastActiveTick >= config.timeoutTicks) {
                ghostManager.enter(player, tick)
            }

            ghostManager.tick(player, tick)
        }
    }

    fun markActivity(player: ServerPlayer, reason: String) {
        val tick = player.level().server.tickCount.toLong()
        snapshots[player.uuid] = Snapshot.from(player, tick)
        ghostManager.exit(player, tick, reason)

        if (config.debug) {
            logger.debug("Activity from {}: {}", player.gameProfile.name, reason)
        }
    }

    fun remove(player: ServerPlayer) {
        snapshots.remove(player.uuid)
    }

    fun clear() {
        snapshots.clear()
    }

    private data class Snapshot(
        val x: Double,
        val y: Double,
        val z: Double,
        val yaw: Float,
        val pitch: Float,
        val lastActiveTick: Long,
    ) {
        fun isActivity(other: Snapshot): Boolean {
            val moved = abs(x - other.x) > MOVE_THRESHOLD ||
                abs(y - other.y) > MOVE_THRESHOLD ||
                abs(z - other.z) > MOVE_THRESHOLD
            val looked = angleDelta(yaw, other.yaw) > LOOK_THRESHOLD ||
                angleDelta(pitch, other.pitch) > LOOK_THRESHOLD

            return moved || looked
        }

        companion object {
            private const val MOVE_THRESHOLD = 0.003
            private const val LOOK_THRESHOLD = 0.1f

            fun from(player: ServerPlayer, lastActiveTick: Long): Snapshot {
                return Snapshot(
                    x = player.x,
                    y = player.y,
                    z = player.z,
                    yaw = player.yRot,
                    pitch = player.xRot,
                    lastActiveTick = lastActiveTick,
                )
            }

            private fun angleDelta(a: Float, b: Float): Float {
                var delta = abs(a - b) % 360.0f
                if (delta > 180.0f) {
                    delta = 360.0f - delta
                }
                return delta
            }
        }
    }
}
