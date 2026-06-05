package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StockAlertDao {
    @Query("SELECT * FROM stock_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<StockAlertEntity>>

    @Query("SELECT * FROM stock_alerts WHERE symbol = :symbol ORDER BY createdAt DESC")
    fun getAlertsForStock(symbol: String): Flow<List<StockAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: StockAlertEntity)

    @Query("DELETE FROM stock_alerts WHERE id = :id")
    suspend fun deleteAlertById(id: Int)

    @Query("UPDATE stock_alerts SET isActive = :isActive WHERE id = :id")
    suspend fun updateAlertActiveStatus(id: Int, isActive: Boolean)
}
