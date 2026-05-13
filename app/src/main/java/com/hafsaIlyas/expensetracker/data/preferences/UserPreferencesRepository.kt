package com.hafsaIlyas.expensetracker.data.preferences

// data/preferences/UserPreferencesRepository.kt
//
// ADD the two members below to your existing UserPreferencesRepository.
// Everything else (appTheme, dynamicColor, monthlyBudget, etc.) stays unchanged.
//
// ── What to add ───────────────────────────────────────────────────────────────
//
//   private companion object {
//       // existing keys …
//       val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
//   }
//
//   /** Emits false on first install, true once markCompleted() has been called. */
//   val onboardingCompleted: Flow<Boolean>
//       get() = dataStore.data.map { prefs ->
//           prefs[ONBOARDING_COMPLETED] ?: false
//       }
//
//   suspend fun setOnboardingCompleted(value: Boolean) {
//       dataStore.edit { prefs ->
//           prefs[ONBOARDING_COMPLETED] = value
//       }
//   }
//
// ── Why DataStore and not SharedPreferences ───────────────────────────────────
//
// The rest of the app already uses DataStore (appTheme, dynamicColor, etc.),
// so adding one more key here keeps everything in a single file and avoids
// a second SharedPreferences call on the main thread.
//
// ── Full file (copy-paste ready, replace your existing file) ─────────────────

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hafsaIlyas.expensetracker.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    // ── Keys ──────────────────────────────────────────────────────────────────
    private companion object {
        val APP_THEME             = stringPreferencesKey("app_theme")
        val DYNAMIC_COLOR         = booleanPreferencesKey("dynamic_color")
        val MONTHLY_BUDGET        = doublePreferencesKey("monthly_budget")
        val ONBOARDING_COMPLETED  = booleanPreferencesKey("onboarding_completed")  // ← NEW
    }

    // ── Reads ─────────────────────────────────────────────────────────────────

    val appTheme: Flow<AppTheme>
        get() = dataStore.data.map { prefs ->
            when (prefs[APP_THEME]) {
                "LIGHT"  -> AppTheme.LIGHT
                "DARK"   -> AppTheme.DARK
                else     -> AppTheme.SYSTEM
            }
        }

    val dynamicColor: Flow<Boolean>
        get() = dataStore.data.map { prefs ->
            prefs[DYNAMIC_COLOR] ?: true
        }

    val monthlyBudget: Flow<Double>
        get() = dataStore.data.map { prefs ->
            prefs[MONTHLY_BUDGET] ?: 0.0
        }

    /** false on first install; true after onboarding is completed or skipped. */
    val onboardingCompleted: Flow<Boolean>                                         // ← NEW
        get() = dataStore.data.map { prefs ->
            prefs[ONBOARDING_COMPLETED] ?: false
        }

    // ── Writes ────────────────────────────────────────────────────────────────

    suspend fun setAppTheme(theme: AppTheme) {
        dataStore.edit { prefs -> prefs[APP_THEME] = theme.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[DYNAMIC_COLOR] = enabled }
    }

    suspend fun setMonthlyBudget(value: Double) {
        dataStore.edit { prefs -> prefs[MONTHLY_BUDGET] = value }
    }

    suspend fun setOnboardingCompleted(value: Boolean) {                           // ← NEW
        dataStore.edit { prefs -> prefs[ONBOARDING_COMPLETED] = value }
    }
}