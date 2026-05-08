package com.hafsaIlyas.expensetracker.data.ai

// data/ai/GeminiAiService.kt
import com.hafsaIlyas.expensetracker.BuildConfig   // ← add this import

import com.hafsaIlyas.expensetracker.data.local.entity.Expense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

/**
 * Production implementation — swap the Hilt binding in [AiModule] to activate.
 *
 * 1. Add your Gemini API key to local.properties:
 *       GEMINI_API_KEY=your_key_here
 * 2. Expose it in build.gradle.kts:
 *       buildConfigField("String", "GEMINI_API_KEY",
 *           "\"${properties["GEMINI_API_KEY"]}\"")
 * 3. Flip the binding in AiModule from MockAiInsightService → GeminiAiService.
 */
class GeminiAiService @Inject constructor() : AiInsightService {

    // Read from BuildConfig — never hardcode API keys in source
    private val apiKey: String
        get() = com.hafsaIlyas.expensetracker.BuildConfig.GEMINI_API_KEY

    private val endpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    override suspend fun generateInsights(
        expenses: List<Expense>,
        monthlyBudget: Double
    ): InsightResult = withContext(Dispatchers.IO) {

        val fmt   = NumberFormat.getCurrencyInstance(Locale.US)
        val total = expenses.sumOf { it.amount }

        // ── Build prompt ──────────────────────────────────────────────────────
        val expenseSummary = expenses
            .groupBy { it.category }
            .map { (cat, list) ->
                val catTotal = list.sumOf { it.amount }
                val pct      = ((catTotal / total) * 100).toInt()
                "$cat: ${fmt.format(catTotal)} ($pct%)"
            }
            .joinToString("\n")

        val budgetLine = if (monthlyBudget > 0)
            "Monthly budget: ${fmt.format(monthlyBudget)}" else "No budget set."

        val prompt = """
            You are a concise personal finance assistant.
            Analyse this user's current month spending and return a JSON array of insights.
            
            Spending summary:
            $expenseSummary
            Total: ${fmt.format(total)}
            $budgetLine
            Transaction count: ${expenses.size}
            
            Return ONLY a JSON array with 3-5 objects, each having:
            - "type": one of [OVERSPENDING_ALERT, SAVING_SUGGESTION, BUDGET_FORECAST, POSITIVE_REINFORCEMENT, CATEGORY_TIP]
            - "title": short title (max 6 words)
            - "body": actionable insight (2-3 sentences)
            - "severity": one of [INFO, WARNING, CRITICAL, POSITIVE]
            - "actionLabel": optional short CTA string or null
            
            Do not include markdown fences or any text outside the JSON array.
        """.trimIndent()

        // ── HTTP call ─────────────────────────────────────────────────────────
        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
        }.toString()

        val url  = URL("$endpoint?key=$apiKey")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 15_000
            readTimeout    = 30_000
        }

        try {
            OutputStreamWriter(conn.outputStream).use { it.write(requestBody) }

            val responseText = conn.inputStream.bufferedReader().readText()
            val root         = JSONObject(responseText)
            val text         = root
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            // ── Parse response ────────────────────────────────────────────────
            val jsonArray = JSONArray(text)
            val insights  = (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                Insight(
                    type        = runCatching {
                        InsightType.valueOf(obj.getString("type"))
                    }.getOrDefault(InsightType.CATEGORY_TIP),
                    title       = obj.getString("title"),
                    body        = obj.getString("body"),
                    severity    = runCatching {
                        Severity.valueOf(obj.getString("severity"))
                    }.getOrDefault(Severity.INFO),
                    actionLabel = obj.optString("actionLabel").takeIf { it.isNotBlank() }
                )
            }

            InsightResult(
                summary  = "AI analysed ${expenses.size} transactions · ${fmt.format(total)} total",
                insights = insights
            )

        } catch (e: Exception) {
            // Graceful fallback — surface error as a single insight card
            InsightResult(
                summary  = "AI analysis unavailable",
                insights = listOf(
                    Insight(
                        type     = InsightType.CATEGORY_TIP,
                        title    = "Couldn't reach AI",
                        body     = "We were unable to generate insights right now. Please check your connection and try again. (${e.message})",
                        severity = Severity.WARNING
                    )
                )
            )
        } finally {
            conn.disconnect()
        }
    }
}