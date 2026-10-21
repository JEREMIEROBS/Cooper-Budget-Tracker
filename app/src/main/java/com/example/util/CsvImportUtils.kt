package com.example.util

import com.example.data.model.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CsvRowPreview(
    val rowNumber: Int,
    val rawLine: String,
    val dateStr: String,
    val amountMinor: Long,
    val note: String,
    val isExpense: Boolean
)

data class CsvMapping(
    val dateColIndex: Int = 0,
    val amountColIndex: Int = 1,
    val noteColIndex: Int = 2,
    val categoryColIndex: Int = -1,
    val hasHeader: Boolean = true
)

object CsvImportUtils {

    fun parsePreview(
        csvContent: String,
        mapping: CsvMapping,
        currencyCode: String = "PHP"
    ): List<CsvRowPreview> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        val contentLines = if (mapping.hasHeader && lines.isNotEmpty()) lines.drop(1) else lines

        val previews = mutableListOf<CsvRowPreview>()
        for ((idx, line) in contentLines.take(20).withIndex()) {
            val cols = parseCsvLine(line)
            val dateStr = cols.getOrNull(mapping.dateColIndex)?.trim() ?: ""
            val amountStr = cols.getOrNull(mapping.amountColIndex)?.trim() ?: "0"
            val noteStr = cols.getOrNull(mapping.noteColIndex)?.trim() ?: ""

            val minor = MoneyUtils.toMinor(amountStr, currencyCode)
            previews.add(
                CsvRowPreview(
                    rowNumber = idx + 1,
                    rawLine = line,
                    dateStr = dateStr,
                    amountMinor = kotlin.math.abs(minor),
                    note = noteStr,
                    isExpense = !amountStr.startsWith("+") && !amountStr.contains("income", ignoreCase = true)
                )
            )
        }
        return previews
    }

    fun parseAll(
        csvContent: String,
        mapping: CsvMapping,
        defaultCategoryId: String,
        defaultAccountId: String,
        currencyCode: String = "PHP"
    ): List<TransactionEntity> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        val contentLines = if (mapping.hasHeader && lines.isNotEmpty()) lines.drop(1) else lines

        val list = mutableListOf<TransactionEntity>()
        val dateFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
            SimpleDateFormat("MM/dd/yyyy", Locale.US),
            SimpleDateFormat("dd/MM/yyyy", Locale.US),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
            SimpleDateFormat("MMM d, yyyy", Locale.US)
        )

        for (line in contentLines) {
            val cols = parseCsvLine(line)
            if (cols.isEmpty()) continue

            val dateStr = cols.getOrNull(mapping.dateColIndex)?.trim() ?: ""
            val amountRaw = cols.getOrNull(mapping.amountColIndex)?.trim() ?: ""
            val note = cols.getOrNull(mapping.noteColIndex)?.trim() ?: ""

            var timestamp = System.currentTimeMillis()
            for (fmt in dateFormats) {
                try {
                    val d = fmt.parse(dateStr)
                    if (d != null) {
                        timestamp = d.time
                        break
                    }
                } catch (_: Exception) {}
            }

            val minor = MoneyUtils.toMinor(amountRaw, currencyCode)
            val absMinor = kotlin.math.abs(minor)
            if (absMinor == 0L) continue

            val isIncome = amountRaw.startsWith("+") || amountRaw.contains("cash in", ignoreCase = true) || amountRaw.contains("deposit", ignoreCase = true)

            list.add(
                TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    categoryId = defaultCategoryId,
                    accountId = defaultAccountId,
                    amountMinor = absMinor,
                    kind = if (isIncome) "income" else "expense",
                    note = note,
                    occurredAt = timestamp
                )
            )
        }
        return list
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when (ch) {
                '"' -> inQuotes = !inQuotes
                ',' -> {
                    if (inQuotes) {
                        sb.append(ch)
                    } else {
                        tokens.add(sb.toString().trim())
                        sb.clear()
                    }
                }
                else -> sb.append(ch)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }
}
