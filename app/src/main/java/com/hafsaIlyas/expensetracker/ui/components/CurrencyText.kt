package com.hafsaIlyas.expensetracker.ui.components

// ui/components/CurrencyText.kt
// Drop-in Text composable for currency-formatted amounts.
// Reacts automatically when the user changes their currency in Settings.

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.hafsaIlyas.expensetracker.data.currency.Currency
import com.hafsaIlyas.expensetracker.data.currency.CurrencyFormatter
import com.hafsaIlyas.expensetracker.data.currency.CurrencyService
import com.hafsaIlyas.expensetracker.data.currency.getByCode

// ── Helper — remember a CurrencyFormatter that updates when currency changes ──

/**
 * Returns a [CurrencyFormatter] that is automatically recreated whenever the
 * active currency changes.  Collect the flow once and cache the result.
 *
 * Usage:
 * ```kotlin
 * val formatter = rememberCurrencyFormatter(currencyService)
 * Text(text = formatter.format(amount))
 * ```
 */
@Composable
fun rememberCurrencyFormatter(currencyService: CurrencyService): CurrencyFormatter {
    val currency by currencyService.currentCurrency.collectAsState(
        initial = getByCode("PKR")
    )
    return remember(currency) { CurrencyFormatter(currency) }
}

// ── CurrencyText composable ───────────────────────────────────────────────────

/**
 * Displays [amount] formatted with the active currency.
 * Automatically updates when the user switches currency in Settings.
 *
 * @param amount         Raw numeric amount (e.g. 4000.0)
 * @param currencyService Injected service — hiltViewModel() works, but prefer
 *                        passing the instance already collected in the parent screen.
 * @param compact         If true, uses the compact formatter (e.g. "Rs. 4K").
 * @param prefix          Optional prefix inserted before the formatted string (e.g. "-").
 */
@Composable
fun CurrencyText(
    amount          : Double,
    currencyService : CurrencyService,
    modifier        : Modifier  = Modifier,
    style           : TextStyle = LocalTextStyle.current,
    color           : Color     = Color.Unspecified,
    compact         : Boolean   = false,
    prefix          : String    = ""
) {
    val formatter = rememberCurrencyFormatter(currencyService)
    val text = prefix + if (compact) formatter.formatCompact(amount) else formatter.format(amount)

    Text(
        text     = text,
        modifier = modifier,
        style    = style,
        color    = color
    )
}