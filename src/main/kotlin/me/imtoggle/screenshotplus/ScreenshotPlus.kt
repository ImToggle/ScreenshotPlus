package me.imtoggle.screenshotplus

import me.imtoggle.screenshotplus.config.ModConfig
import me.imtoggle.screenshotplus.config.ModConfig.dateFormat
import me.imtoggle.screenshotplus.config.ModConfig.screenShotRootFolder
import me.imtoggle.screenshotplus.tree.FileTreeManager
import me.imtoggle.screenshotplus.util.dateTimeFormatter
import net.fabricmc.api.ClientModInitializer
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.Locale

class ScreenshotPlus : ClientModInitializer {

    override fun onInitializeClient() {
        System.setProperty("java.awt.headless", "false")
        ModConfig.preload()
        FileTreeManager.root = File(screenShotRootFolder)
        dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat, Locale.getDefault())
    }
}