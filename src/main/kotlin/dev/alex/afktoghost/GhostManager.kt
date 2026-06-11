package dev.alex.afktoghost

import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import org.slf4j.Logger
import java.util.UUID

class GhostManager(
    private var config: AfkGhostConfig,
    private val logger: Logger,
) {
    private val ghosts = mutableSetOf<UUID>()
    private val enteredAtTick = mutableMapOf<UUID, Long>()
    private val hadInvisibility = mutableMapOf<UUID, Boolean>()

    fun enter(player: ServerPlayer, tick: Long) {
        val uuid = player.uuid
        if (!ghosts.add(uuid)) {
            return
        }

        enteredAtTick[uuid] = tick
        hadInvisibility[uuid] = player.hasEffect(MobEffects.INVISIBILITY)
        applyGhostEffects(player)
        protectFromAccidents(player)
        sendEnterTitle(player)

        logger.info(
            "{} entered AFK ghost mode at tick {} with game mode {}",
            player.gameProfile.name,
            tick,
            player.gameMode(),
        )
    }

    fun tick(player: ServerPlayer, tick: Long) {
        if (!isGhost(player)) {
            return
        }

        applyGhostEffects(player)
        protectFromAccidents(player)

        if (config.actionbar && tick % 40L == 0L) {
            player.sendOverlayMessage(GhostMessages.actionbar(player))
        }
    }

    fun exit(player: ServerPlayer, tick: Long, reason: String) {
        val uuid = player.uuid
        if (!ghosts.remove(uuid)) {
            return
        }

        if (config.invisibility && hadInvisibility.remove(uuid) == false) {
            player.removeEffect(MobEffects.INVISIBILITY)
        } else {
            hadInvisibility.remove(uuid)
        }

        val entered = enteredAtTick.remove(uuid)
        val durationSeconds = entered?.let { (tick - it) / 20.0 }
        player.sendSystemMessage(GhostMessages.exit(player))
        logger.info(
            "{} exited AFK ghost mode at tick {} reason={} durationSeconds={}",
            player.gameProfile.name,
            tick,
            reason,
            durationSeconds ?: "unknown",
        )
    }

    fun remove(player: ServerPlayer, reason: String, tick: Long?) {
        val uuid = player.uuid
        val wasGhost = ghosts.remove(uuid)
        hadInvisibility.remove(uuid)
        val entered = enteredAtTick.remove(uuid)

        if (wasGhost) {
            val durationSeconds = if (tick != null && entered != null) (tick - entered) / 20.0 else null
            logger.info(
                "{} removed from AFK ghost state reason={} durationSeconds={}",
                player.gameProfile.name,
                reason,
                durationSeconds ?: "unknown",
            )
        }
    }

    fun clear(reason: String) {
        val count = ghosts.size
        ghosts.clear()
        enteredAtTick.clear()
        hadInvisibility.clear()
        logger.info("Cleared {} AFK ghost runtime states: {}", count, reason)
    }

    fun isGhost(player: ServerPlayer): Boolean {
        return ghosts.contains(player.uuid)
    }

    fun shouldBlockDamage(player: ServerPlayer): Boolean {
        return config.invulnerable && isGhost(player)
    }

    fun updateConfig(newConfig: AfkGhostConfig, players: Iterable<ServerPlayer>) {
        val previousConfig = config
        config = newConfig

        if (previousConfig.invisibility && !newConfig.invisibility) {
            for (player in players) {
                if (isGhost(player) && hadInvisibility[player.uuid] == false) {
                    player.removeEffect(MobEffects.INVISIBILITY)
                }
            }
        }
    }

    private fun applyGhostEffects(player: ServerPlayer) {
        if (config.invisibility) {
            player.addEffect(
                MobEffectInstance(
                    MobEffects.INVISIBILITY,
                    MobEffectInstance.INFINITE_DURATION,
                    0,
                    true,
                    false,
                    false,
                )
            )
        }
    }

    private fun protectFromAccidents(player: ServerPlayer) {
        player.clearFire()
        player.resetFallDistance()
    }

    private fun sendEnterTitle(player: ServerPlayer) {
        player.connection.send(ClientboundSetTitlesAnimationPacket(10, 50, 20))
        player.connection.send(ClientboundSetTitleTextPacket(GhostMessages.enterTitle(player)))
        player.connection.send(ClientboundSetSubtitleTextPacket(GhostMessages.enterSubtitle(player)))
    }
}
