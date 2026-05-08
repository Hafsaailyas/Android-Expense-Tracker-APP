package com.hafsaIlyas.expensetracker.data.preferences

// data/preferences/UserPreferencesRepository.kt

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.hafsaIlyas.expensetracker.ui.theme.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME          = stringPreferencesKey("app_theme")
        val DYNAMIC_COLOR  = booleanPreferencesKey("dynamic_color")
        val MONTHLY_BUDGET = doublePreferencesKey("monthly_budget")
    }

    // ── Flows ─────────────────────────────────────────────────────────────────

    val appTheme: Flow<AppTheme> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            AppTheme.valueOf(prefs[Keys.THEME] ?: AppTheme.SYSTEM.name)
        }

    val dynamicColor: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[Keys.DYNAMIC_COLOR] ?: true }

    val monthlyBudget: Flow<Double> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[Keys.MONTHLY_BUDGET] ?: 0.0 }

    // ── Mutators ──────────────────────────────────────────────────────────────

    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setMonthlyBudget(budget: Double) {
        context.dataStore.edit { it[Keys.MONTHLY_BUDGET] = budget }
    }
}