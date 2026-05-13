package com.hafsaIlyas.expensetracker.ui.screens.onboarding

// ui/screens/onboarding/OnboardingViewModel.kt
//
// Manages the "has user seen onboarding?" flag via DataStore.
// Uses the same DataStore instance as UserPreferencesRepository so there is
// no second preferences file in the app.
//
// Key stored: "onboarding_completed" (Boolean, default false).
// Once the user finishes or skips onboarding, markCompleted() writes true
// and the flag is never reset.

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hafsaIlyas.expensetracker.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    /**
     * Tri-state:
     *   null  → still reading from disk (show splash / nothing)
     *   false → first launch → show onboarding
     *   true  → already completed → skip to main app
     */
    val onboardingCompleted: StateFlow<Boolean?> = prefsRepo
        .onboardingCompleted          // Flow<Boolean> exposed from the repo
        .map<Boolean, Boolean?> { it }
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5_000),
            initialValue  = null      // unknown until DataStore emits
        )

    fun markCompleted() {
        viewModelScope.launch {
            prefsRepo.setOnboardingCompleted(true)
        }
    }
}