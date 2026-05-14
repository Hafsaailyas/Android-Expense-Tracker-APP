package com.hafsaIlyas.expensetracker.data.currency

// data/currency/CurrencyService.kt
// Singleton that bridges DataStore preferences with the rest of the UI.
// Inject this wherever you need the active currency or formatter.

import com.hafsaIlyas.expensetracker.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyService @Inject constructor(
    private val prefsRepo: UserPreferencesRepository
) {
    /**
     * Reactive stream of the currently selected [Currency].
     * Emits a new value whenever the user changes their currency in Settings.
     * Defaults to PKR.
     */
    val currentCurrency: Flow<Currency> = prefsRepo.currencyCode.map { code ->
        getByCode(code)
    }

    /**
     * Persist the chosen currency code to DataStore.
     * This is a suspend function — call from a coroutine / ViewModel scope.
     */
    suspend fun setCurrency(currency: Currency) {
        prefsRepo.setCurrencyCode(currency.code)
    }
}