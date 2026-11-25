package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType {
    CREDIT,
    DEBIT;

    companion object {
        fun fromString(raw: String): TransactionType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: DEBIT
    }
}

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode");

    companion object {
        fun fromString(raw: String?): ThemeMode =
            entries.firstOrNull { it.name == raw } ?: SYSTEM
    }
}

object TransactionCategories {
    val creditCategories = listOf(
        "Sales",
        "Business Income",
        "Online Payment",
        "Cash Payment",
        "Other Income"
    )

    val debitCategories = listOf(
        "Shopping",
        "Food",
        "Transport",
        "Business Expenses",
        "Electricity Bill",
        "Rent",
        "Other Expenses"
    )

    val paymentMethods = listOf(
        "Cash",
        "UPI",
        "Bank Transfer",
        "Card",
        "Other"
    )
}

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["dateKey"]),
        Index(value = ["type"]),
        Index(value = ["isDemo"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: String, // "CREDIT" or "DEBIT"
    val amountPaise: Long, // Exact integer paise (INR * 100) to avoid floating point errors
    val dateKey: String, // "yyyy-MM-dd"
    val timeText: String, // "HH:mm" (24h sorted representation)
    val timestampMillis: Long,
    val category: String,
    val partyOrMethod: String = "", // Customer Name (Credit) or Payment Method (Debit)
    val description: String = "",
    val isDemo: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    val transactionType: TransactionType
        get() = TransactionType.fromString(type)
}
