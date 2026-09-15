package com.havn.app.data.session

import com.havn.app.data.prefs.UserPreferences
import com.havn.app.data.repository.HavnRepository
import com.havn.app.domain.model.User
import com.havn.app.notifications.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Who is signed in, resolved once for the whole app.
 *
 * [Resolving] is a real state, not a nuisance — DataStore and Room are both
 * async, so for the first frames after launch the honest answer is "not known
 * yet". Screens that treat that as "signed out" are what produce the flash of
 * the login screen on every cold start.
 */
sealed interface SessionState {
    data object Resolving : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val user: User, val allProfiles: List<User>) : SessionState

    val userOrNull: User? get() = (this as? SignedIn)?.user
}

/**
 * The single source of truth for the local session.
 *
 * Previously every screen resolved the active profile for itself by combining
 * `prefs.activeUserId` with the user list, and each did it slightly
 * differently. Three bugs came out of that:
 *
 *  1. **Dangling id.** If the stored id pointed at a deleted profile, some
 *     screens silently fell back to `users.first()` while others saw `-1` and
 *     rendered empty. The app looked half signed-in.
 *
 *  2. **Orphaned writes.** `AddMedicationViewModel` read the raw pref, so when
 *     the id dangled it saved medications against `userId = -1`. Those rows
 *     were written successfully and then never displayed by anything — the
 *     user's medication simply vanished.
 *
 *  3. **No sign-out.** Nothing cleared the session; "switch profile" navigated
 *     to onboarding while leaving the signed-in screens on the back stack.
 *
 * This class resolves the session in one place and *heals* the stored id when
 * it dangles, so the pref and the UI can never disagree again.
 */
@Singleton
class SessionManager @Inject constructor(
    private val prefs: UserPreferences,
    private val repository: HavnRepository,
    private val reminderScheduler: ReminderScheduler,
    private val appScope: CoroutineScope,
) {

    val state: StateFlow<SessionState> = combine(
        prefs.activeUserId,
        prefs.onboardingDone,
        repository.getAllUsers(),
    ) { activeId, onboardingDone, users ->
        resolve(activeId, onboardingDone, users)
    }
        .distinctUntilChanged()
        .stateIn(
            scope = appScope,
            // Eagerly, not WhileSubscribed: the session must already be
            // resolved by the time the first screen asks, otherwise the nav
            // graph sees Resolving, routes to onboarding, and only then gets
            // the real answer — which is exactly the "logged out on reopen"
            // symptom.
            started = SharingStarted.Eagerly,
            initialValue = SessionState.Resolving,
        )

    /** Convenience for ViewModels that only need the id. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val activeUserId: Flow<Long> = state.map { it.userOrNull?.id ?: NO_USER }.distinctUntilChanged()

    private suspend fun resolve(
        activeId: Long,
        onboardingDone: Boolean,
        users: List<User>,
    ): SessionState {
        if (users.isEmpty()) {
            // No profiles exist at all. Clear any stale completion flag so the
            // app doesn't try to restore a session into an empty database.
            if (onboardingDone || activeId != NO_USER) {
                prefs.clearSession()
            }
            return SessionState.SignedOut
        }

        val exact = users.firstOrNull { it.id == activeId }
        if (exact != null) {
            return SessionState.SignedIn(exact, users)
        }

        // The stored id points at nothing. This is recoverable — profiles
        // exist — but it must be *written back*, not just patched in memory,
        // or every component that reads the raw pref keeps seeing the stale id.
        if (activeId != NO_USER) {
            val fallback = users.first()
            prefs.setActiveUser(fallback.id)
            prefs.setOnboardingDone(true)
            return SessionState.SignedIn(fallback, users)
        }

        // Profiles exist but none is selected: a genuine signed-out state.
        return SessionState.SignedOut
    }

    /** Sign in to an existing local profile. */
    suspend fun signIn(userId: Long) {
        val user = repository.getUserById(userId) ?: return
        prefs.setActiveUser(user.id)
        prefs.setOnboardingDone(true)
        reminderScheduler.scheduleDailySyncWork()
    }

    /** Create a profile and sign straight into it. */
    suspend fun createAndSignIn(name: String, age: Int, avatarColor: String): Long {
        val id = repository.createUser(
            User(name = name.trim(), age = age, avatarColor = avatarColor)
        )
        prefs.setActiveUser(id)
        prefs.setOnboardingDone(true)
        reminderScheduler.scheduleDailySyncWork()
        return id
    }

    /**
     * Sign out without destroying anything.
     *
     * Reminders are cancelled too — leaving them scheduled would fire
     * notifications naming a profile nobody is signed into.
     */
    suspend fun signOut() {
        val current = state.value.userOrNull
        if (current != null) {
            repository.getMedicationsForUser(current.id).first().forEach { med ->
                reminderScheduler.cancelReminder(med)
            }
        }
        reminderScheduler.cancelAllScheduledWork()
        prefs.clearSession()
    }

    /**
     * Delete a profile and everything belonging to it.
     *
     * If it was the active profile, the session moves to another profile when
     * one exists, or to signed-out when none does. Returns the resulting state
     * so callers can react without re-reading.
     */
    suspend fun deleteProfile(user: User) {
        repository.getMedicationsForUser(user.id).first().forEach { med ->
            reminderScheduler.cancelReminder(med)
        }
        repository.deleteUser(user)

        val remaining = repository.getAllUsers().first()
        val wasActive = prefs.activeUserId.first() == user.id
        when {
            remaining.isEmpty() -> {
                reminderScheduler.cancelAllScheduledWork()
                prefs.clearSession()
            }
            wasActive -> {
                prefs.setActiveUser(remaining.first().id)
                prefs.setOnboardingDone(true)
                reminderScheduler.scheduleDailySyncWork()
            }
        }
    }

    companion object {
        const val NO_USER = -1L
    }
}
