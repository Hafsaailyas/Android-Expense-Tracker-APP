package com.hafsaIlyas.expensetracker.data.export

// data/export/ExportService.kt

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.hafsaIlyas.expensetracker.data.local.entity.Expense
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    // ── CSV ───────────────────────────────────────────────────────────────────

    fun buildCsv(expenses: List<Expense>): String = buildString {
        // Header
        appendLine("ID,Date,Category,Amount,Note")
        // Rows
        expenses.forEach { e ->
            val date     = dateFormatter.format(Date(e.date))
            val amount   = "%.2f".format(e.amount)
            val note     = "\"${e.note.replace("\"", "\"\"")}\"" // RFC-4180 escaping
            val category = "\"${e.category.replace("\"", "\"\"")}\""
            appendLine("${e.id},$date,$category,$amount,$note")
        }
        // Summary footer
        appendLine()
        val total = expenses.sumOf { it.amount }
        appendLine(",,Total,${currencyFormatter.format(total)},")
        appendLine(",,Count,${expenses.size},")
    }

    // ── Plain text (readable) ─────────────────────────────────────────────────

    fun buildPlainText(expenses: List<Expense>): String = buildString {
        appendLine("===== Expense Report =====")
        appendLine("Generated: ${dateFormatter.format(Date())}")
        appendLine("Total Entries: ${expenses.size}")
        appendLine()

        // Group by month
        expenses
            .groupBy { monthKey(it.date) }
            .forEach { (month, monthExpenses) ->
                appendLine("── $month ──")
                monthExpenses.forEach { e ->
                    val day  = SimpleDateFormat("dd", Locale.getDefault()).format(Date(e.date))
                    appendLine("  [$day] ${e.category.padEnd(18)} ${currencyFormatter.format(e.amount)}" +
                            if (e.note.isNotBlank()) "  (${e.note})" else "")
                }
                val subtotal = monthExpenses.sumOf { it.amount }
                appendLine("  Subtotal: ${currencyFormatter.format(subtotal)}")
                appendLine()
            }

        val total = expenses.sumOf { it.amount }
        appendLine("Grand Total: ${currencyFormatter.format(total)}")
    }

    private fun monthKey(epochMillis: Long): String =
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(epochMillis))

    // ── Share intent ──────────────────────────────────────────────────────────

    /**
     * Writes [content] to a file in the app's cache and returns a chooser
     * Intent using [FileProvider] — no WRITE_EXTERNAL_STORAGE permission needed.
     */
    fun buildShareIntent(
        content: String,
        fileName: String = "expenses_${System.currentTimeMillis()}.csv",
        mimeType: String = "text/csv"
    ): Intent {
        // Write to cache dir (no special permission)
        val exportDir  = File(context.cacheDir, "exports").also { it.mkdirs() }
        val exportFile = File(exportDir, fileName)
        exportFile.writeText(content)

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            exportFile
        )

        return Intent(Intent.ACTION_SEND).apply {
            type        = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "My Expense Report")
            putExtra(Intent.EXTRA_TEXT,
                "Here is my expense report exported from Expense Tracker.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }.let { Intent.createChooser(it, "Share Expenses via…") }
    }
}