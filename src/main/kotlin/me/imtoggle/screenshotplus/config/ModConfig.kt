package me.imtoggle.screenshotplus.config

import me.imtoggle.screenshotplus.screen.currentPath
import me.imtoggle.screenshotplus.util.dateTimeFormatter
import me.imtoggle.screenshotplus.util.rootFolder
import org.polyfrost.oneconfig.api.config.v1.Config
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown
import org.polyfrost.oneconfig.api.config.v1.annotations.File
import org.polyfrost.oneconfig.api.config.v1.annotations.RadioButton
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.config.v1.annotations.Text
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import java.time.format.DateTimeFormatter
import java.util.Locale

object ModConfig : Config("screenshotplus.json", "/assets/screenshotplus/icon_dark.svg", "Screenshot+", Category.UTILITY) {

    @Switch(
        title = "Hide GUI"
    )
    var hideGUI = false

    @Slider(
        title = "Screenshot Delay (Frame)",
        min = 0f, max = 200f, step = 1f
    )
    var delay = 0

    @Switch(
        title = "Use Custom Resolution"
    )
    var customSize = false

    @RadioButton(
        title = "Resize Mode",
        options = ["Multiplier", "Absolute"]
    )
    var resizeMode = 0

    @Slider(
        title = "Scale",
        min = 0.1f, max = 4f, step = 0.1f
    )
    var screenShotScale = 1f

    @Slider(
        title = "Screenshot Width",
        min = 320f, max = 7680f, step = 10f
    )
    var screenShotWidth = 1920

    @Slider(
        title = "Screenshot Height",
        min = 240f, max = 4320f, step = 10f
    )
    var screenShotHeight = 1080

    @File(
        title = "Screenshot Root Folder",
        filterName = "Folder",
        directory = true
    )
    var screenShotRootFolder = java.io.File(mc.gameDirectory, "/screenshots").absolutePath

    @Dropdown(
        title = "Organized by",
        options = ["None", "Date", "World/Server"]
    )
    var organizeRule = 0

    @Text(
        title = "Date Format",
        description = "y: Year, M: Month, d: Day"
    )
    var dateFormat = "yyyy-MM"

    @Switch(
        title = "Copy Screenshot",
        description = "Automatically copy screenshot"
    )
    var copyScreenshot = false

    init {
        addCallback("screenShotRootFolder") {
            rootFolder = java.io.File(screenShotRootFolder)
            currentPath = mutableListOf(screenShotRootFolder)
        }
        addCallback("dateFormat") {
            dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat, Locale.getDefault())
        }
        hideIf("dateFormat") { organizeRule != 1 }
        arrayOf("resizeMode", "screenShotScale", "screenShotWidth", "screenShotHeight").forEach { option ->
            hideIf(option) { !customSize }
        }
        hideIf("screenShotScale") { resizeMode == 1 }
        hideIf("screenShotWidth") { resizeMode == 0 }
        hideIf("screenShotHeight") { resizeMode == 0 }
    }

}