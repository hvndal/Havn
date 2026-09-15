package com.havn.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.data.session.SessionManager
import com.havn.app.data.session.SessionState
import com.havn.app.ui.navigation.HavnNavGraph
import com.havn.app.ui.theme.HavnTheme
import com.havn.app.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    prefs: UserPreferences,
    sessionManager: SessionManager,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode?> = prefs.themePref
        .map { ThemeMode.from(it) }
        // null means "not read yet". The system splash stays up until this
        // resolves, which is what prevents the light-theme flash a dark-mode
        // user used to get on every launch.
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val session: StateFlow<SessionState> = sessionManager.state
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userPreferences: UserPreferences

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Hold the system splash until both the theme and the session are
        // known. Everything after this point can render its final appearance
        // immediately — no flash of the wrong theme, no flash of onboarding
        // for a user who is already signed in.
        splash.setKeepOnScreenCondition {
            viewModel.themeMode.value == null ||
                viewModel.session.value is SessionState.Resolving
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val session by viewModel.session.collectAsStateWithLifecycle()

            HavnTheme(themeMode = themeMode ?: ThemeMode.SYSTEM) {
                HavnNavGraph(session = session)
            }
        }
    }
}
