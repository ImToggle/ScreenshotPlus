package me.imtoggle.screenshotplus.config

import me.imtoggle.screenshotplus.util.dateTimeFormatter
import me.imtoggle.screenshotplus.util.rootFolder
import org.polyfrost.oneconfig.api.config.v1.Config
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown
import org.polyfrost.oneconfig.api.config.v1.annotations.File
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.config.v1.annotations.Text
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import java.time.format.DateTimeFormatter
import java.util.Locale

object ModConfig : Config("screenshotplus.json", "Screenshot+", Category.UTILITY) {

    @Switch(
        title = "Hide GUI"
    )
    var hideGUI = false

    @Slider(
        title = "Screenshot Delay",
        min = 0f, max = 200f, step = 1f
    )
    var delay = 0

    @Switch(
        title = "Use Custom Size"
    )
    var customSize = false

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
    )
    var dateFormat = "yyyy-MM"

    init {
        addCallback("screenShotRootFolder") {
            rootFolder = java.io.File(screenShotRootFolder)
        }
        addCallback("dateFormat") {
            dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat, Locale.getDefault())
        }
        hideIf("dateFormat") { organizeRule != 1 }
        addDependency("screenShotWidth", "customSize")
        addDependency("screenShotHeight", "customSize")
    }

}