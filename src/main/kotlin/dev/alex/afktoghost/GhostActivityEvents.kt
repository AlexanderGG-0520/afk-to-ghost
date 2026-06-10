package dev.alex.afktoghost

import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import org.slf4j.Logger

object GhostActivityEvents {
    fun register(afkTracker: () -> AfkTracker, logger: Logger) {
        UseItemCallback.EVENT.register { player, _, _ ->
            wake(player, afkTracker(), "item use")
            InteractionResult.PASS
        }

        UseBlockCallback.EVENT.register { player, _, _, _ ->
            wake(player, afkTracker(), "block use")
            InteractionResult.PASS
        }

        AttackEntityCallback.EVENT.register { player, _, _, _, _ ->
            wake(player, afkTracker(), "entity attack")
            InteractionResult.PASS
        }

        AttackBlockCallback.EVENT.register { player, _, _, _, _ ->
            wake(player, afkTracker(), "block attack")
            InteractionResult.PASS
        }

        ServerMessageEvents.CHAT_MESSAGE.register { _, player, _ ->
            afkTracker().markActivity(player, "chat")
        }

        ServerMessageEvents.COMMAND_MESSAGE.register { _, source, _ ->
            source.player?.let { player ->
                afkTracker().markActivity(player, "command")
            }
        }

        logger.info("Registered AFK ghost activity callbacks")
    }

    private fun wake(player: net.minecraft.world.entity.player.Player, afkTracker: AfkTracker, reason: String) {
        if (player is ServerPlayer) {
            afkTracker.markActivity(player, reason)
        }
    }
}
