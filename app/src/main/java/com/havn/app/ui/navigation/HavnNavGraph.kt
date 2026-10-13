package com.havn.app.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.havn.app.data.session.SessionState
import com.havn.app.ui.screens.addmed.AddMedicationScreen
import com.havn.app.ui.screens.home.HomeScreen
import com.havn.app.ui.screens.managemeds.ManageMedicationsScreen
import com.havn.app.ui.screens.onboarding.OnboardingScreen
import com.havn.app.ui.screens.organizer.OrganizerScreen
import com.havn.app.ui.screens.progress.ProgressScreen
import com.havn.app.ui.screens.reminders.RemindersScreen
import com.havn.app.ui.screens.settings.SettingsScreen
import com.havn.app.ui.theme.HavnMotion
import com.havn.app.ui.theme.HavnTheme

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Organizer : Screen("organizer")
    data object Progress : Screen("progress")
    data object Reminders : Screen("reminders")
    data object Settings : Screen("settings")
    data object AddMed : Screen("add_medication")
    data object EditMed : Screen("edit_medication/{medId}") {
        fun create(medId: Long) = "edit_medication/$medId"
        const val ARG = "medId"
    }
    data object ManageMeds : Screen("manage_medications")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: String,
    val activeIcon: String,
)

/**
 * Four destinations, not five.
 *
 * "Adherence" and "History" were separate tabs showing the same 30 days of dose
 * data in two shapes; they are now two views of one Progress screen. Beyond the
 * clutter, five labelled tabs could not fit a 320dp phone — each item carried
 * 36dp of fixed horizontal padding, so the row overflowed and the last tab was
 * clipped off-screen on small devices.
 */
val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Today", "home", "home_fill"),
    BottomNavItem(Screen.Organizer, "Organizer", "pill", "pill_fill"),
    BottomNavItem(Screen.Progress, "Progress", "chart", "chart_fill"),
    BottomNavItem(Screen.Settings, "Settings", "settings", "settings_fill"),
)

/**
 * Top-level routing is driven by [SessionState], not by navigation calls.
 *
 * The signed-in and signed-out graphs are separate `NavHost`s, swapped by the
 * session. That structurally removes a whole class of bug the previous graph
 * had: sign-out navigated to onboarding with `popUpTo(Home) { inclusive =
 * false }`, which left Home on the back stack, so pressing back after signing
 * out returned to a signed-in screen bound to a profile that no longer existed.
 * Here, signing out destroys the authenticated graph outright.
 */
@Composable
fun HavnNavGraph(session: SessionState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HavnTheme.colors.canvas)
    ) {
        AnimatedContent(
            targetState = session::class,
            transitionSpec = {
                // A dissolve, not a slide. Signing in or out is a change of
                // context, not a step forward or back through a hierarchy.
                fadeIn(tween(HavnMotion.Considered, easing = HavnMotion.Enter)) togetherWith
                    fadeOut(tween(HavnMotion.Quick, easing = HavnMotion.Exit))
            },
            label = "sessionGate",
        ) { stateClass ->
            when (stateClass) {
                SessionState.Resolving::class -> {
                    // The system splash is still up; painting the canvas keeps
                    // the handoff seamless rather than flashing a blank frame.
                    Box(Modifier.fillMaxSize().background(HavnTheme.colors.canvas))
                }
                SessionState.SignedOut::class -> AuthGraph()
                else -> MainGraph()
            }
        }
    }
}

@Composable
private fun AuthGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route,
    ) {
        composable(Screen.Onboarding.route) {
            // No onDone callback: completing onboarding writes the session,
            // and the session swaps this graph out. Navigation never has to
            // agree with auth state, because it is derived from it.
            OnboardingScreen()
        }
    }
}

