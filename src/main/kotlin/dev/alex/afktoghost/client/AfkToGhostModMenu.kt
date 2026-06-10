package dev.alex.afktoghost.client

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.alex.afktoghost.AfkToGhostMod
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screens.Screen

class AfkToGhostModMenu : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<out Screen> {
        return ConfigScreenFactory { parent ->
            if (FabricLoader.getInstance().isModLoaded("cloth-config")) {
                AfkToGhostClothConfigScreen.create(parent)
            } else {
                AfkToGhostMod.LOGGER.warn("Mod Menu config screen requested, but Cloth Config is not installed")
                parent
            }
        }
    }
}
