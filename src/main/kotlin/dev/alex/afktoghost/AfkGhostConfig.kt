package dev.alex.afktoghost

import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

data class AfkGhostConfig(
    val timeoutSeconds: Long = 300,
    val debug: Boolean = true,
    val invisibility: Boolean = true,
    val invulnerable: Boolean = true,
    val actionbar: Boolean = true,
) {
    val timeoutTicks: Long
        get() = timeoutSeconds.coerceAtLeast(1) * 20L

    companion object {
        private val gson = GsonBuilder().setPrettyPrinting().create()

        fun load(logger: Logger): AfkGhostConfig {
            val path = configPath()
            val defaults = AfkGhostConfig()

            return try {
                ensureConfigExists(path, defaults)
                val loaded = Files.newBufferedReader(path).use { reader ->
                    gson.fromJson(reader, AfkGhostConfig::class.java)
                } ?: defaults

                logger.info(
                    "AFK to Ghost config loaded from {}: timeoutSeconds={}, debug={}, invisibility={}, invulnerable={}, actionbar={}",
                    path,
                    loaded.timeoutSeconds,
                    loaded.debug,
                    loaded.invisibility,
                    loaded.invulnerable,
                    loaded.actionbar,
                )
                loaded
            } catch (error: JsonParseException) {
                logger.error("Invalid AFK to Ghost config at {}; using defaults", path, error)
                defaults
            } catch (error: IOException) {
                logger.error("Could not load AFK to Ghost config at {}; using defaults", path, error)
                defaults
            }
        }

        fun save(config: AfkGhostConfig, logger: Logger): Boolean {
            val path = configPath()
            return try {
                path.parent?.let(Files::createDirectories)
                Files.newBufferedWriter(path).use { writer ->
                    gson.toJson(config, writer)
                }
                logger.info(
                    "AFK to Ghost config saved to {}: timeoutSeconds={}, debug={}, invisibility={}, invulnerable={}, actionbar={}",
                    path,
                    config.timeoutSeconds,
                    config.debug,
                    config.invisibility,
                    config.invulnerable,
                    config.actionbar,
                )
                true
            } catch (error: IOException) {
                logger.error("Could not save AFK to Ghost config at {}", path, error)
                false
            }
        }

        private fun configPath(): Path {
            return FabricLoader.getInstance().configDir.resolve("afk-to-ghost.json")
        }

        private fun ensureConfigExists(path: Path, defaults: AfkGhostConfig) {
            if (Files.exists(path)) {
                return
            }

            path.parent?.let(Files::createDirectories)
            Files.newBufferedWriter(path).use { writer ->
                gson.toJson(defaults, writer)
            }
        }
    }
}
