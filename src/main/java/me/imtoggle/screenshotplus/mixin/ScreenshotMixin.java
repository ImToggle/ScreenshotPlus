package me.imtoggle.screenshotplus.mixin;

import me.imtoggle.screenshotplus.util.Util;
import net.minecraft.client.Screenshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.File;

@Mixin(Screenshot.class)
public class ScreenshotMixin {

    @ModifyVariable(method = "lambda$grab$2", at = @At("STORE"), ordinal = 1)
    private static File replaceDirectory(File picDir) {
        return Util.getFolder();
    }
}