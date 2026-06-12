package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year ORDER BY category ASC")
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget): Long

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("DELETE FROM budgets")
    suspend fun clearAll()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE isIncome = :isIncome ORDER BY timestamp DESC")
    fun getTransactionsByType(isIncome: Boolean): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY timestamp DESC")
    fun getAllDocuments(): Flow<List<Document>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long

    @Delete
    suspend fun deleteDocument(document: Document)

    @Query("DELETE FROM documents")
    suspend fun clearAll()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface TaxProjectionDao {
    @Query("SELECT * FROM tax_returns ORDER BY year DESC LIMIT 1")
    fun getLatestProjection(): Flow<TaxProjection?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjection(projection: TaxProjection)

    @Query("DELETE FROM tax_returns")
    suspend fun clearAll()
}

@Dao
interface PremiumTenantDao {
    @Query("SELECT * FROM premium_tenants")
    fun getAllTenants(): Flow<List<PremiumTenant>>

    @Query("SELECT * FROM premium_tenants WHERE tenant_id = :tenantId LIMIT 1")
    suspend fun getTenantById(tenantId: String): PremiumTenant?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenant(tenant: PremiumTenant)

    @Delete
    suspend fun deleteTenant(tenant: PremiumTenant)

    @Query("DELETE FROM premium_tenants")
    suspend fun clearAll()
}
