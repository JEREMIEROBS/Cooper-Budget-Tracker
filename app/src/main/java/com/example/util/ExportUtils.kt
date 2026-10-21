package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    fun generateCsv(
        transactions: List<TransactionEntity>,
        categoriesMap: Map<String, CategoryEntity>,
        currencyCode: String
    ): String {
        val sb = StringBuilder()
        sb.append("ID,Date,Time,Category,Kind,Amount,Currency,Note\n")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

        for (tx in transactions) {
            val date = Date(tx.occurredAt)
            val category = categoriesMap[tx.categoryId]?.name ?: "Uncategorized"
            val formattedAmount = MoneyUtils.formatPlain(tx.amountMinor, currencyCode)
            val safeNote = tx.note.replace("\"", "\"\"")

            sb.append("\"${tx.id}\",")
            sb.append("\"${dateFormat.format(date)}\",")
            sb.append("\"${timeFormat.format(date)}\",")
            sb.append("\"$category\",")
            sb.append("\"${tx.kind}\",")
            sb.append("$formattedAmount,")
            sb.append("\"$currencyCode\",")
            sb.append("\"$safeNote\"\n")
        }

        return sb.toString()
    }

    fun generateJson(
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        currencyCode: String
    ): String {
        val root = JSONObject()
        root.put("app", "Budget Tracker")
        root.put("exportedAt", System.currentTimeMillis())
        root.put("currency", currencyCode)

        val categoriesArray = JSONArray()
        for (cat in categories) {
            val catObj = JSONObject()
            catObj.put("id", cat.id)
            catObj.put("name", cat.name)
            catObj.put("kind", cat.kind)
            catObj.put("colorHex", cat.colorHex)
            categoriesArray.put(catObj)
        }
        root.put("categories", categoriesArray)

        val txArray = JSONArray()
        for (tx in transactions) {
            val txObj = JSONObject()
            txObj.put("id", tx.id)
            txObj.put("categoryId", tx.categoryId)
            txObj.put("amountMinor", tx.amountMinor)
            txObj.put("kind", tx.kind)
            txObj.put("note", tx.note)
            txObj.put("occurredAt", tx.occurredAt)
            txArray.put(txObj)
        }
        root.put("transactions", txArray)

        return root.toString(2)
    }

    fun shareExport(context: Context, content: String, mimeType: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            type = mimeType
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
