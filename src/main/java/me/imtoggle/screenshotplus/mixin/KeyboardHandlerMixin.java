package me.imtoggle.screenshotplus.mixin;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;

//? if < 26.2 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import kotlin.Unit;
import me.imtoggle.screenshotplus.util.Util;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.injection.At;
import java.io.File;
import java.util.function.Consumer;
//? }

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    //? if < 26.2 {
    /*@WrapOperation(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Screenshot;grab(Ljava/io/File;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V"))
    private void start(File workDir, RenderTarget target, Consumer<Component> callback, Operation<Void> original) {
        Util.startCapture((() -> {
            original.call(workDir, target, callback);
            return Unit.INSTANCE;
        }));
    }
    *///? }

}