package me.imtoggle.screenshotplus.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import me.imtoggle.screenshotplus.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    @Final
    public Options options;

    @Inject(method = "renderFrame", at = @At(value = "TAIL"))
    private void takeScreenshot(CallbackInfo ci) {
        Util.handle();
    }

    @Inject(method = "handleGlobalKeyPress", at = @At("HEAD"), cancellable = true)
    private void start(InputConstants.Key key, boolean controlDown, CallbackInfoReturnable<Boolean> cir) {
        if (options.keyScreenshot.matches(key)) {
            Util.startCapture();
            cir.setReturnValue(true);
        }
    }
}