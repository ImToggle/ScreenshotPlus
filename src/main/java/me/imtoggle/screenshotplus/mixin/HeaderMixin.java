package me.imtoggle.screenshotplus.mixin;

import androidx.compose.animation.AnimatedContentScope;
import androidx.compose.runtime.Composer;
import kotlin.Unit;
import kotlin.jvm.functions.Function4;
import me.imtoggle.screenshotplus.config.ScreenshotsGraph;
import me.imtoggle.screenshotplus.screen.ScreenshotsKt;
import org.polyfrost.oneconfig.internal.ui.components.HeaderKt;
import org.polyfrost.oneconfig.internal.ui.shell.ShellState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = HeaderKt.class, remap = false)
public class HeaderMixin {

    @ModifyArg(
            method = "Header",
            at = @At(
                    value = "INVOKE",
                    target = "Landroidx/compose/animation/AnimatedContentKt;AnimatedContent(Ljava/lang/Object;Landroidx/compose/ui/Modifier;Lkotlin/jvm/functions/Function1;Landroidx/compose/ui/Alignment;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function4;Landroidx/compose/runtime/Composer;II)V"
            ),
            index = 6, remap = false
    )
    private static Function4<? super AnimatedContentScope, ? super String, ? super Composer, ? super Integer, Unit> wrapTextCall(Function4<? super AnimatedContentScope, ? super String, ? super Composer, ? super Integer, Unit> content) {
        if (ShellState.INSTANCE.getLastRoute() instanceof ScreenshotsGraph) {
            return (scope, text, $composer, $changed) -> {
                ScreenshotsKt.ScreenshotHeader($composer, $changed);
                return Unit.INSTANCE;
            };
        }
        return content;
    }
}