@Composable
private fun MainGraph() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    HavnScaffold(
        currentRoute = currentRoute,
        onNavigate = { screen -> navController.navigateToTab(screen) },
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize(),
            // Tab switches cross-dissolve with a whisper of scale — lateral
            // moves between peers should not imply direction.
            enterTransition = {
                fadeIn(tween(HavnMotion.Standard, easing = HavnMotion.Enter)) +
                    scaleIn(initialScale = 0.985f, animationSpec = tween(HavnMotion.Standard, easing = HavnMotion.Enter))
            },
            exitTransition = { fadeOut(tween(HavnMotion.Quick, easing = HavnMotion.Exit)) },
            popEnterTransition = { fadeIn(tween(HavnMotion.Standard, easing = HavnMotion.Enter)) },
            popExitTransition = { fadeOut(tween(HavnMotion.Quick, easing = HavnMotion.Exit)) },
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    contentPadding = contentPadding,
                    onAddMedication = { navController.navigate(Screen.AddMed.route) },
                    onOpenOrganizer = { navController.navigateToTab(Screen.Organizer) },
                    onManageMedications = { navController.navigate(Screen.ManageMeds.route) },
                    onOpenProgress = { navController.navigateToTab(Screen.Progress) },
                    onOpenReminders = { navController.navigate(Screen.Reminders.route) },
                )
            }

            composable(Screen.Organizer.route) {
                OrganizerScreen(contentPadding = contentPadding)
            }

            composable(Screen.Progress.route) {
                ProgressScreen(contentPadding = contentPadding)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    contentPadding = contentPadding,
                    onManageMedications = { navController.navigate(Screen.ManageMeds.route) },
                    onOpenReminders = { navController.navigate(Screen.Reminders.route) },
                )
            }

            // ── Pushed screens ───────────────────────────────────────────────
            // These rise from the bottom: they are modal tasks layered over the
            // tab you were on, and the motion should say so.
            modalDestination(Screen.ManageMeds.route) {
                ManageMedicationsScreen(
                    onBack = { navController.popBackStack() },
                    onAddMedication = { navController.navigate(Screen.AddMed.route) },
                    onEditMedication = { id -> navController.navigate(Screen.EditMed.create(id)) },
                )
            }

            modalDestination(Screen.AddMed.route) {
                AddMedicationScreen(onBack = { navController.popBackStack() })
            }

            modalDestination(
                route = Screen.EditMed.route,
                arguments = listOf(
                    androidx.navigation.navArgument(Screen.EditMed.ARG) {
                        type = androidx.navigation.NavType.LongType
                    }
                ),
            ) {
                AddMedicationScreen(onBack = { navController.popBackStack() })
            }

            modalDestination(Screen.Reminders.route) {
                RemindersScreen(
                    onBack = { navController.popBackStack() },
                    onAddMedication = { navController.navigate(Screen.AddMed.route) },
                    onEditMedication = { id -> navController.navigate(Screen.EditMed.create(id)) },
                )
            }
        }
    }
}

/**
 * A pushed, task-shaped destination. Enters from below and leaves the same way,
 * so the layering is legible without a visible "card" chrome.
 */
private fun androidx.navigation.NavGraphBuilder.modalDestination(
    route: String,
    arguments: List<androidx.navigation.NamedNavArgument> = emptyList(),
    content: @Composable androidx.compose.animation.AnimatedContentScope.(androidx.navigation.NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        arguments = arguments,
        enterTransition = {
            slideInVertically(
                initialOffsetY = { it / 6 },
                animationSpec = tween(HavnMotion.Considered, easing = HavnMotion.Enter),
            ) + fadeIn(tween(HavnMotion.Standard, easing = HavnMotion.Enter))
        },
        exitTransition = { fadeOut(tween(HavnMotion.Quick, easing = HavnMotion.Exit)) },
        popEnterTransition = { fadeIn(tween(HavnMotion.Standard, easing = HavnMotion.Enter)) },
        popExitTransition = {
            slideOutVertically(
                targetOffsetY = { it / 6 },
                animationSpec = tween(HavnMotion.Standard, easing = HavnMotion.Exit),
            ) + fadeOut(tween(HavnMotion.Quick, easing = HavnMotion.Exit))
        },
        content = content,
    )
}

private fun NavHostController.navigateToTab(screen: Screen) {
    if (currentDestination?.route == screen.route) return
    navigate(screen.route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
