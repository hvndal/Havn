package com.havn.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import com.havn.app.data.prefs.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val prefs: UserPreferences,
) : ViewModel() {

    suspend fun needsOnboarding(): Boolean {
        return !prefs.onboardingDone.first()
    }
}
