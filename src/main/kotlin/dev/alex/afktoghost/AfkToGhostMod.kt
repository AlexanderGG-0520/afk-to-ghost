package dev.alex.afktoghost

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.level.ServerPlayer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object AfkToGhostMod : ModInitializer {
    const val MOD_ID = "afk-to-ghost"

    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    private lateinit var config: AfkGhostConfig
    private lateinit var ghostManager: GhostManager
    private lateinit var afkTracker: AfkTracker

    override fun onInitialize() {
        config = AfkGhostConfig.load(LOGGER)
        ghostManager = GhostManager(config, LOGGER)
        afkTracker = AfkTracker(config, ghostManager, LOGGER)

        LOGGER.info(
            "AFK to Ghost dependency baseline: Minecraft 26.1.2, Java 25, Fabric Loader 0.18.6, Fabric API 0.151.0+26.1.2, fabric-language-kotlin 1.13.12+kotlin.2.4.0, Loom 1.14.1, Gradle 9.2.0"
        )

        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            LOGGER.info("AFK to Ghost initialized on server tick {}", server.tickCount)
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            afkTracker.tick(server)
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            val player = handler.player
            afkTracker.remove(player)
            ghostManager.remove(player, "disconnect", null)
        }

        ServerLifecycleEvents.SERVER_STOPPING.register { _ ->
            afkTracker.clear()
            ghostManager.clear("server stopping")
        }

        GhostActivityEvents.register(afkTracker, LOGGER)
    }

    @JvmStatic
    fun shouldBlockDamage(player: ServerPlayer): Boolean {
        return ::ghostManager.isInitialized && ghostManager.shouldBlockDamage(player)
    }
}
