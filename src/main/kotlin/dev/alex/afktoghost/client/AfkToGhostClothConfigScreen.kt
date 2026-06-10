package dev.alex.afktoghost.client

import dev.alex.afktoghost.AfkGhostConfig
import dev.alex.afktoghost.AfkToGhostMod
import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import java.util.Optional

object AfkToGhostClothConfigScreen {
    fun create(parent: Screen): Screen {
        val current = AfkToGhostMod.currentConfig()
        var timeoutSeconds = current.timeoutSeconds
        var debug = current.debug
        var invisibility = current.invisibility
        var invulnerable = current.invulnerable
        var actionbar = current.actionbar

        val builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("AFK to Ghost"))

        val entryBuilder = builder.entryBuilder()
        val category = builder.getOrCreateCategory(Component.literal("Settings"))

        category.addEntry(
            entryBuilder.startTextDescription(Component.literal(reloadNotice()))
                .build()
        )

        category.addEntry(
            entryBuilder.startLongField(Component.literal("AFK timeout seconds"), timeoutSeconds)
                .setDefaultValue(AfkGhostConfig().timeoutSeconds)
                .setMin(1L)
                .setTooltip(Component.literal("Players enter AFK Ghost mode after this many inactive seconds."))
                .setErrorSupplier { value ->
                    if (value < 1L) Optional.of(Component.literal("Timeout must be at least 1 second."))
                    else Optional.empty()
                }
                .setSaveConsumer { value -> timeoutSeconds = value }
                .requireRestart()
                .build()
        )

        category.addEntry(
            entryBuilder.startBooleanToggle(Component.literal("Debug logging"), debug)
                .setDefaultValue(AfkGhostConfig().debug)
                .setTooltip(Component.literal("Writes diagnostic AFK to Ghost events to the log."))
                .setSaveConsumer { value -> debug = value }
                .requireRestart()
                .build()
        )

        category.addEntry(
            entryBuilder.startBooleanToggle(Component.literal("Invisibility"), invisibility)
                .setDefaultValue(AfkGhostConfig().invisibility)
                .setTooltip(Component.literal("Applies invisibility while a player is in AFK Ghost mode."))
                .setSaveConsumer { value -> invisibility = value }
                .requireRestart()
                .build()
        )

        category.addEntry(
            entryBuilder.startBooleanToggle(Component.literal("Damage protection"), invulnerable)
                .setDefaultValue(AfkGhostConfig().invulnerable)
                .setTooltip(Component.literal("Prevents incoming damage while a player is in AFK Ghost mode."))
                .setSaveConsumer { value -> invulnerable = value }
                .requireRestart()
                .build()
        )

        category.addEntry(
            entryBuilder.startBooleanToggle(Component.literal("Title and actionbar feedback"), actionbar)
                .setDefaultValue(AfkGhostConfig().actionbar)
                .setTooltip(Component.literal("Shows title and actionbar feedback while a player is in AFK Ghost mode."))
                .setSaveConsumer { value -> actionbar = value }
                .requireRestart()
                .build()
        )

        builder.setSavingRunnable {
            AfkToGhostMod.saveConfigFromScreen(
                AfkGhostConfig(
                    timeoutSeconds = timeoutSeconds,
                    debug = debug,
                    invisibility = invisibility,
                    invulnerable = invulnerable,
                    actionbar = actionbar,
                )
            )
        }

        return builder.build()
    }

    private fun reloadNotice(): String {
        return if (AfkToGhostMod.configChangesRequireWorldRestart()) {
            "Settings are saved to config/afk-to-ghost.json. Reopen the world or restart the server for changes to take effect."
        } else {
            "Settings are saved to config/afk-to-ghost.json. They will be used by the next world or server you open."
        }
    }
}
