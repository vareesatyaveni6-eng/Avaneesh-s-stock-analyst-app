package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stocks ORDER BY symbol ASC")
    fun getAllStocks(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stocks WHERE isFavorite = 1 ORDER BY symbol ASC")
    fun getFavoriteStocks(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stocks WHERE symbol LIKE :query OR name LIKE :query ORDER BY symbol ASC")
    fun searchStocks(query: String): Flow<List<StockEntity>>

    @Query("SELECT * FROM stocks WHERE symbol = :symbol LIMIT 1")
    suspend fun getStockBySymbol(symbol: String): StockEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStocks(stocks: List<StockEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStock(stock: StockEntity)

    @Query("UPDATE stocks SET isFavorite = :isFavorite WHERE symbol = :symbol")
    suspend fun updateFavoriteStatus(symbol: String, isFavorite: Boolean)

    @Query("UPDATE stocks SET cachedAnalysisJson = :json, lastUpdated = :timestamp, name = :name, sector = :sector, region = :region WHERE symbol = :symbol")
    suspend fun updateCachedAnalysis(symbol: String, name: String, sector: String, region: String, json: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM stocks")
    suspend fun getCount(): Int
}
