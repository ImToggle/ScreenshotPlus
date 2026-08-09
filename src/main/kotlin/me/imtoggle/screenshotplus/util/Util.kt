@file:JvmName("Util")

package me.imtoggle.screenshotplus.util

import androidx.compose.ui.util.fastForEach
import com.mojang.blaze3d.platform.NativeImage
import me.imtoggle.screenshotplus.config.ModConfig
import me.imtoggle.screenshotplus.tree.FileTreeManager
import org.polyfrost.oneconfig.utils.v1.ClipboardHelper
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.polyfrost.oneconfig.utils.v1.dsl.runAsync
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private val DATE = 1

private val WORLD = 2

var dateTimeFormatter = DateTimeFormatter.ofPattern(ModConfig.dateFormat, Locale.getDefault())

val capturing
    get() = tasks.isNotEmpty()

private val tasks = ArrayList<ScreenshotInfo>()

var toggled = false

data class ScreenshotInfo(var frame: Int, val function: () -> Unit)

val isGuiHidden: Boolean
    get() {
        //? if >= 26.2 {
        return mc.gui.hud.isHidden
        //? } else {
        /*return mc.options.hideGui
        *///? }
    }

fun toggleGui(on: Boolean) {
    //? if >= 26.2 {
    if (isGuiHidden == on) mc.gui.hud.toggle()
    //? } else {
    /*mc.options.hideGui = !on
    *///? }
}

fun resizeMC() {
    //? if >= 26.1 {
    mc.resizeGui()
    //? } else {
    /*mc.resizeDisplay()
    *///? }
}

fun startCapture(function: () -> Unit) {
    if (!capturing && !isGuiHidden && ModConfig.hideGUI) {
        toggled = true
        toggleGui(false)
    }
    if (ModConfig.customSize) {
        if (ModConfig.resizeMode == 1) {
            mc.window.width = ModConfig.screenShotWidth
            mc.window.height = ModConfig.screenShotHeight
        } else {
            mc.window.width = (mc.window.width * ModConfig.screenShotScale).toInt()
            mc.window.height = (mc.window.height * ModConfig.screenShotScale).toInt()
        }
        resizeMC()
    }
    tasks.add(ScreenshotInfo(if (ModConfig.customSize || ModConfig.delay == 0 && toggled) 1 else ModConfig.delay, function))
}

fun update() {
    if (!capturing) {
        if (toggled) {
            toggled = false
            toggleGui(true)
        }
        if (ModConfig.customSize) {
            mc.window.width = mc.window.screenWidth
            mc.window.height = mc.window.screenHeight
            resizeMC()
        }
    }
}

fun handleScreenshot() {
    if (!capturing) return
    tasks.fastForEach { task ->
        if (--task.frame < 0) {
            task.function.invoke()
            tasks.remove(task)
            update()
        }
    }
}

fun handleCallback(image: NativeImage, file: File) {
    if (ModConfig.copyScreenshot) {
        runAsync {
            ClipboardHelper.setTransferable(MultiFiles(file))
        }
    }
}

fun getWorldName(): String {
    mc.singleplayerServer?.let { return it.worldData.levelName }
    mc.currentServer?.let { server -> return (server.name.takeIf { it != "Minecraft Server" } ?: server.ip) }
    return ""
}

fun getFolder(): File {
    return with(FileTreeManager.root) {
        when (ModConfig.organizeRule) {
            DATE -> resolve("${dateTimeFormatter.format(LocalDateTime.now())}")
            WORLD -> resolve(getWorldName())
            else -> this
        }
    }
}