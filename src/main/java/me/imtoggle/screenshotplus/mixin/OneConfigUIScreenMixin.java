package me.imtoggle.screenshotplus.mixin;

import me.imtoggle.screenshotplus.config.ScreenshotsRoute;
import me.imtoggle.screenshotplus.util.Util;
import org.polyfrost.oneconfig.internal.ui.compose.impls.OneConfigUIScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OneConfigUIScreen.class)
public class OneConfigUIScreenMixin {

    @Inject(method = "resolveOpeningBehaviorRoute", at = @At("HEAD"), cancellable = true)
    private void navigate(CallbackInfoReturnable<Object> cir) {
        if (Util.quit) {
            cir.setReturnValue(ScreenshotsRoute.INSTANCE);
            Util.quit = false;
        }
    }
}