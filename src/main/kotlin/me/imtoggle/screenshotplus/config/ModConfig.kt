package me.imtoggle.screenshotplus.config

import org.polyfrost.oneconfig.api.config.v1.Config
import org.polyfrost.oneconfig.api.config.v1.Properties
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider
import org.polyfrost.oneconfig.api.config.v1.dsl.category
import org.polyfrost.oneconfig.utils.v1.dsl.mc

object ModConfig : Config("screenshotplus.json", "Screenshot+", Category.UTILITY) {

    @Slider(
        title = "Screenshot Width",
        category = "Settings",
        min = 320f, max = 7680f, step = 10f
    )
    var screenShotWidth = 1920

    @Slider(
        title = "Screenshot Height",
        category = "Settings",
        min = 240f, max = 4320f, step = 10f
    )
    var screenShotHeight = 1080

//    override fun makeTree(): Tree? {
//        val tree = super.makeTree()
//        tree.addMetadata("on_click") {
//            mc.setScreenAndShow(ModScreen())
//        }
//        return tree
//    }




}