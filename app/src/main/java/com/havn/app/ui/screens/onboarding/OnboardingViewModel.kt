package com.havn.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havn.app.data.prefs.UserPreferences
import com.havn.app.data.repository.HavnRepository
import com.havn.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: HavnRepository,
    private val prefs: UserPreferences,
) : ViewModel() {

    fun createUser(
        name: String,
        age: Int,
        color: String,
        onComplete: () -> Unit,
    ) {
        viewModelScope.launch {
            val id = repository.createUser(
                User(name = name, age = age, avatarColor = color)
            )
            prefs.setActiveUser(id)
            prefs.setOnboardingDone(true)
            onComplete()
        }
    }
}
