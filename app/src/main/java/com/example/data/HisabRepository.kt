package com.example.data

import com.example.domain.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.json.JSONArray
import org.json.JSONObject

class HisabRepository(
    private val dao: TransactionDao,
    private val preferences: UserPreferences
) {
    val settingsFlow: Flow<HisabSettings> = preferences.settingsFlow

    /**
     * Emits real transactions normally, or demo+real only when Demo Mode is explicitly enabled.
     * In fact, when Demo Mode is enabled, we show demo entries (isolated via isDemo = true)
     * alongside any user entries so turning Demo Mode off cleanly removes only demo data.
     */
    val activeTransactionsFlow: Flow<List<TransactionEntity>> = combine(
        dao.observeAllTransactions(),
        preferences.settingsFlow
    ) { all, settings ->
        if (settings.isDemoMode) all else all.filter { !it.isDemo }
    }

    suspend fun findPotentialDuplicates(
        type: TransactionType,
        amountPaise: Long,
        dateKey: String,
        category: String,
        isDemo: Boolean,
        excludeId: Long? = null
    ): List<TransactionEntity> {
        return dao.findPotentialDuplicates(
            type = type.name,
            amountPaise = amountPaise,
            dateKey = dateKey,
            category = category,
            isDemo = isDemo
        ).filter { it.id != excludeId }
    }

    suspend fun addTransaction(
        type: TransactionType,
        amountPaise: Long,
        dateKey: String,
        timeText: String,
        category: String,
        partyOrMethod: String,
        description: String,
        isDemo: Boolean = false
    ): Long {
        val entity = TransactionEntity(
            type = type.name,
            amountPaise = amountPaise,
            dateKey = dateKey,
            timeText = timeText,
            timestampMillis = DateUtils.toEpochMillis(dateKey, timeText),
            category = category.trim(),
            partyOrMethod = partyOrMethod.trim(),
            description = description.trim(),
            isDemo = isDemo,
            createdAtMillis = System.currentTimeMillis()
        )
        return dao.insert(entity)
    }

    suspend fun updateTransaction(entity: TransactionEntity) {
        val updated = entity.copy(
            timestampMillis = DateUtils.toEpochMillis(entity.dateKey, entity.timeText),
            category = entity.category.trim(),
            partyOrMethod = entity.partyOrMethod.trim(),
            description = entity.description.trim()
        )
        dao.update(updated)
    }

    suspend fun deleteTransaction(id: Long) {
        dao.deleteById(id)
    }

    suspend fun setOpeningBalancePaise(paise: Long) {
        preferences.setOpeningBalancePaise(paise)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        preferences.setThemeMode(mode)
    }

    suspend fun enablePin(pin: String) {
        preferences.enablePin(pin)
    }

    suspend fun disablePin() {
        preferences.disablePin()
    }

    fun verifyPin(pin: String, settings: HisabSettings): Boolean =
        preferences.verifyPin(pin, settings)

    suspend fun setDemoMode(enabled: Boolean) {
        if (enabled) {
            dao.deleteAllDemo()
            dao.insertAll(buildSampleDemoTransactions())
            preferences.setDemoMode(true)
        } else {
            preferences.setDemoMode(false)
            dao.deleteAllDemo()
        }
    }

    suspend fun clearAllRealData() {
        dao.deleteAllReal()
        preferences.setOpeningBalancePaise(0L)
    }

    suspend fun exportBackupJson(currentOpeningBalancePaise: Long): String {
        val realTransactions = dao.getAllRealTransactions()
        val root = JSONObject().apply {
            put("app", "MY_DAILY_HISAB")
            put("version", 1)
            put("exportedAtMillis", System.currentTimeMillis())
            put("openingBalancePaise", currentOpeningBalancePaise)
            val txArray = JSONArray()
            for (tx in realTransactions) {
                txArray.put(
                    JSONObject().apply {
                        put("type", tx.type)
                        put("amountPaise", tx.amountPaise)
                        put("dateKey", tx.dateKey)
                        put("timeText", tx.timeText)
                        put("timestampMillis", tx.timestampMillis)
                        put("category", tx.category)
                        put("partyOrMethod", tx.partyOrMethod)
                        put("description", tx.description)
                        put("createdAtMillis", tx.createdAtMillis)
                    }
                )
            }
            put("transactions", txArray)
        }
        return root.toString(2)
    }

    suspend fun restoreBackupJson(jsonString: String, replaceExisting: Boolean): Int {
        val root = JSONObject(jsonString)
        require(root.optString("app") == "MY_DAILY_HISAB" && root.has("transactions")) {
            "Invalid backup file format."
        }
        val openingBalance = root.optLong("openingBalancePaise", 0L)
        val array = root.getJSONArray("transactions")

        val parsed = mutableListOf<TransactionEntity>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val type = TransactionType.fromString(obj.optString("type", "DEBIT"))
            val amountPaise = obj.optLong("amountPaise", 0L)
            val dateKey = obj.optString("dateKey", "")
            val timeText = obj.optString("timeText", "09:00")
            val category = obj.optString("category", "Other")
            if (amountPaise <= 0L || DateUtils.parseDateKey(dateKey) == null) continue

            parsed.add(
                TransactionEntity(
                    type = type.name,
                    amountPaise = amountPaise,
                    dateKey = dateKey,
                    timeText = timeText,
                    timestampMillis = obj.optLong("timestampMillis", DateUtils.toEpochMillis(dateKey, timeText)),
                    category = category,
                    partyOrMethod = obj.optString("partyOrMethod", ""),
                    description = obj.optString("description", ""),
                    isDemo = false,
                    createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                )
            )
        }

        if (replaceExisting) {
            dao.deleteAllReal()
            preferences.setOpeningBalancePaise(openingBalance)
            dao.insertAll(parsed)
            return parsed.size
        } else {
            val existing = dao.getAllRealTransactions()
            val existingFingerprints = existing.map {
                "${it.type}|${it.amountPaise}|${it.dateKey}|${it.timeText}|${it.category}|${it.description}"
            }.toHashSet()
            val newOnes = parsed.filter {
                val fp = "${it.type}|${it.amountPaise}|${it.dateKey}|${it.timeText}|${it.category}|${it.description}"
                fp !in existingFingerprints
            }
            if (newOnes.isNotEmpty()) dao.insertAll(newOnes)
            return newOnes.size
        }
    }

    private fun buildSampleDemoTransactions(): List<TransactionEntity> {
        val today = DateUtils.todayDateKey()
        val ym = DateUtils.currentYearMonth()
        val prevYm = ym.previousMonth()
        val prev2Ym = prevYm.previousMonth()

        fun tx(
            type: TransactionType,
            rupees: Long,
            dateKey: String,
            time: String,
            category: String,
            partyOrMethod: String,
            desc: String
        ) = TransactionEntity(
            type = type.name,
            amountPaise = rupees * 100L,
            dateKey = dateKey,
            timeText = time,
            timestampMillis = DateUtils.toEpochMillis(dateKey, time),
            category = category,
            partyOrMethod = partyOrMethod,
            description = desc,
            isDemo = true
        )

        return listOf(
            // Today
            tx(TransactionType.CREDIT, 4850, today, "10:15", "Sales", "Sharma Traders", "Morning counter retail sales"),
            tx(TransactionType.CREDIT, 3200, today, "13:40", "Online Payment", "Rohan Verma", "UPI wholesale invoice #104"),
            tx(TransactionType.DEBIT, 1150, today, "11:20", "Transport", "UPI", "Local delivery van fuel"),
            tx(TransactionType.DEBIT, 420, today, "14:05", "Food", "Cash", "Staff tea & lunch"),

            // Recent days this month
            tx(TransactionType.CREDIT, 12500, DateUtils.addDays(today, -1), "17:30", "Business Income", "Patel Electronics", "Bulk order settlement"),
            tx(TransactionType.DEBIT, 3400, DateUtils.addDays(today, -1), "15:10", "Business Expenses", "Bank Transfer", "Packaging & boxes"),
            tx(TransactionType.CREDIT, 6400, DateUtils.addDays(today, -2), "12:00", "Cash Payment", "Walk-in Customers", "Daily shop cash collection"),
            tx(TransactionType.DEBIT, 2850, DateUtils.addDays(today, -2), "18:45", "Electricity Bill", "UPI", "Monthly shop power bill"),
            tx(TransactionType.CREDIT, 9800, DateUtils.addDays(today, -4), "16:20", "Sales", "Gupta Hardware", "Hardware stock dispatch"),
            tx(TransactionType.DEBIT, 8500, DateUtils.addDays(today, -5), "10:00", "Rent", "Bank Transfer", "Shop monthly rent"),
            tx(TransactionType.CREDIT, 7100, DateUtils.addDays(today, -7), "11:45", "Online Payment", "Mehta Mart", "UPI QR counter collection"),
            tx(TransactionType.DEBIT, 1950, DateUtils.addDays(today, -7), "19:15", "Shopping", "Card", "Office stationery & printer ink"),

            // Previous month
            tx(TransactionType.CREDIT, 28000, DateUtils.formatDateKey(prevYm.year, prevYm.month, 8), "14:30", "Sales", "Kiran Stores", "Monthly supply batch"),
            tx(TransactionType.CREDIT, 19500, DateUtils.formatDateKey(prevYm.year, prevYm.month, 19), "16:00", "Business Income", "Anand Agencies", "Distributor commission"),
            tx(TransactionType.DEBIT, 8500, DateUtils.formatDateKey(prevYm.year, prevYm.month, 5), "10:30", "Rent", "Bank Transfer", "Shop rent"),
            tx(TransactionType.DEBIT, 6200, DateUtils.formatDateKey(prevYm.year, prevYm.month, 14), "13:15", "Business Expenses", "UPI", "Inventory restock"),
            tx(TransactionType.DEBIT, 2400, DateUtils.formatDateKey(prevYm.year, prevYm.month, 22), "17:00", "Transport", "Cash", "Freight charges"),

            // Two months ago
            tx(TransactionType.CREDIT, 34000, DateUtils.formatDateKey(prev2Ym.year, prev2Ym.month, 11), "15:20", "Sales", "City Mart", "Festive wholesale supply"),
            tx(TransactionType.DEBIT, 8500, DateUtils.formatDateKey(prev2Ym.year, prev2Ym.month, 5), "10:00", "Rent", "Bank Transfer", "Shop rent"),
            tx(TransactionType.DEBIT, 9100, DateUtils.formatDateKey(prev2Ym.year, prev2Ym.month, 18), "12:30", "Business Expenses", "Card", "Equipment maintenance")
        )
    }
}
