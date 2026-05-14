package com.hafsaIlyas.expensetracker.data.currency

// data/currency/Currency.kt
// Defines all supported currencies and the CurrencyFormatter used throughout the app.

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// ── Data model ────────────────────────────────────────────────────────────────

data class Currency(
    val code          : String,
    val symbol        : String,
    val name          : String,
    val decimalDigits : Int       // 0 = whole numbers only, 2 = two decimal places
)

// ── Supported currency list ───────────────────────────────────────────────────

val SUPPORTED_CURRENCIES: List<Currency> = listOf(
    Currency("PKR", "Rs.",  "Pakistani Rupee",     0),
    Currency("USD", "$",    "US Dollar",            2),
    Currency("EUR", "€",    "Euro",                 2),
    Currency("GBP", "£",    "British Pound",        2),
    Currency("INR", "₹",    "Indian Rupee",         0),
    Currency("AED", "د.إ",  "UAE Dirham",           2),
    Currency("SAR", "﷼",    "Saudi Riyal",          2),
    Currency("CAD", "C$",   "Canadian Dollar",      2),
    Currency("AUD", "A$",   "Australian Dollar",    2),
    Currency("JPY", "¥",    "Japanese Yen",         0),
    Currency("CNY", "¥",    "Chinese Yuan",         2),
    Currency("TRY", "₺",    "Turkish Lira",         2),
    Currency("MYR", "RM",   "Malaysian Ringgit",    2),
    Currency("SGD", "S$",   "Singapore Dollar",     2)
)

/** Returns the [Currency] matching [code], falling back to PKR if not found. */
fun getByCode(code: String): Currency =
    SUPPORTED_CURRENCIES.firstOrNull { it.code == code }
        ?: SUPPORTED_CURRENCIES.first()   // PKR default

// ── Formatter ─────────────────────────────────────────────────────────────────

/**
 * Formats monetary amounts according to the active [Currency].
 *
 * Rules:
 *  - decimalDigits == 0 → whole numbers, thousands-separated  (e.g. "Rs. 4,000")
 *  - decimalDigits == 2 → two decimal places (e.g. "$4,000.00")
 *  - PKR uses a space after the symbol; all others do not.
 *
 * [formatCompact] abbreviates large numbers:
 *   ≥ 1 000 000 → "1.2M", ≥ 1 000 → "1.2K"  (no decimals for 0-decimal currencies)
 */
class CurrencyFormatter(val currency: Currency) {

    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        // Force US grouping/decimal separators for consistency
        groupingSeparator = ','
        decimalSeparator  = '.'
    }

    private val fullPattern: DecimalFormat
        get() = if (currency.decimalDigits == 0)
            DecimalFormat("#,##0", symbols)
        else
            DecimalFormat("#,##0.00", symbols)

    /** Formats [amount] with the currency symbol, e.g. "Rs. 4,000" or "$4,000.00". */
    fun format(amount: Double): String {
        val formatted = fullPattern.format(amount)
        return "${currency.symbol}${symbolSeparator()}$formatted"
    }

    /**
     * Compact format for tight spaces.
     * e.g. "Rs. 4K", "$1.2M"
     */
    fun formatCompact(amount: Double): String {
        val (divisor, suffix) = when {
            amount >= 1_000_000.0 -> 1_000_000.0 to "M"
            amount >= 1_000.0     -> 1_000.0      to "K"
            else                  -> return format(amount)
        }
        val compact = amount / divisor
        val compactFormatted = if (currency.decimalDigits == 0)
            DecimalFormat("#,##0", symbols).format(compact)
        else
            DecimalFormat("#,##0.#", symbols).format(compact)
        return "${currency.symbol}${symbolSeparator()}$compactFormatted$suffix"
    }

    // PKR gets a space between symbol and number ("Rs. 4,000"); others don't ("$4,000.00")
    private fun symbolSeparator(): String = if (currency.code == "PKR") " " else ""
}