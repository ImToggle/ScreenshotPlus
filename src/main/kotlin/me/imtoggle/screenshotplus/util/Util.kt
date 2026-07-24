@file:JvmName("Util")

package me.imtoggle.screenshotplus.util

import androidx.compose.ui.util.fastForEach
import me.imtoggle.screenshotplus.config.ModConfig
import net.minecraft.client.Screenshot
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

val NONE = 0

val DATE = 1

val WORLD = 2

var dateTimeFormatter = DateTimeFormatter.ofPattern(ModConfig.dateFormat, Locale.getDefault())

var rootFolder = File(ModConfig.screenShotRootFolder)

val capturing
    get() = tasks.isNotEmpty()

var tasks = ArrayList<ScreenshotInfo>()

var toggled = false

data class ScreenshotInfo(var frame: Int)

fun startCapture() {
    if (!capturing && !mc.gui.hud.isHidden && ModConfig.hideGUI) {
        toggled = true
        mc.gui.hud.toggle()
    }
    tasks.add(ScreenshotInfo(ModConfig.delay))
    if (ModConfig.customSize) {
        mc.window.width = ModConfig.screenShotWidth
        mc.window.height = ModConfig.screenShotHeight
        mc.resizeGui()
    }
}

fun update() {
    if (!capturing) {
        if (toggled) {
            toggled = false
            mc.gui.hud.toggle()
        }
        if (ModConfig.customSize) {
            mc.window.width = mc.window.screenWidth
            mc.window.height = mc.window.screenHeight
            mc.resizeGui()
        }
    }
}

fun handle() {
    if (!capturing) return
    tasks.fastForEach { task ->
        if (--task.frame < 0) {
            Screenshot.grab(mc, false)
            tasks.removeFirst()
            update()
        }
    }

}

fun getWorldName(): String {
    mc.singleplayerServer?.let { return it.worldData.levelName }
    mc.currentServer?.let { server -> return (server.name.takeIf { it != "Minecraft Server" } ?: server.ip) }
    return ""
}

fun getFolder(): File {
    return when(ModConfig.organizeRule) {
        DATE -> rootFolder.resolve("${dateTimeFormatter.format(LocalDateTime.now())}")
        WORLD -> rootFolder.resolve(getWorldName())
        else -> rootFolder.resolve(getFolder())
    }
}