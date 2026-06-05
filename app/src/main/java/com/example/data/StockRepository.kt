package com.example.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class StockRepository(
    private val stockDao: StockDao,
    private val alertDao: StockAlertDao
) {
    private val TAG = "StockRepository"

    val allStocks: Flow<List<StockEntity>> = stockDao.getAllStocks()
    val favoriteStocks: Flow<List<StockEntity>> = stockDao.getFavoriteStocks()

    fun getAllAlerts(): Flow<List<StockAlertEntity>> = alertDao.getAllAlerts()
    fun getAlertsForStock(symbol: String): Flow<List<StockAlertEntity>> = alertDao.getAlertsForStock(symbol)

    suspend fun insertAlert(alert: StockAlertEntity) {
        alertDao.insertAlert(alert)
    }

    suspend fun deleteAlertById(id: Int) {
        alertDao.deleteAlertById(id)
    }

    suspend fun updateAlertActiveStatus(id: Int, isActive: Boolean) {
        alertDao.updateAlertActiveStatus(id, isActive)
    }

    fun searchStocks(query: String): Flow<List<StockEntity>> {
        val trimmed = query.trim()
        return if (trimmed.isEmpty()) {
            allStocks
        } else {
            stockDao.searchStocks("%$trimmed%")
        }
    }

    suspend fun getStockBySymbol(symbol: String): StockEntity? {
        return stockDao.getStockBySymbol(symbol.uppercase().trim())
    }

    suspend fun addStockToWatchlist(symbol: String) {
        val upperSymbol = symbol.uppercase().trim()
        val existing = stockDao.getStockBySymbol(upperSymbol)
        if (existing != null) {
            stockDao.updateFavoriteStatus(upperSymbol, true)
        } else {
            val newStock = StockEntity(
                symbol = upperSymbol,
                name = "$upperSymbol Corporation",
                sector = "Financial / Tech",
                region = "Global",
                isFavorite = true,
                isSeed = false
            )
            stockDao.insertOrUpdateStock(newStock)
        }
    }

    suspend fun setFavorite(symbol: String, isFavorite: Boolean) {
        val upperSymbol = symbol.uppercase().trim()
        val existing = stockDao.getStockBySymbol(upperSymbol)
        if (existing == null && isFavorite) {
            addStockToWatchlist(upperSymbol)
        } else {
            stockDao.updateFavoriteStatus(upperSymbol, isFavorite)
        }
    }

    suspend fun fetchProAnalysis(symbol: String): StockAnalysisResult? = withContext(Dispatchers.IO) {
        val upperSymbol = symbol.uppercase().trim()
        
        // 1. Check if we already have it cached locally
        val cachedLocal = stockDao.getStockBySymbol(upperSymbol)
        
        // 2. Fetch fresh analysis from Gemini REST Client
        val result = GeminiNetworkClient.getProStockAnalysis(upperSymbol, cachedLocal?.name)
        
        if (result != null) {
            // Check if the stock is flagged as invalid or unknown by the Gemini analyzer
            if (result.name.contains("Unknown stock ticker", ignoreCase = true) || 
                result.summary.startsWith("Invalid Stock:", ignoreCase = true)) {
                Log.w(TAG, "Gemini detected invalid stock ticker: $upperSymbol")
                return@withContext result // Return and let UI handle invalid state
            }

            // Convert result back to database record and update/insert
            val moshi = com.squareup.moshi.Moshi.Builder()
                .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(StockAnalysisResult::class.java)
            val json = adapter.toJson(result)

            val existingEntity = cachedLocal ?: StockEntity(
                symbol = upperSymbol,
                name = result.name,
                sector = result.sector,
                region = result.region,
                isFavorite = false,
                isSeed = false
            )

            val updatedEntity = existingEntity.copy(
                name = result.name,
                sector = result.sector,
                region = result.region,
                cachedAnalysisJson = json,
                lastUpdated = System.currentTimeMillis()
            )

            stockDao.insertOrUpdateStock(updatedEntity)
            return@withContext result
        }

        // 3. Fallback: Parse previous analysis JSON if Gemini fetch fails (offline flow)
        cachedLocal?.cachedAnalysisJson?.let { previousJson ->
            try {
                val moshi = com.squareup.moshi.Moshi.Builder()
                    .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()
                val adapter = moshi.adapter(StockAnalysisResult::class.java)
                return@withContext adapter.fromJson(previousJson)
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing cached stock analysis: ${e.message}")
            }
        }

        return@withContext null
    }

    suspend fun seedIfNeeded() {
        if (stockDao.getCount() == 0) {
            val seedStocks = listOf(
                StockEntity("AAPL", "Apple Inc.", "Technology", "US", isSeed = true, isFavorite = true),
                StockEntity("MSFT", "Microsoft Corporation", "Technology", "US", isSeed = true),
                StockEntity("GOOGL", "Alphabet Inc.", "Technology", "US", isSeed = true),
                StockEntity("AMZN", "Amazon.com, Inc.", "Consumer Cyclical", "US", isSeed = true),
                StockEntity("TSLA", "Tesla Inc.", "Consumer Cyclical", "US", isSeed = true, isFavorite = true),
                StockEntity("NVDA", "NVIDIA Corporation", "Technology", "US", isSeed = true, isFavorite = true),
                StockEntity("META", "Meta Platforms, Inc.", "Technology", "US", isSeed = true),
                StockEntity("BRK.A", "Berkshire Hathaway Inc.", "Financial Services", "US", isSeed = true),
                StockEntity("LLY", "Eli Lilly & Company", "Healthcare", "US", isSeed = true),
                StockEntity("JPM", "JPMorgan Chase & Co.", "Financial Services", "US", isSeed = true),
                StockEntity("TSMC", "Taiwan Semiconductor Manufacturing", "Technology", "Taiwan", isSeed = true),
                StockEntity("ASML", "ASML Holding N.V.", "Technology", "Netherlands", isSeed = true),
                StockEntity("RELIANCE", "Reliance Industries Limited", "Energy / Conglomerate", "India", isSeed = true),
                StockEntity("TCS", "Tata Consultancy Services", "Technology", "India", isSeed = true),
                StockEntity("INFY", "Infosys Limited", "Technology", "India", isSeed = true),
                StockEntity("LVMH", "LVMH Moët Hennessy", "Consumer Defensive", "France", isSeed = true),
                StockEntity("BABA", "Alibaba Group Holding", "Consumer Cyclical", "China", isSeed = true),
                StockEntity("TCEHY", "Tencent Holdings Ltd.", "Technology", "China", isSeed = true),
                StockEntity("SAP", "SAP SE", "Technology", "Germany", isSeed = true),
                StockEntity("NVR", "NVR Inc.", "Consumer Cyclical", "US", isSeed = true),
                StockEntity("SONY", "Sony Group Corporation", "Technology", "Japan", isSeed = true),
                StockEntity("TM", "Toyota Motor Corporation", "Consumer Cyclical", "Japan", isSeed = true),
                StockEntity("SSNLF", "Samsung Electronics", "Technology", "South Korea", isSeed = true),
                StockEntity("HDB", "HDFC Bank Limited", "Financial Services", "India", isSeed = true),
                StockEntity("SHEL", "Shell plc", "Energy", "UK", isSeed = true),
                StockEntity("AZN", "AstraZeneca plc", "Healthcare", "UK", isSeed = true),
                StockEntity("BP", "BP p.l.c.", "Energy", "UK", isSeed = true),
                StockEntity("NFLX", "Netflix, Inc.", "Communication Services", "US", isSeed = true),
                StockEntity("AMD", "Advanced Micro Devices, Inc.", "Technology", "US", isSeed = true),
                StockEntity("NKE", "NIKE, Inc.", "Consumer Cyclical", "US", isSeed = true)
            )
            stockDao.insertStocks(seedStocks)
        }
    }
}
