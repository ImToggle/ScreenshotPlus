package me.imtoggle.screenshotplus.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.mojang.blaze3d.platform.InputConstants
import me.imtoggle.screenshotplus.util.quit
import net.minecraft.client.input.KeyEvent
import org.polyfrost.oneconfig.api.platform.v1.Platform
import org.polyfrost.oneconfig.internal.ui.compose.ComposeScreen
import org.polyfrost.oneconfig.internal.ui.compose.impls.OneConfigUIScreen
import org.polyfrost.oneconfig.internal.ui.keybind.KeybindRecordingBus
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme
import org.polyfrost.oneconfig.internal.ui.themes.Theme

class EditorScreen : ComposeScreen() {

    //? >= 1.21.10 {
    override fun keyPressed(event: KeyEvent): Boolean {
        val key = event.key
        //? } else {
        /*override fun keyPressed(key: Int, scanCode: Int, modifiers: Int): Boolean {
        *///? }
        if (key == InputConstants.KEY_ESCAPE) {
            if (KeybindRecordingBus.consumeEscape()) return true
            quit = true
            Platform.screen().display(OneConfigUIScreen())
            return true
        }
        //? >= 1.21.10 {
        return super.keyPressed(event)
        //? } else {
        /*return super.keyPressed(key, scanCode, modifiers)
        *///? }
    }

    @Composable
    override fun compose() {
        Theme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocalTheme.current.pageBackground.copy(alpha = 1f)),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset()
                        .border(2.dp, LocalTheme.current.borderColor.copy(alpha = 0.75f))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->

                                }
                            )
                        }
                ) {

                }
            }
        }
    }

    @Composable
    fun Section() {

    }
}