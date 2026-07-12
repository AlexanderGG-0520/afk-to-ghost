package dev.alex.afktoghost

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
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

    fun valueAsString(key: String): String? {
        return entries().firstOrNull { it.key == key }?.let { it.format(this) }
    }

    fun withParsedValue(key: String, rawValue: String): Result<AfkGhostConfig> {
        val entry = entries().firstOrNull { it.key == key }
            ?: return Result.failure(IllegalArgumentException("Unknown config key '$key'. Supported keys: ${supportedKeys().joinToString()}"))

        return entry.parse(rawValue).map { value -> entry.apply(this, value) }
    }

    companion object {
        private val gson = GsonBuilder().setPrettyPrinting().create()
        private val configEntries = listOf(
            ConfigEntry(
                key = "timeoutSeconds",
                description = "AFK timeout in seconds",
                format = { it.timeoutSeconds.toString() },
                parse = { raw ->
                    raw.toLongOrNull()
                        ?.takeIf { it >= 1L }
                        ?.let { Result.success(it as Any) }
                        ?: Result.failure(IllegalArgumentException("timeoutSeconds must be a whole number greater than or equal to 1"))
                },
                apply = { config, value -> config.copy(timeoutSeconds = value as Long) },
            ),
            booleanEntry("debug", "Enable debug logging", { it.debug }) { config, value -> config.copy(debug = value) },
            booleanEntry("invisibility", "Apply invisibility while ghosted", { it.invisibility }) { config, value ->
                config.copy(invisibility = value)
            },
            booleanEntry("invulnerable", "Block incoming damage while ghosted", { it.invulnerable }) { config, value ->
                config.copy(invulnerable = value)
            },
            booleanEntry("actionbar", "Show actionbar feedback while ghosted", { it.actionbar }) { config, value ->
                config.copy(actionbar = value)
            },
        )

        fun entries(): List<ConfigEntry> {
            return configEntries
        }

        fun supportedKeys(): List<String> {
            return configEntries.map { it.key }
        }

        fun load(logger: Logger): AfkGhostConfig {
            val path = configPath()
            val defaults = AfkGhostConfig()

            return loadValidated(path, defaults, logger).onSuccess { loaded ->
                logger.info(
                    "AFK to Ghost config loaded from {}: timeoutSeconds={}, debug={}, invisibility={}, invulnerable={}, actionbar={}",
                    path,
                    loaded.timeoutSeconds,
                    loaded.debug,
                    loaded.invisibility,
                    loaded.invulnerable,
                    loaded.actionbar,
                )
            }.getOrElse { error ->
                logger.error("Invalid AFK to Ghost config at {}; using defaults", path, error)
                defaults
            }
        }

        fun loadStrict(logger: Logger): Result<AfkGhostConfig> {
            val path = configPath()
            val defaults = AfkGhostConfig()
            return loadValidated(path, defaults, logger).onSuccess { loaded ->
                logger.info(
                    "AFK to Ghost config reloaded from {}: timeoutSeconds={}, debug={}, invisibility={}, invulnerable={}, actionbar={}",
                    path,
                    loaded.timeoutSeconds,
                    loaded.debug,
                    loaded.invisibility,
                    loaded.invulnerable,
                    loaded.actionbar,
                )
            }.onFailure { error ->
                logger.error("Could not reload AFK to Ghost config at {}", path, error)
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

        private fun loadValidated(path: Path, defaults: AfkGhostConfig, logger: Logger): Result<AfkGhostConfig> {
            return try {
                ensureConfigExists(path, defaults)
                val json = Files.newBufferedReader(path).use { reader ->
                    JsonParser.parseReader(reader)
                }
                parseJsonElement(json, defaults)
            } catch (error: JsonParseException) {
                Result.failure(error)
            } catch (error: IllegalArgumentException) {
                Result.failure(error)
            } catch (error: IOException) {
                Result.failure(error)
            }
        }

        fun parseJson(jsonText: String): Result<AfkGhostConfig> {
            return try {
                parseJsonElement(JsonParser.parseString(jsonText), AfkGhostConfig())
            } catch (error: JsonParseException) {
                Result.failure(error)
            } catch (error: IllegalArgumentException) {
                Result.failure(error)
            }
        }

        private fun parseJsonElement(json: JsonElement, defaults: AfkGhostConfig): Result<AfkGhostConfig> {
            if (json !is JsonObject) {
                return Result.failure(IllegalArgumentException("Config root must be a JSON object"))
            }

            val supported = supportedKeys().toSet()
            val unknown = json.keySet().filterNot(supported::contains)
            if (unknown.isNotEmpty()) {
                return Result.failure(IllegalArgumentException("Unsupported config keys: ${unknown.joinToString()}"))
            }

            val timeoutSeconds = readLong(json, "timeoutSeconds", defaults.timeoutSeconds)
                .getOrElse { return Result.failure(it) }
            if (timeoutSeconds < 1L) {
                return Result.failure(IllegalArgumentException("timeoutSeconds must be greater than or equal to 1"))
            }

            return Result.success(
                AfkGhostConfig(
                    timeoutSeconds = timeoutSeconds,
                    debug = readBoolean(json, "debug", defaults.debug).getOrElse { return Result.failure(it) },
                    invisibility = readBoolean(json, "invisibility", defaults.invisibility).getOrElse { return Result.failure(it) },
                    invulnerable = readBoolean(json, "invulnerable", defaults.invulnerable).getOrElse { return Result.failure(it) },
                    actionbar = readBoolean(json, "actionbar", defaults.actionbar).getOrElse { return Result.failure(it) },
                )
            )
        }

        private fun readLong(json: JsonObject, key: String, defaultValue: Long): Result<Long> {
            val element = json.get(key) ?: return Result.success(defaultValue)
            if (!element.isJsonPrimitive || !element.asJsonPrimitive.isNumber) {
                return Result.failure(IllegalArgumentException("$key must be a whole number"))
            }

            return try {
                val value = element.asLong
                if (element.asBigDecimal.stripTrailingZeros().scale() > 0) {
                    Result.failure(IllegalArgumentException("$key must be a whole number"))
                } else {
                    Result.success(value)
                }
            } catch (error: NumberFormatException) {
                Result.failure(IllegalArgumentException("$key must be a whole number", error))
            } catch (error: ArithmeticException) {
                Result.failure(IllegalArgumentException("$key must be a whole number", error))
            }
        }

        private fun readBoolean(json: JsonObject, key: String, defaultValue: Boolean): Result<Boolean> {
            val element = json.get(key) ?: return Result.success(defaultValue)
            if (!element.isJsonPrimitive || !element.asJsonPrimitive.isBoolean) {
                return Result.failure(IllegalArgumentException("$key must be true or false"))
            }
            return Result.success(element.asBoolean)
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

        private fun booleanEntry(
            key: String,
            description: String,
            getter: (AfkGhostConfig) -> Boolean,
            setter: (AfkGhostConfig, Boolean) -> AfkGhostConfig,
        ): ConfigEntry {
            return ConfigEntry(
                key = key,
                description = description,
                format = { getter(it).toString() },
                parse = { raw ->
                    when (raw.lowercase()) {
                        "true" -> Result.success(true)
                        "false" -> Result.success(false)
                        else -> Result.failure(IllegalArgumentException("$key must be true or false"))
                    }.map { it as Any }
                },
                apply = { config, value -> setter(config, value as Boolean) },
            )
        }
    }

    data class ConfigEntry(
        val key: String,
        val description: String,
        val format: (AfkGhostConfig) -> String,
        val parse: (String) -> Result<Any>,
        val apply: (AfkGhostConfig, Any) -> AfkGhostConfig,
    )
}
