package me.imtoggle.screenshotplus.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import me.imtoggle.screenshotplus.util.Util;
import net.minecraft.client.Screenshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

@Mixin(Screenshot.class)
public class ScreenshotMixin {

    @ModifyVariable(
            method =
            //? if >= 26.2 {
            "lambda$grab$2"
            //? } else {
            /*"lambda$grab$0"
            *///? }
            , at = @At("STORE"), ordinal = 1)
    private static File replaceDirectory(File picDir) {
        return Util.getFolder();
    }

    @Inject(method = 
            //? if >= 26.2 {
            "lambda$grab$3"
            //? } else {
            /*"lambda$grab$1"
            *///? }
            , at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;writeToFile(Ljava/io/File;)V", shift = At.Shift.AFTER))
    private static void handleCallback(NativeImage image, File file, Consumer<?> callback, CallbackInfo ci) {
        Util.handleCallback(image, file);
    }

}