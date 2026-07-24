package me.imtoggle.screenshotplus

import me.imtoggle.screenshotplus.config.ModConfig
import net.fabricmc.api.ClientModInitializer

class ScreenshotPlus : ClientModInitializer {

    override fun onInitializeClient() {
        ModConfig.preload()
    }
}