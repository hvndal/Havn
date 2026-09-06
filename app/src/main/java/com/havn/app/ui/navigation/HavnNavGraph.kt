package com.havn.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.ui.screens.addmed.AddMedicationScreen
import com.havn.app.ui.screens.history.HistoryScreen
import com.havn.app.ui.screens.home.HomeScreen
import com.havn.app.ui.screens.onboarding.OnboardingScreen
import com.havn.app.ui.screens.organizer.OrganizerScreen
import com.havn.app.ui.screens.settings.SettingsScreen
import com.havn.app.ui.screens.splash.SplashScreen
import javax.inject.Inject

sealed class Screen(val route: String) {
    object Splash      : Screen("splash")
    object Onboarding  : Screen("onboarding")
    object Home        : Screen("home")
    object Organizer   : Screen("organizer")
    object History     : Screen("history")
    object Settings    : Screen("settings")
    object AddMed      : Screen("add_medication")
}

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home,      "Ritual",    "home",          "home_fill"),
    BottomNavItem(Screen.Organizer, "Organizer", "pill",          "pill_fill"),
    BottomNavItem(Screen.History,   "History",   "calendar_today","calendar_today"),
    BottomNavItem(Screen.Settings,  "Settings",  "settings",      "settings_fill"),
)

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: String,
    val activeIcon: String,
)

@Composable
fun HavnNavGraph() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDest = currentBackStack?.destination
    val mainRoutes = setOf(
        Screen.Home.route, Screen.Organizer.route,
        Screen.History.route, Screen.Settings.route
    )
    val showBottomBar = currentDest?.route in mainRoutes

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                HavnBottomBar(navController = navController, currentDest = currentDest)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(
                route = Screen.Splash.route,
                exitTransition = { fadeOut() },
            ) {
                SplashScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = Screen.Onboarding.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() },
            ) {
                OnboardingScreen(
                    onDone = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = Screen.Home.route,
                enterTransition = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                    slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { it / 8 }
                },
                exitTransition = { fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) },
                popEnterTransition = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                    slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { -it / 8 }
                },
                popExitTransition = { fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) },
            ) {
                HomeScreen(
                    onAddMedication = { navController.navigate(Screen.AddMed.route) },
                    onOpenOrganizer = { navController.navigate(Screen.Organizer.route) },
                )
            }
            composable(
                route = Screen.Organizer.route,
                enterTransition = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                    slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { it / 6 }
                },
                exitTransition = { fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) },
                popEnterTransition = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                    slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { -it / 6 }
                },
                popExitTransition = {
                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                    slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { it / 6 }
                },
            ) {
                OrganizerScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.History.route,
                enterTransition = { fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) },
                exitTransition = { fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) },
                popEnterTransition = { fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) },
                popExitTransition = { fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) },
            ) {
                HistoryScreen()
            }
            composable(
                route = Screen.Settings.route,
                enterTransition = { fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) },
                exitTransition = { fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) },
                popEnterTransition = { fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) },
                popExitTransition = { fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) },
            ) {
                SettingsScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }
            composable(
                route = Screen.AddMed.route,
                enterTransition = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                    slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioLowBouncy)) { it / 3 }
                },
                exitTransition = {
                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                    slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { it / 3 }
                },
                popEnterTransition = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                },
                popExitTransition = {
                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                    slideOutVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)) { it / 3 }
                },
            ) {
                AddMedicationScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
