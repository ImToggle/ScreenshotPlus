package me.imtoggle.screenshotplus.mixin;

import androidx.navigation.NavGraphBuilder;
import me.imtoggle.screenshotplus.config.ScreenshotRouteKt;
import org.polyfrost.oneconfig.internal.ui.navigation.NavigationKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NavigationKt.class, remap = false)
public class NavigationMixin {

    @Inject(method = "navigation", at = @At(value = "INVOKE", target = "Lorg/polyfrost/oneconfig/internal/ui/navigation/graph/ThemesKt;themesGraph(Landroidx/navigation/NavGraphBuilder;)V"))
    private static void addNavigation(NavGraphBuilder $this$navigation, CallbackInfo ci) {
        ScreenshotRouteKt.screenshots($this$navigation);
    }
}
