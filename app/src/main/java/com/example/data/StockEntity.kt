package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stocks")
data class StockEntity(
    @PrimaryKey val symbol: String, // e.g., "AAPL", "RELIANCE.NS", "TSLA"
    val name: String,
    val sector: String,
    val region: String,
    val isFavorite: Boolean = false,
    val isSeed: Boolean = false, // True if pre-populated as a seed stock
    val cachedAnalysisJson: String? = null, // Store previous analysis results
    val lastUpdated: Long = 0
)
