package me.imtoggle.screenshotplus

import me.imtoggle.screenshotplus.config.ModConfig
import me.imtoggle.screenshotplus.config.ModConfig.dateFormat
import me.imtoggle.screenshotplus.config.ModConfig.screenShotRootFolder
import me.imtoggle.screenshotplus.util.dateTimeFormatter
import me.imtoggle.screenshotplus.util.rootFolder
import net.fabricmc.api.ClientModInitializer
import java.time.format.DateTimeFormatter
import java.util.Locale

class ScreenshotPlus : ClientModInitializer {

    override fun onInitializeClient() {
        System.setProperty("java.awt.headless", "false")
        ModConfig.preload()
        rootFolder = java.io.File(screenShotRootFolder)
        dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat, Locale.getDefault())
    }
}