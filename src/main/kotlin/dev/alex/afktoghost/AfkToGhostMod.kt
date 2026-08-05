package dev.alex.afktoghost

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

object AfkToGhostMod : ModInitializer {
    const val MOD_ID = "afk-to-ghost"

    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    private lateinit var config: AfkGhostConfig
    private lateinit var ghostManager: GhostManager
    private lateinit var afkTracker: AfkTracker
    @Volatile
    private var serverRunning = false
    private var activeServer: MinecraftServer? = null
    private val playerLocales = mutableMapOf<UUID, String>()

    override fun onInitialize() {
        config = AfkGhostConfig.load(LOGGER)
        ghostManager = GhostManager(config, LOGGER)
        afkTracker = AfkTracker(config, ghostManager, LOGGER)

        LOGGER.info(
            "AFK to Ghost dependency baseline: Minecraft 1.21.1, Java 21, Fabric Loader 0.19.3, Fabric API 0.116.15+1.21.1, fabric-language-kotlin 1.13.13+kotlin.2.4.10, Loom 1.14.1, Gradle 9.2.0"
        )

        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            serverRunning = true
            activeServer = server
            LOGGER.info("AFK to Ghost initialized on server tick {}", server.tickCount)
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            afkTracker.tick(server)
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            val player = handler.player
            afkTracker.remove(player)
            ghostManager.remove(player, "disconnect", null)
            playerLocales.remove(player.uuid)
        }

        ServerLifecycleEvents.SERVER_STOPPING.register { _ ->
            afkTracker.clear()
            ghostManager.clear("server stopping")
        }

        ServerLifecycleEvents.SERVER_STOPPED.register { _ ->
            serverRunning = false
            activeServer = null
            playerLocales.clear()
        }

        GhostActivityEvents.register({ afkTracker }, LOGGER)
        AfkGhostCommands.register()
    }

    fun currentConfig(): AfkGhostConfig {
        return if (::config.isInitialized) config else AfkGhostConfig.load(LOGGER)
    }

    fun saveConfigFromScreen(newConfig: AfkGhostConfig): Boolean {
        val saved = AfkGhostConfig.save(newConfig, LOGGER)
        if (saved && !serverRunning) {
            applyConfig(newConfig)
        }
        return saved
    }

    fun saveAndApplyConfig(newConfig: AfkGhostConfig): Boolean {
        val saved = AfkGhostConfig.save(newConfig, LOGGER)
        if (saved) {
            applyConfig(newConfig)
        }
        return saved
    }

    fun reloadConfig(): Result<AfkGhostConfig> {
        return AfkGhostConfig.loadStrict(LOGGER).onSuccess { reloaded ->
            applyConfig(reloaded)
        }
    }

    fun configChangesRequireWorldRestart(): Boolean {
        return serverRunning
    }

    @JvmStatic
    fun shouldBlockDamage(player: ServerPlayer): Boolean {
        return ::ghostManager.isInitialized && ghostManager.shouldBlockDamage(player)
    }

    @JvmStatic
    fun recordActivity(player: ServerPlayer, reason: String) {
        if (::afkTracker.isInitialized) {
            afkTracker.markActivity(player, reason)
        }
    }

    @JvmStatic
    fun recordLocale(player: ServerPlayer, locale: String) {
        playerLocales[player.uuid] = locale
    }

    fun localeFor(player: ServerPlayer): String {
        return playerLocales[player.uuid] ?: "en_us"
    }

    private fun applyConfig(newConfig: AfkGhostConfig) {
        config = newConfig
        if (::ghostManager.isInitialized) {
            ghostManager.updateConfig(newConfig, activeServer?.playerList?.players ?: emptyList())
        } else {
            ghostManager = GhostManager(config, LOGGER)
        }

        if (::afkTracker.isInitialized) {
            afkTracker.updateConfig(newConfig)
        } else {
            afkTracker = AfkTracker(config, ghostManager, LOGGER)
        }
    }
}
