package me.imtoggle.screenshotplus.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import kotlin.Unit;
import me.imtoggle.screenshotplus.util.Util;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    //~ if >= 26.1 'runTick' -> 'renderFrame'
    @Inject(method = "renderFrame", at = @At(value = "TAIL"))
    private void takeScreenshot(CallbackInfo ci) {
        Util.handleScreenshot();
    }

    //? if >= 26.2 {
    @WrapOperation(method = "handleGlobalKeyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Screenshot;grab(Lnet/minecraft/client/Minecraft;Z)V"))
    private void start(Minecraft minecraft, boolean debugPanoramaRequested, Operation<Void> original) {
        Util.startCapture(() -> {
            original.call(minecraft, debugPanoramaRequested);
            return Unit.INSTANCE;
        });
    }
    //? }

}