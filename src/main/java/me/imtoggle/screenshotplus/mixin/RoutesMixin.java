package me.imtoggle.screenshotplus.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.imtoggle.screenshotplus.config.ScreenshotsGraph;
import org.polyfrost.oneconfig.internal.ui.navigation.NavigationGroup;
import org.polyfrost.oneconfig.internal.ui.navigation.NavigationRoute;
import org.polyfrost.oneconfig.internal.ui.navigation.RoutesKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(value = RoutesKt.class, remap = false, priority = 900)
public class RoutesMixin {

    @Unique
    private static List<NavigationGroup> groups = new ArrayList<>();

    @ModifyReturnValue(method = "getNavigationGroups", at = @At("RETURN"))
    private static List<NavigationGroup> insertGroup(List<NavigationGroup> original) {
        if (groups.isEmpty()) {
            groups.addAll(original);
            NavigationGroup group = groups.getFirst();
            List<NavigationRoute> routes = new ArrayList<>(Arrays.stream(group.getRoutes()).toList());
            routes.add(new NavigationRoute("screenshots", "/assets/screenshotplus/icons/screenshot.svg", ScreenshotsGraph.INSTANCE));
            groups.set(0, new NavigationGroup(group.getId(), routes.toArray(new NavigationRoute[0])));
        }
        return groups;
    }
}