package com.havn.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.havn.app.data.repository.HavnRepository
import com.havn.app.data.session.SessionManager
import com.havn.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val existingProfiles: List<User> = emptyList(),
    val isSubmitting: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: HavnRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val submitting = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<OnboardingUiState> = combine(
        repository.getAllUsers(),
        submitting,
        error,
    ) { users, isSubmitting, err ->
        OnboardingUiState(
            existingProfiles = users,
            isSubmitting = isSubmitting,
            error = err,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardingUiState())

    /**
     * Creating a profile writes the session; the nav graph reacts to that.
     *
     * No `onComplete` callback: the previous version navigated manually *and*
     * wrote the session, so the two could disagree — navigation fired before
     * DataStore had flushed, and the destination screen read a session that
     * was not there yet.
     */
    fun createProfile(name: String, age: Int?, avatarColor: String) {
        if (name.isBlank() || submitting.value) return
        viewModelScope.launch {
            submitting.value = true
            error.value = null
            runCatching {
                sessionManager.createAndSignIn(
                    name = name.trim(),
                    age = age ?: 0,
                    avatarColor = avatarColor,
                )
            }.onFailure {
                error.value = "Couldn't create that profile. Try again."
            }
            submitting.value = false
        }
    }

    fun signIn(userId: Long) {
        if (submitting.value) return
        viewModelScope.launch {
            submitting.value = true
            error.value = null
            runCatching { sessionManager.signIn(userId) }
                .onFailure { error.value = "Couldn't open that profile." }
            submitting.value = false
        }
    }

    fun dismissError() { error.value = null }
}
