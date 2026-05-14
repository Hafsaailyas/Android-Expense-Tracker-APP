package com.hafsaIlyas.expensetracker.data.preferences

// data/preferences/UserPreferencesRepository.kt
// No changes needed — CURRENCY_CODE key and currencyCode Flow are already present
// in the version you uploaded.  This file is included here for completeness so
// all currency feature files are in one place.

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
    private companion object {
        val APP_THEME            = stringPreferencesKey("app_theme")
        val DYNAMIC_COLOR        = booleanPreferencesKey("dynamic_color")
        val MONTHLY_BUDGET       = doublePreferencesKey("monthly_budget")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val CURRENCY_CODE        = stringPreferencesKey("currency_code")
    }

    val appTheme: Flow<AppTheme>
        get() = dataStore.data.map { prefs ->
            when (prefs[APP_THEME]) {
                "LIGHT" -> AppTheme.LIGHT
                "DARK"  -> AppTheme.DARK
                else    -> AppTheme.SYSTEM
            }
        }

    val dynamicColor: Flow<Boolean>
        get() = dataStore.data.map { prefs -> prefs[DYNAMIC_COLOR] ?: true }

    val monthlyBudget: Flow<Double>
        get() = dataStore.data.map { prefs -> prefs[MONTHLY_BUDGET] ?: 0.0 }

    val onboardingCompleted: Flow<Boolean>
        get() = dataStore.data.map { prefs -> prefs[ONBOARDING_COMPLETED] ?: false }

    val currencyCode: Flow<String>
        get() = dataStore.data.map { prefs -> prefs[CURRENCY_CODE] ?: "PKR" }

    suspend fun setAppTheme(theme: AppTheme) {
        dataStore.edit { prefs -> prefs[APP_THEME] = theme.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[DYNAMIC_COLOR] = enabled }
    }

    suspend fun setMonthlyBudget(value: Double) {
        dataStore.edit { prefs -> prefs[MONTHLY_BUDGET] = value }
    }

    suspend fun setOnboardingCompleted(value: Boolean) {
        dataStore.edit { prefs -> prefs[ONBOARDING_COMPLETED] = value }
    }

    suspend fun setCurrencyCode(code: String) {
        dataStore.edit { prefs -> prefs[CURRENCY_CODE] = code }
    }
}