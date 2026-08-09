package me.imtoggle.screenshotplus.mixin;

import me.imtoggle.screenshotplus.config.ScreenshotsGraph;
import me.imtoggle.screenshotplus.screen.ScreenshotsKt;
import org.polyfrost.oneconfig.internal.ui.shell.LocalNavController;
import org.polyfrost.oneconfig.internal.ui.shell.ShellState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalNavController.NavControllerWrapper.class)
public class NavControllerWrapperMixin {

    @Inject(method = "back", at = @At("HEAD"), cancellable = true)
    private void back(CallbackInfo ci) {
        if (ShellState.INSTANCE.getLastRoute() instanceof ScreenshotsGraph) {
            ScreenshotsKt.pageBack();
            ci.cancel();
        }
    }

    @Inject(method = "forward", at = @At("HEAD"), cancellable = true)
    private void forward(CallbackInfo ci) {
        if (ShellState.INSTANCE.getLastRoute() instanceof ScreenshotsGraph) {
            ScreenshotsKt.pageForward();
            ci.cancel();
        }
    }
}