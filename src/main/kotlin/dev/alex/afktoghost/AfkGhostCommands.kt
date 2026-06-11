package dev.alex.afktoghost

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

object AfkGhostCommands {
    private val unknownKeyError = DynamicCommandExceptionType { key ->
        Component.literal("Unknown AFK to Ghost config key '$key'. Supported keys: ${AfkGhostConfig.supportedKeys().joinToString()}")
    }
    private val invalidConfigError = DynamicCommandExceptionType { reason ->
        Component.literal("Invalid AFK to Ghost config: $reason")
    }
    private val saveFailedError = SimpleCommandExceptionType(Component.literal("Failed to save AFK to Ghost config. Check the server log."))

    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                Commands.literal("afktoghost")
                    .then(
                        Commands.literal("config")
                            .executes(::listConfig)
                            .then(Commands.literal("list").executes(::listConfig))
                            .then(
                                Commands.literal("get")
                                    .then(
                                        Commands.argument("key", StringArgumentType.word())
                                            .suggests { _, builder ->
                                                SharedSuggestionProvider.suggest(AfkGhostConfig.supportedKeys(), builder)
                                            }
                                            .executes(::getConfig)
                                    )
                            )
                            .then(
                                Commands.literal("set")
                                    .requires { source -> Commands.hasPermission<CommandSourceStack>(Commands.LEVEL_ADMINS).test(source) }
                                    .then(
                                        Commands.argument("key", StringArgumentType.word())
                                            .suggests { _, builder ->
                                                SharedSuggestionProvider.suggest(AfkGhostConfig.supportedKeys(), builder)
                                            }
                                            .then(
                                                Commands.argument("value", StringArgumentType.word())
                                                    .suggests { context, builder -> suggestValues(context, builder) }
                                                    .executes(::setConfig)
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("reload")
                                    .requires { source -> Commands.hasPermission<CommandSourceStack>(Commands.LEVEL_ADMINS).test(source) }
                                    .executes(::reloadConfig)
                            )
                    )
            )
        }
    }

    private fun listConfig(context: CommandContext<CommandSourceStack>): Int {
        val config = AfkToGhostMod.currentConfig()
        context.source.sendSuccess(
            {
                Component.literal(
                    AfkGhostConfig.entries().joinToString(separator = "\n") { entry ->
                        "${entry.key}=${entry.format(config)} - ${entry.description}"
                    }
                )
            },
            false,
        )
        return AfkGhostConfig.entries().size
    }

    private fun getConfig(context: CommandContext<CommandSourceStack>): Int {
        val key = StringArgumentType.getString(context, "key")
        val value = AfkToGhostMod.currentConfig().valueAsString(key) ?: throw unknownKeyError.create(key)
        context.source.sendSuccess({ Component.literal("$key=$value") }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun setConfig(context: CommandContext<CommandSourceStack>): Int {
        val key = StringArgumentType.getString(context, "key")
        val rawValue = StringArgumentType.getString(context, "value")
        val current = AfkToGhostMod.currentConfig()
        val updated = current.withParsedValue(key, rawValue).getOrElse { error ->
            throw DynamicCommandExceptionType { Component.literal(error.message ?: "Invalid value") }.create(rawValue)
        }

        if (!AfkToGhostMod.saveAndApplyConfig(updated)) {
            throw saveFailedError.create()
        }

        context.source.sendSuccess({ Component.literal("Set $key=${updated.valueAsString(key)}") }, true)
        return Command.SINGLE_SUCCESS
    }

    private fun reloadConfig(context: CommandContext<CommandSourceStack>): Int {
        val reloaded = AfkToGhostMod.reloadConfig().getOrElse { error ->
            throw invalidConfigError.create(error.message ?: "check the server log")
        }
        context.source.sendSuccess(
            {
                Component.literal(
                    "Reloaded AFK to Ghost config: " +
                        AfkGhostConfig.entries().joinToString { entry -> "${entry.key}=${entry.format(reloaded)}" }
                )
            },
            true,
        )
        return Command.SINGLE_SUCCESS
    }

    private fun suggestValues(
        context: CommandContext<CommandSourceStack>,
        builder: com.mojang.brigadier.suggestion.SuggestionsBuilder,
    ): java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> {
        val key = runCatching { StringArgumentType.getString(context, "key") }.getOrNull()
        return when (key) {
            "debug", "invisibility", "invulnerable", "actionbar" -> SharedSuggestionProvider.suggest(listOf("true", "false"), builder)
            else -> builder.buildFuture()
        }
    }
}
