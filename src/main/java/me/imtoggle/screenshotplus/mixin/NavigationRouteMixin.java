package me.imtoggle.screenshotplus.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.polyfrost.oneconfig.internal.ui.navigation.NavigationRoute;
import org.polyfrost.oneconfig.internal.ui.themes.ThemeRegistry;
import org.polyfrost.oneconfig.internal.ui.themes.UITheme;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NavigationRoute.class)
public class NavigationRouteMixin {

    @ModifyReturnValue(method = "getIcon", at = @At("RETURN"))
    private String themed(String original) {
        if (!original.equals("/assets/screenshotplus/icons/screenshot.svg")) return original;
        UITheme theme = ThemeRegistry.INSTANCE.getActiveTheme$internal();
        if (theme != null && theme.getName().contains("Minecraft")) {
            return "/assets/screenshotplus/icons/minecraft/screenshot.svg";
        }
        return original;
    }
}