package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_alerts")
data class StockAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val targetPrice: Float,
    val isAbove: Boolean, // True if triggering when price goes above target, False if below.
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
