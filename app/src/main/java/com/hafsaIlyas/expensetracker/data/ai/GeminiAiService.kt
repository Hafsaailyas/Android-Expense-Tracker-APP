package com.hafsaIlyas.expensetracker.data.ai

// data/ai/GeminiAiService.kt

import com.hafsaIlyas.expensetracker.BuildConfig
import com.hafsaIlyas.expensetracker.data.currency.Currency
import com.hafsaIlyas.expensetracker.data.currency.CurrencyFormatter
import com.hafsaIlyas.expensetracker.data.local.entity.Expense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class GeminiAiService @Inject constructor() : AiInsightService {

    private val apiKey: String
        get() = BuildConfig.GEMINI_API_KEY

    private val endpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    override suspend fun generateInsights(
        expenses      : List<Expense>,
        currency      : Currency,        // ✅ added — drives all formatting in the prompt
        monthlyBudget : Double
    ): InsightResult = withContext(Dispatchers.IO) {

        // ✅ Use CurrencyFormatter so amounts in the prompt match the user's currency
        val fmt   = CurrencyFormatter(currency)
        val total = expenses.sumOf { it.amount }

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
            All monetary values are in ${currency.name} (${currency.code}, symbol: ${currency.symbol}).
            
            Spending summary:
            $expenseSummary
            Total: ${fmt.format(total)}
            $budgetLine
            Transaction count: ${expenses.size}
            
            Return ONLY a JSON array with 3-5 objects, each having:
            - "type": one of [OVERSPENDING_ALERT, SAVING_SUGGESTION, BUDGET_FORECAST, POSITIVE_REINFORCEMENT, CATEGORY_TIP]
            - "title": short title (max 6 words)
            - "body": actionable insight (2-3 sentences). Use ${currency.symbol} for all amounts.
            - "severity": one of [INFO, WARNING, CRITICAL, POSITIVE]
            - "actionLabel": optional short CTA string or null
            
            Do not include markdown fences or any text outside the JSON array.
        """.trimIndent()

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
            doOutput        = true
            connectTimeout  = 15_000
            readTimeout     = 30_000
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
                summary  = "AI analysed ${expenses.size} transactions · ${fmt.format(total)}",
                insights = insights
            )

        } catch (e: Exception) {
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