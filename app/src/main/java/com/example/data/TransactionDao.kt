package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateKey DESC, timeText DESC, id DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isDemo = 0 ORDER BY dateKey DESC, timeText DESC, id DESC")
    suspend fun getAllRealTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query(
        "SELECT * FROM transactions WHERE type = :type AND amountPaise = :amountPaise " +
            "AND dateKey = :dateKey AND category = :category AND isDemo = :isDemo"
    )
    suspend fun findPotentialDuplicates(
        type: String,
        amountPaise: Long,
        dateKey: String,
        category: String,
        isDemo: Boolean
    ): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions WHERE isDemo = 1")
    suspend fun deleteAllDemo()

    @Query("DELETE FROM transactions WHERE isDemo = 0")
    suspend fun deleteAllReal()
}
