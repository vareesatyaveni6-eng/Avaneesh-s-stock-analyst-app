package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AnalysisUiState {
    object Idle : AnalysisUiState
    object Loading : AnalysisUiState
    data class Success(val analysis: StockAnalysisResult) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
}

sealed interface NewsUiState {
    object Idle : NewsUiState
    object Loading : NewsUiState
    data class Success(val news: NewsFeedResponse) : NewsUiState
    data class Error(val message: String) : NewsUiState
}

class StockViewModel(application: Application) : AndroidViewModel(application) {

    private val db = StockDatabase.getDatabase(application, viewModelScope)
    private val repository = StockRepository(db.stockDao(), db.stockAlertDao())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _analysisState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val analysisState: StateFlow<AnalysisUiState> = _analysisState.asStateFlow()

    private val _newsState = MutableStateFlow<NewsUiState>(NewsUiState.Idle)
    val newsState: StateFlow<NewsUiState> = _newsState.asStateFlow()

    private val _activeTab = MutableStateFlow(0) // 0: Explore / Global, 1: Watchlist
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    fun getAlertsForStock(symbol: String): Flow<List<StockAlertEntity>> {
        return repository.getAlertsForStock(symbol)
    }

    fun addAlert(symbol: String, price: Float, isAbove: Boolean) {
        viewModelScope.launch {
            repository.insertAlert(
                StockAlertEntity(
                    symbol = symbol,
                    targetPrice = price,
                    isAbove = isAbove,
                    isActive = true
                )
            )
        }
    }

    fun deleteAlert(id: Int) {
        viewModelScope.launch {
            repository.deleteAlertById(id)
        }
    }

    fun toggleAlertActive(id: Int, isActive: Boolean) {
        viewModelScope.launch {
            repository.updateAlertActiveStatus(id, isActive)
        }
    }

    // Monitor stocks matching current search
    val stocksList: StateFlow<List<StockEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            repository.searchStocks(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteStocks: StateFlow<List<StockEntity>> = repository.favoriteStocks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.seedIfNeeded()
        }
    }

    fun setActiveTab(tab: Int) {
        _activeTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(symbol: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.setFavorite(symbol, !currentStatus)
        }
    }

    fun addStockToWatchlist(symbol: String) {
        val upperSymbol = symbol.uppercase().trim()
        if (upperSymbol.isBlank()) return
        viewModelScope.launch {
            repository.addStockToWatchlist(upperSymbol)
        }
    }

    fun selectStock(symbol: String) {
        if (symbol.isBlank()) return
        
        viewModelScope.launch {
            _analysisState.value = AnalysisUiState.Loading
            fetchNews(symbol)
            try {
                val result = repository.fetchProAnalysis(symbol)
                if (result != null) {
                    if (result.name.contains("Unknown stock ticker", ignoreCase = true) || 
                        result.summary.startsWith("Invalid Stock:", ignoreCase = true)) {
                        _analysisState.value = AnalysisUiState.Error("Unknown stock ticker symbol. Please select or search for a valid global stock ticker (e.g., TSLA, NFLX, RELIANCE).")
                    } else {
                        _analysisState.value = AnalysisUiState.Success(result)
                    }
                } else {
                    _analysisState.value = AnalysisUiState.Error("Failed to fetch analysis. Check your network or verify your Gemini API key in the AI Studio Secrets panel.")
                }
            } catch (e: Exception) {
                _analysisState.value = AnalysisUiState.Error("Error: ${e.localizedMessage ?: "Unknown error occurred"}")
            }
        }
    }

    fun fetchNews(symbol: String) {
        if (symbol.isBlank()) return
        viewModelScope.launch {
            _newsState.value = NewsUiState.Loading
            try {
                val cachedLocal = repository.getStockBySymbol(symbol)
                val result = GeminiNetworkClient.getRecentNewsForTicker(symbol, cachedLocal?.name)
                if (result != null) {
                    _newsState.value = NewsUiState.Success(result)
                } else {
                    _newsState.value = NewsUiState.Error("No recent search-grounded headlines found for $symbol.")
                }
            } catch (e: Exception) {
                _newsState.value = NewsUiState.Error("Error: ${e.localizedMessage ?: "Unknown news loading error"}")
            }
        }
    }

    fun clearSelectedStock() {
        _analysisState.value = AnalysisUiState.Idle
        _newsState.value = NewsUiState.Idle
    }
}
