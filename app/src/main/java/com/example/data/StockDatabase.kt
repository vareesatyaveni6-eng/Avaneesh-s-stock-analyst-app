package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [StockEntity::class, StockAlertEntity::class], version = 2, exportSchema = false)
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    abstract fun stockAlertDao(): StockAlertDao

    companion object {
        @Volatile
        private var INSTANCE: StockDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): StockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockDatabase::class.java,
                    "stock_pro_analysis_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(StockDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class StockDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateSeedData(database.stockDao())
                }
            }
        }

        private suspend fun populateSeedData(dao: StockDao) {
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
            dao.insertStocks(seedStocks)
        }
    }
}
