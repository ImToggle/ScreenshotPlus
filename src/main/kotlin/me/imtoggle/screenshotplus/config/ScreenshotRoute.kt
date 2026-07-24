package me.imtoggle.screenshotplus.config

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import me.imtoggle.screenshotplus.screen.Screenshots
import org.polyfrost.oneconfig.internal.ui.screens.ModConfig
import org.polyfrost.oneconfig.internal.ui.screens.Mods

@Serializable
data object ScreenshotsGraph

@Serializable
data object ScreenshotsRoute

@Serializable
data class ModConfigRoute(val id: String, val category: String? = null)

fun NavGraphBuilder.screenshots() {
    navigation<ScreenshotsGraph>(startDestination = ScreenshotsRoute) {
        composable<ScreenshotsRoute> {
            Screenshots()
        }
        composable<ModConfigRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ModConfigRoute>()
            ModConfig(route.id, route.category)
        }
    }
}
