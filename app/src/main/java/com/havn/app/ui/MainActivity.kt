package com.havn.app.ui

import android.app.UiModeManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
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
        // known, but never longer than 1500ms so slow devices never ANR freeze.
        val splashStartTime = System.currentTimeMillis()
        splash.setKeepOnScreenCondition {
            if (System.currentTimeMillis() - splashStartTime > 1500L) {
                false
            } else {
                viewModel.themeMode.value == null ||
                    viewModel.session.value is SessionState.Resolving
            }
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val session by viewModel.session.collectAsStateWithLifecycle()

            // Tell Android which mode the app is in. Without this, choosing
            // Light while the phone is in dark mode left the launch window,
            // splash and the 3D organizer WebView in dark.
            LaunchedEffect(themeMode) {
                val mode = themeMode ?: return@LaunchedEffect
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    runCatching {
                        getSystemService(UiModeManager::class.java)?.setApplicationNightMode(
                            when (mode) {
                                ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
                                ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
                                ThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
                            }
                        )
                    }
                }
            }

            HavnTheme(themeMode = themeMode ?: ThemeMode.LIGHT) {
                HavnNavGraph(session = session)
            }
        }
    }
}
