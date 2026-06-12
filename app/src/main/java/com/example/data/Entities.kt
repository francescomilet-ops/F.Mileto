package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val limitAmount: Double,
    val month: Int,
    val year: Int
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val isIncome: Boolean,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val taxRate: Double = 0.19, // Default standard tax (e.g., German 19% MwSt or custom)
    val estimatedTaxAmount: Double = 0.0,
    val isRecurring: Boolean = false,
    val recurrenceInterval: String = "MONTHLY" // MONTHLY, WEEKLY, YEARLY
)

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "PDF", "RECHNUNG", "STEUER", "CONTRACT", "OTHER"
    val sizeBytes: Long,
    val contentUri: String = "", // Placeholders or simulated encrypted path
    val timestamp: Long = System.currentTimeMillis(),
    val summaryText: String = "",
    val encryptionKeyAlias: String = "shirin_e2e_sec_key",
    val notes: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentPath: String? = null,
    val speechUrl: String? = null, // For audio playback simulation
    val translatedText: String? = null
)

@Entity(tableName = "tax_returns")
data class TaxProjection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val year: Int = 2026,
    val totalIncome: Double,
    val totalExpenses: Double,
    val totalDeductions: Double,
    val netTaxableIncome: Double,
    val projectedTaxLiability: Double,
    val optimizedSavingsProgress: Double,
    val optimizationTipsJson: String = ""
)

@Entity(tableName = "premium_tenants")
data class PremiumTenant(
    @PrimaryKey @androidx.room.ColumnInfo(name = "tenant_id") val tenantId: String,
    @androidx.room.ColumnInfo(name = "company_name") val companyName: String,
    @androidx.room.ColumnInfo(name = "hourly_rate_target") val hourlyRateTarget: Double = 65.00,
    @androidx.room.ColumnInfo(name = "preferred_vendors") val preferredVendors: List<String> = listOf("Hornbach", "Amazon", "Rexel"),
    @androidx.room.ColumnInfo(name = "career_stage") val careerStage: String = "MEISTERBETRIEB"
)
