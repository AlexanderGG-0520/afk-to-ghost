package dev.alex.afktoghost

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import org.slf4j.Logger
import java.util.UUID

class GhostManager(
    private val config: AfkGhostConfig,
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
            player.sendOverlayMessage(Component.literal("AFK Ghost"))
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
}
