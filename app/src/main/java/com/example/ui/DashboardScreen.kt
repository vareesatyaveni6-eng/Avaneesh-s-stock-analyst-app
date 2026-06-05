package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.IndicatorState
import com.example.data.StockAnalysisResult
import com.example.data.StockEntity
import com.example.data.StockAlertEntity
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: StockViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val stocksList by viewModel.stocksList.collectAsStateWithLifecycle()
    val favoriteStocks by viewModel.favoriteStocks.collectAsStateWithLifecycle()
    val analysisState by viewModel.analysisState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    // High Density Design Theme Colors
    val appBg = Color(0xFFFDF7FF)        // Light pastel background
    val cardBg = Color(0xFFFFFFFF)       // White clean card containers
    val accentGreen = Color(0xFF146C2E)  // Pro dark green for bullish signals
    val accentRed = Color(0xFFB3261E)    // Pro M3 red for bearish signals
    val borderLight = Color(0xFFEADDFF)  // Light violet border matching HTML
    val borderDark = Color(0xFFCAC4D0)   // Deeper container borders
    val textMuted = Color(0xFF49454F)    // Material 3 medium-dark grey/violet text
    val textPrimary = Color(0xFF1D1B20)  // Material 3 high contrast dark purple text
    val violetDeep = Color(0xFF21005D)   // Royal violet for contrast cards
    val lavenderPrimary = Color(0xFF6750A4) // Medium accent purple
    val lavenderSoft = Color(0xFFEADDFF) // Software selection background / badges
    val navBg = Color(0xFFF3EDF7)        // Bottom navigation / tab background

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var inlineAddText by remember { mutableStateOf("") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = appBg,
                drawerContentColor = textPrimary,
                modifier = Modifier.width(310.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Watchlist Sidebar Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFCC00),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Watchlist Board",
                            color = textPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    IconButton(
                        onClick = { scope.launch { drawerState.close() } },
                        modifier = Modifier.size(32.dp).testTag("close_watchlist_sidebar")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close Sidebar", tint = textMuted)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = borderLight)

                // Inline Watchlist adder card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderLight)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Add Stock Ticker",
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = inlineAddText,
                                onValueChange = { inlineAddText = it.uppercase() },
                                placeholder = { Text("e.g. TSLA", fontSize = 11.sp, color = textMuted) },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = lavenderPrimary,
                                    unfocusedBorderColor = borderLight
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("sidebar_watchlist_input")
                            )
                            Button(
                                onClick = {
                                    val clean = inlineAddText.trim()
                                    if (clean.isNotEmpty()) {
                                        viewModel.addStockToWatchlist(clean)
                                        inlineAddText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = lavenderPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("sidebar_watchlist_add_btn")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Text(
                    text = "MY WATCHLIST (${favoriteStocks.size})",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = lavenderPrimary,
                    letterSpacing = 1.sp
                )

                if (favoriteStocks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Your watchlist is empty.\nEnter stock tickers above to build your private panel.",
                            color = textMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(favoriteStocks, key = { it.symbol }) { stock ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderLight, RoundedCornerShape(12.dp))
                                    .background(cardBg, RoundedCornerShape(12.dp))
                                    .clickable {
                                        scope.launch { drawerState.close() }
                                        viewModel.selectStock(stock.symbol)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(lavenderSoft, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stock.symbol.take(2),
                                            color = violetDeep,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = stock.symbol,
                                            color = textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = stock.name.take(20) + (if (stock.name.length > 20) ".." else ""),
                                            color = textMuted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.toggleFavorite(stock.symbol, true) },
                                    modifier = Modifier.size(28.dp).testTag("sidebar_remove_${stock.symbol}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Remove from Watchlist",
                                        tint = accentRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick suggestions board
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "POPULAR CHIPS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val populars = listOf("AAPL", "TSLA", "NVDA", "NFLX", "MSFT", "GOOGL")
                        items(populars) { sym ->
                            val isAdded = favoriteStocks.any { it.symbol == sym }
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isAdded) lavenderSoft else Color(0xFFCAC4D0).copy(alpha = 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        if (!isAdded) {
                                            viewModel.addStockToWatchlist(sym)
                                        } else {
                                            scope.launch { drawerState.close() }
                                            viewModel.selectStock(sym)
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = sym,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAdded) violetDeep else textPrimary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(lavenderSoft, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountCircle,
                                        contentDescription = "Profile",
                                        tint = violetDeep,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "ANALYST PRO",
                                        color = lavenderPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Stock Intelligence",
                                        color = textPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            // Watchlist trigger icon button with badge count
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("watchlist_sidebar_toggle")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (favoriteStocks.isNotEmpty()) {
                                            Badge(
                                                containerColor = lavenderPrimary,
                                                contentColor = Color.White
                                            ) {
                                                Text(
                                                    text = favoriteStocks.size.toString(),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bookmarks,
                                        contentDescription = "Open Watchlist Sidebar",
                                        tint = lavenderPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = appBg,
                        titleContentColor = textPrimary
                    )
                )
            },
            containerColor = appBg
        ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(appBg)
        ) {
            // Searched Stock Search Bar matching bg-[#F3EDF7] rounded-2xl border-[#CAC4D0]
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("stock_search_bar"),
                placeholder = { Text("Search tickers, names (e.g., TSLA, LVMH, AAPL)...", color = textMuted, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search icon", tint = textMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search", tint = textMuted)
                        }
                    } else {
                        IconButton(onClick = { 
                            if (searchQuery.isNotBlank()) {
                                viewModel.selectStock(searchQuery)
                                keyboardController?.hide()
                            }
                        }) {
                            Icon(Icons.Outlined.QueryStats, contentDescription = "Query Stock", tint = lavenderPrimary)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary,
                    focusedContainerColor = navBg,
                    unfocusedContainerColor = navBg,
                    focusedBorderColor = lavenderPrimary,
                    unfocusedBorderColor = borderDark,
                    cursorColor = violetDeep
                ),
                shape = RoundedCornerShape(16.dp)
            )

            // Horizontal Ticker Strip
            PrebuiltTickersStrip(
                favoriteStocks = favoriteStocks,
                accentGreen = accentGreen,
                accentRed = accentRed,
                borderLight = borderLight,
                cardBg = cardBg,
                textPrimary = textPrimary,
                onTickerSelected = { symbol ->
                    viewModel.selectStock(symbol)
                    keyboardController?.hide()
                }
            )

            // Analysis state dispatcher
            when (val state = analysisState) {
                is AnalysisUiState.Idle -> {
                    // Show standard Catalog Explorer
                    TabsAndSelector(
                        activeTab = activeTab,
                        onTabChanged = { viewModel.setActiveTab(it) },
                        lavenderPrimary = lavenderPrimary,
                        navBg = navBg,
                        lavenderSoft = lavenderSoft,
                        textMuted = textMuted
                    )

                    val activeList = if (activeTab == 0) stocksList else favoriteStocks
                    
                    if (activeList.isEmpty()) {
                        EmptyStatePrompt(
                            searchQuery = searchQuery,
                            activeTab = activeTab,
                            textMuted = textMuted,
                            textPrimary = textPrimary,
                            accentGreen = accentGreen,
                            cardBg = cardBg,
                            borderLight = borderLight,
                            onQueryOnline = {
                                if (searchQuery.isNotBlank()) {
                                    viewModel.selectStock(searchQuery)
                                    keyboardController?.hide()
                                }
                            }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(activeList, key = { it.symbol }) { stock ->
                                StockCatalogItemCard(
                                    stock = stock,
                                    cardBg = cardBg,
                                    borderLight = borderLight,
                                    textMuted = textMuted,
                                    textPrimary = textPrimary,
                                    accentGreen = accentGreen,
                                    onSelect = { 
                                        viewModel.selectStock(stock.symbol)
                                        keyboardController?.hide()
                                    },
                                    onFavoriteToggle = { viewModel.toggleFavorite(stock.symbol, stock.isFavorite) }
                                )
                            }
                        }
                    }
                }
                is AnalysisUiState.Loading -> {
                    LoadingAnalysisProgress(
                        accentGreen = accentGreen,
                        textMuted = textMuted,
                        textPrimary = textPrimary,
                        borderLight = borderLight,
                        cardBg = cardBg
                    )
                }
                is AnalysisUiState.Success -> {
                    Box(modifier = Modifier.weight(1f)) {
                        DetailedAnalysisView(
                            analysis = state.analysis,
                            isFavorite = favoriteStocks.any { it.symbol == state.analysis.symbol && it.isFavorite },
                            onToggleFavorite = { viewModel.toggleFavorite(state.analysis.symbol, favoriteStocks.any { it.symbol == state.analysis.symbol && it.isFavorite }) },
                            onBack = { viewModel.clearSelectedStock() },
                            accentGreen = accentGreen,
                            accentRed = accentRed,
                            cardBg = cardBg,
                            borderLight = borderLight,
                            textMuted = textMuted,
                            textPrimary = textPrimary,
                            violetDeep = violetDeep,
                            lavenderPrimary = lavenderPrimary,
                            lavenderSoft = lavenderSoft,
                            navBg = navBg,
                            viewModel = viewModel
                        )
                    }
                }
                is AnalysisUiState.Error -> {
                    ErrorStateView(
                        message = state.message,
                        onRetry = {
                            if (searchQuery.isNotBlank()) {
                                viewModel.selectStock(searchQuery)
                            } else {
                                viewModel.clearSelectedStock()
                            }
                        },
                        onBack = { viewModel.clearSelectedStock() },
                        accentRed = accentRed,
                        cardBg = cardBg,
                        borderLight = borderLight,
                        textMuted = textMuted,
                        textPrimary = textPrimary
                    )
                }
            }
        }
    }
}
}

// --- Prebuilt quick tap ticker strip ---
@Composable
fun PrebuiltTickersStrip(
    favoriteStocks: List<StockEntity>,
    accentGreen: Color,
    accentRed: Color,
    borderLight: Color,
    cardBg: Color,
    textPrimary: Color,
    onTickerSelected: (String) -> Unit
) {
    val quickTickers = remember(favoriteStocks) {
        val base = listOf("AAPL", "TSLA", "NVDA", "RELIANCE", "LVMH", "ASML")
        val favSymbols = favoriteStocks.map { it.symbol }
        (favSymbols + base).distinct().take(8)
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(quickTickers) { ticker ->
            // Simulating price colors for active tickers
            val isEven = ticker.hashCode() % 2 == 0
            val color = if (isEven) accentGreen else accentRed
            val percent = if (isEven) "+1.42%" else "-0.85%"

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(cardBg)
                    .border(1.dp, borderLight, RoundedCornerShape(32.dp))
                    .clickable { onTickerSelected(ticker) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = ticker,
                        color = textPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            letterSpacing = 0.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Text(
                        text = percent,
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// --- Tab Selector ---
@Composable
fun TabsAndSelector(
    activeTab: Int,
    onTabChanged: (Int) -> Unit,
    lavenderPrimary: Color,
    navBg: Color,
    lavenderSoft: Color,
    textMuted: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(navBg, RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val tabs = listOf("Global Database", "My Watchlist")
        tabs.forEachIndexed { index, title ->
            val isSelected = activeTab == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) lavenderSoft else Color.Transparent)
                    .clickable { onTabChanged(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (index == 0) Icons.Filled.Public else Icons.Filled.Star,
                        contentDescription = title,
                        tint = if (isSelected) lavenderPrimary else textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        color = if (isSelected) Color(0xFF21005D) else textMuted,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// --- Empty representation state ---
@Composable
fun EmptyStatePrompt(
    searchQuery: String,
    activeTab: Int,
    textMuted: Color,
    textPrimary: Color,
    accentGreen: Color,
    cardBg: Color,
    borderLight: Color,
    onQueryOnline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (activeTab == 0) Icons.Filled.Hub else Icons.Filled.StarOutline,
            contentDescription = "Not found",
            tint = textMuted.copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        if (searchQuery.isNotEmpty()) {
            Text(
                text = "No local matches for '$searchQuery'",
                color = textPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Query Gemini stock AI database instantly to fetch live indicators for any ticker symbol in the world.",
                color = textMuted,
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onQueryOnline,
                colors = ButtonDefaults.buttonColors(containerColor = accentGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("analyze_online_button")
            ) {
                Icon(Icons.Filled.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyze '$searchQuery' via Global AI", color = Color.White, fontWeight = FontWeight.Bold)
            }
        } else {
            Text(
                text = if (activeTab == 0) "Stock Database Seed List is Loading" else "Your Watchlist is Empty",
                color = textPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (activeTab == 0) "Preloading global catalog items..." else "Type in any ticker symbol above to generate custom reports and add them to your persistent watchlist.",
                color = textMuted,
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )
        }
    }
}

// --- Catalog card ---
@Composable
fun StockCatalogItemCard(
    stock: StockEntity,
    cardBg: Color,
    borderLight: Color,
    textMuted: Color,
    textPrimary: Color,
    accentGreen: Color,
    onSelect: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stock_item_${stock.symbol}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stock.symbol,
                        color = textPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            letterSpacing = 0.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEADDFF), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stock.region,
                            color = Color(0xFF21005D),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stock.name,
                    color = textMuted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Sector: ${stock.sector}",
                    color = textMuted.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Little AI indicator badge if it has a cached report
                if (stock.cachedAnalysisJson != null) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Has Report Cached",
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { onFavoriteToggle() },
                    modifier = Modifier.testTag("favorite_toggle_${stock.symbol}")
                ) {
                    Icon(
                        imageVector = if (stock.isFavorite) Icons.Filled.Star else Icons.Filled.StarOutline,
                        contentDescription = "Watchlist toggle",
                        tint = if (stock.isFavorite) Color(0xFFFFCC00) else textMuted
                    )
                }
            }
        }
    }
}

// --- Dynamic skeleton loader state ---
@Composable
fun LoadingAnalysisProgress(
    accentGreen: Color,
    textMuted: Color,
    textPrimary: Color,
    borderLight: Color,
    cardBg: Color
) {
    // Staggered loading state strings to entertain the user during analysis
    val loadingSteps = listOf(
        "Initiating global stock database connection...",
        "Querying stock exchange historical records...",
        "Evaluating technical 14-Day RSI thresholds...",
        "Analyzing MACD crossovers and trend channels...",
        "Conducting fundamental balance sheet review...",
        "Synthesizing financial pros, risks, and consensus target...",
        "Drafting expert analytics report card..."
    )
    var currentStepIdx by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (currentStepIdx < loadingSteps.size - 1) {
            delay(1500)
            currentStepIdx++
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF6750A4),
            modifier = Modifier.size(64.dp),
            strokeWidth = 6.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "PRO AI FINANCIAL ANALYSIS",
            color = textPrimary,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = loadingSteps[currentStepIdx],
            color = textMuted,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Mock analysis blueprint preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(cardBg, RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, borderLight), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp).background(Color(0xFFF3EDF7), RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.fillMaxWidth(0.9f).height(12.dp).background(Color(0xFFF3EDF7), RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.fillMaxWidth(0.7f).height(12.dp).background(Color(0xFFF3EDF7), RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.fillMaxWidth(0.85f).height(12.dp).background(Color(0xFFF3EDF7), RoundedCornerShape(4.dp)))
            }
        }
    }
}

// --- Recharts-Inspired Custom Interactive Line Chart with Touch tooltips and formatted Axes ---
data class HistoricalChartPoint(
    val label: String,
    val price: Float,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Long,
    val isUp: Boolean,
    val sma50: Float,
    val sma200: Float,
    val macd: Float = 0f,
    val signal: Float = 0f,
    val histogram: Float = 0f
)

@Composable
fun StockPerformanceChart(
    prices: List<Float>,
    support: String,
    resistance: String,
    isPositive: Boolean,
    accentGreen: Color,
    accentRed: Color,
    textMuted: Color,
    symbol: String = "Stock",
    modifier: Modifier = Modifier
) {
    if (prices.size < 2) return

    val strokeColor = if (isPositive) accentGreen else accentRed
    
    // Timeframe state
    val timeframes = listOf("1M", "3M", "6M", "1Y", "YTD")
    var selectedTimeframe by remember { mutableStateOf("1M") }

    // Dynamic historical data points (30 points) based on selected timeframe
    val historicalData = remember(symbol, selectedTimeframe, prices) {
        generateHistoricalData(symbol, selectedTimeframe, prices)
    }

    val dynamicPrices = historicalData.map { it.price }
    val dynamicVolume = historicalData.map { it.volume }

    val minVal = remember(historicalData) {
        historicalData.flatMap { listOf(it.price, it.sma50, it.sma200) }.minOrNull() ?: 0f
    }
    val maxVal = remember(historicalData) {
        historicalData.flatMap { listOf(it.price, it.sma50, it.sma200) }.maxOrNull() ?: 100f
    }
    val diff = (maxVal - minVal).let { if (it == 0f) 1f else it }

    val rsiValues = remember(dynamicPrices) {
        calculateMiniRSI(dynamicPrices)
    }

    // Formatted labels for Y-Axis (max, mid, min)
    val yLabels = remember(minVal, maxVal) {
        listOf(
            String.format("%.2f", maxVal),
            String.format("%.2f", minVal + diff * 0.5f),
            String.format("%.2f", minVal)
        )
    }

    // Days representation of spaced labels for X-Axis (show every 6th label to avoid crowding)
    val daysLabels = historicalData.map { it.label }

    var activeIndex by remember { mutableStateOf<Int?>(null) }
    var activeOffset by remember { mutableStateOf<Offset?>(null) }
    
    // Touch tracking helper to compute activeIndex over any list
    val listSize = historicalData.size

    Column(modifier = modifier) {
        // Core Chart Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RECHARTS PRO LIVE PATH",
                    color = Color(0xFF6750A4),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$selectedTimeframe Historical Trend",
                    color = Color(0xFF1D1B20),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = "Res: $resistance | Sup: $support",
                color = textMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // SMA Legend Overlay
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Price Indicator
            Box(
                modifier = Modifier
                    .size(width = 12.dp, height = 3.dp)
                    .background(strokeColor, RoundedCornerShape(1.5.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Price",
                color = Color(0xFF1D1B20),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(16.dp))

            // SMA 50 Indicator
            Box(
                modifier = Modifier
                    .size(width = 12.dp, height = 3.dp)
                    .background(Color(0xFFFF9800), RoundedCornerShape(1.5.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "50-Day SMA",
                color = Color(0xFF1D1B20),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(16.dp))

            // SMA 200 Indicator
            Box(
                modifier = Modifier
                    .size(width = 12.dp, height = 3.dp)
                    .background(Color(0xFF2196F3), RoundedCornerShape(1.5.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "200-Day SMA",
                color = Color(0xFF1D1B20),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Timeframe Selector Pill Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3EDF7), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            timeframes.forEach { tf ->
                val isActive = selectedTimeframe == tf
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) Color(0xFF6750A4) else Color.Transparent)
                        .clickable {
                            selectedTimeframe = tf
                            activeIndex = null
                            activeOffset = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tf,
                        color = if (isActive) Color.White else Color(0xFF49454F),
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. MAIN PRICE CHART
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Left Y-Axis labels (equal space vertical allocation)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(42.dp)
                    .padding(end = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                yLabels.forEach { label ->
                    Text(
                        text = label,
                        color = textMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Divider vertical axis line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color(0xFFCAC4D0).copy(alpha = 0.5f))
            )

            // Plot Area Box with Canvas & Tooltip Overlays
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(dynamicPrices) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val anyPressed = event.changes.any { it.pressed }
                                if (!anyPressed) {
                                    activeIndex = null
                                    activeOffset = null
                                } else {
                                    val pointer = event.changes.firstOrNull()
                                    if (pointer != null) {
                                        val x = pointer.position.x
                                        val width = size.width.toFloat()

                                        val segmentWidth = width / (listSize - 1).coerceAtLeast(1)
                                        val rawIdx = (x / segmentWidth).roundToInt()
                                        val idx = rawIdx.coerceIn(0, listSize - 1)

                                        activeIndex = idx

                                        // Compute graph-fitted offset
                                        val fitX = idx * segmentWidth
                                        val fitY = size.height - ((dynamicPrices[idx] - minVal) / diff) * size.height
                                        activeOffset = Offset(fitX, fitY)
                                        pointer.consume()
                                    }
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val segmentWidth = width / (listSize - 1).coerceAtLeast(1)

                    // Draw Background Grid (Horizontal lines matching our Y Labels)
                    val gridStroke = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
                    val gridColor = Color(0xFFCAC4D0).copy(alpha = 0.4f)

                    // Lines at 0%, 50%, and 100% heights
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, 0f),
                        end = Offset(width, 0f),
                        strokeWidth = 1f,
                        pathEffect = gridStroke.pathEffect
                    )
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, height * 0.5f),
                        end = Offset(width, height * 0.5f),
                        strokeWidth = 1f,
                        pathEffect = gridStroke.pathEffect
                    )
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, height),
                        end = Offset(width, height),
                        strokeWidth = 1f,
                        pathEffect = gridStroke.pathEffect
                    )

                    // Draw Vertical Grid lines corresponding to each data column point
                    for (i in 0 until listSize) {
                        val gridX = i * segmentWidth
                        drawLine(
                            color = gridColor,
                            start = Offset(gridX, 0f),
                            end = Offset(gridX, height),
                            strokeWidth = 1f,
                            pathEffect = gridStroke.pathEffect
                        )
                    }

                    // Map elements to point offsets
                    val points = dynamicPrices.mapIndexed { idx, price ->
                        val x = idx * segmentWidth
                        val y = height - ((price - minVal) / diff) * height
                        Offset(x, y)
                    }

                    val sma50Points = historicalData.mapIndexed { idx, point ->
                        val x = idx * segmentWidth
                        val y = height - ((point.sma50 - minVal) / diff) * height
                        Offset(x, y)
                    }

                    val sma200Points = historicalData.mapIndexed { idx, point ->
                        val x = idx * segmentWidth
                        val y = height - ((point.sma200 - minVal) / diff) * height
                        Offset(x, y)
                    }

                    // Build graph smooth curve path
                    val graphPath = Path().apply {
                        if (points.isNotEmpty()) {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val pPrev = points[i - 1]
                                val pCurr = points[i]
                                // Smooth cubic control points
                                val cpX1 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                                val cpY1 = pPrev.y
                                val cpX2 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                                val cpY2 = pCurr.y
                                cubicTo(cpX1, cpY1, cpX2, cpY2, pCurr.x, pCurr.y)
                            }
                        }
                    }

                    // Build SMA 50 smooth curve path
                    val sma50Path = Path().apply {
                        if (sma50Points.isNotEmpty()) {
                            moveTo(sma50Points.first().x, sma50Points.first().y)
                            for (i in 1 until sma50Points.size) {
                                val pPrev = sma50Points[i - 1]
                                val pCurr = sma50Points[i]
                                val cpX1 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                                val cpY1 = pPrev.y
                                val cpX2 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                                val cpY2 = pCurr.y
                                cubicTo(cpX1, cpY1, cpX2, cpY2, pCurr.x, pCurr.y)
                            }
                        }
                    }

                    // Build SMA 200 smooth curve path
                    val sma200Path = Path().apply {
                        if (sma200Points.isNotEmpty()) {
                            moveTo(sma200Points.first().x, sma200Points.first().y)
                            for (i in 1 until sma200Points.size) {
                                val pPrev = sma200Points[i - 1]
                                val pCurr = sma200Points[i]
                                val cpX1 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                                val cpY1 = pPrev.y
                                val cpX2 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                                val cpY2 = pCurr.y
                                cubicTo(cpX1, cpY1, cpX2, cpY2, pCurr.x, pCurr.y)
                            }
                        }
                    }

                    // Gradient underfill
                    val fillPath = Path().apply {
                        addPath(graphPath)
                        if (points.isNotEmpty()) {
                            lineTo(points.last().x, height)
                            lineTo(points.first().x, height)
                        }
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(strokeColor.copy(alpha = 0.25f), Color.Transparent),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Draw SMA 50 Line (Orange)
                    drawPath(
                        path = sma50Path,
                        color = Color(0xFFFF9800),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )

                    // Draw SMA 200 Line (Blue)
                    drawPath(
                        path = sma200Path,
                        color = Color(0xFF2196F3),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )

                    // Main trend line (Recharts default strokeWidth 5f)
                    drawPath(
                        path = graphPath,
                        color = strokeColor,
                        style = Stroke(width = 4.5f, cap = StrokeCap.Round)
                    )

                    // Interactive crosshair & active highlight drawing
                    val currentIdx = activeIndex
                    if (currentIdx != null) {
                        val activePoint = points[currentIdx]

                        // Vertical guideline
                        drawLine(
                            color = strokeColor.copy(alpha = 0.7f),
                            start = Offset(activePoint.x, 0f),
                            end = Offset(activePoint.x, height),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        // Highlight halo circles
                        drawCircle(
                            color = strokeColor.copy(alpha = 0.15f),
                            radius = 16f,
                            center = activePoint
                        )
                        drawCircle(
                            color = strokeColor,
                            radius = 7f,
                            center = activePoint
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.5f,
                            center = activePoint
                        )
                    } else {
                        // Standard static endpoint highlights as accent anchors
                        if (points.isNotEmpty()) {
                            drawCircle(
                                color = strokeColor,
                                radius = 6f,
                                center = points.last()
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3f,
                                center = points.last()
                            )
                        }
                    }
                }

                // Interactive Floating Tooltip Overlay matching high-density Pro theme
                val currentIdx = activeIndex
                val currentOffset = activeOffset
                if (currentIdx != null && currentOffset != null) {
                    val pt = historicalData.getOrNull(currentIdx)
                    val priceVal = dynamicPrices[currentIdx]
                    val dayLabel = daysLabels.getOrElse(currentIdx) { "Day ${currentIdx + 1}" }
                    val rsiVal = rsiValues.getOrNull(currentIdx) ?: 50f
                    val volumeVal = dynamicVolume.getOrNull(currentIdx) ?: 1000000L
                    val sma50Val = pt?.sma50 ?: priceVal
                    val sma200Val = pt?.sma200 ?: priceVal
                    val macdVal = pt?.macd ?: 0f
                    val signalVal = pt?.signal ?: 0f
                    val histVal = pt?.histogram ?: 0f

                    val openVal = pt?.open ?: priceVal
                    val highVal = pt?.high ?: priceVal
                    val lowVal = pt?.low ?: priceVal
                    val closeVal = pt?.close ?: priceVal

                    val density = LocalDensity.current
                    
                    // We can compute position cleanly in Dp
                    val xDp = with(density) { currentOffset.x.toDp() }
                    val yDp = with(density) { currentOffset.y.toDp() }
                    val tooltipWidth = 224.dp
                    
                    val xOffset = (xDp - 112.dp).coerceIn(0.dp, (maxWidth - tooltipWidth).coerceAtLeast(0.dp))
                    val yOffset = (yDp - 195.dp).coerceIn((-50).dp, (maxHeight - 110.dp).coerceAtLeast(0.dp))

                    Box(
                        modifier = Modifier
                            .offset(x = xOffset, y = yOffset)
                            .width(tooltipWidth)
                            .background(Color(0xE61D1B20), RoundedCornerShape(12.dp)) // subtle transparency for premium feel
                            .border(BorderStroke(1.dp, Color(0xFFEADDFF).copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                            .testTag("hover_crosshair_tooltip"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$symbol • $dayLabel",
                                    color = Color(0xFFEADDFF),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (pt?.isUp == true) Color(0xFF34C759).copy(alpha = 0.2f) else Color(0xFFFF3B30).copy(alpha = 0.2f),
                                            RoundedCornerShape(4.dp)
                                        )
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (pt?.isUp == true) "UP" else "DOWN",
                                        color = if (pt?.isUp == true) Color(0xFF34C759) else Color(0xFFFF3B30),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = Color(0xFFCAC4D0).copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(4.dp))

                            // OHLC Values Row
                            Text(
                                text = "OHLC METRICS",
                                color = Color(0xFFEADDFF).copy(alpha = 0.6f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = String.format("O: $%.2f", openVal),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = String.format("L: $%.2f", lowVal),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = String.format("H: $%.2f", highVal),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = String.format("C: $%.2f", closeVal),
                                        color = Color(0xFFECE6F0),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = Color(0xFFCAC4D0).copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(4.dp))

                            // Indicators Row
                            Text(
                                text = "TECHNICAL CHANNELS",
                                color = Color(0xFFEADDFF).copy(alpha = 0.6f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1.1f)) {
                                    val rsiStatus = when {
                                        rsiVal >= 70f -> "OB"
                                        rsiVal <= 30f -> "OS"
                                        else -> "NEU"
                                    }
                                    val rsiStatusColor = when {
                                        rsiVal >= 70f -> "#FFB4AB"
                                        rsiVal <= 30f -> "#B4E6B4"
                                        else -> "#EADDFF"
                                    }
                                    Text(
                                        text = String.format("RSI: %.1f (%s)", rsiVal, rsiStatus),
                                        color = Color(android.graphics.Color.parseColor(rsiStatusColor)),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = String.format("Vol: %.2fM", volumeVal / 1000000f),
                                        color = Color(0xFFA8C7FA),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = String.format("MACD: %.2f", macdVal),
                                        color = Color(0xFF90CAF9),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(modifier = Modifier.weight(0.9f)) {
                                    Text(
                                        text = String.format("SMA50: $%.1f", sma50Val),
                                        color = Color(0xFFFFCC80),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = String.format("SMA200: $%.1f", sma200Val),
                                        color = Color(0xFF4FC3F7),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = String.format("Hist: %.2f", histVal),
                                        color = if (histVal >= 0f) Color(0xFF81C784) else Color(0xFFE57373),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. SECONDARY RSI CHART
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color(0xFFEADDFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("RSI", color = Color(0xFF21005D), fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Relative Strength Index (RSI)",
                    color = Color(0xFF1D1B20),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            val activeRsiValue = activeIndex?.let { rsiValues.getOrNull(it) } ?: rsiValues.lastOrNull() ?: 50f
            val rsiStatus = when {
                activeRsiValue >= 70f -> "Overbought (Sell)"
                activeRsiValue <= 30f -> "Oversold (Buy)"
                else -> "Neutral"
            }
            val rsiStatusColor = when {
                activeRsiValue >= 70f -> accentRed
                activeRsiValue <= 30f -> accentGreen
                else -> textMuted
            }
            Text(
                text = String.format("RSI: %.1f (%s)", activeRsiValue, rsiStatus),
                color = rsiStatusColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Y-Axis labels for RSI (equal space vertical allocation bounds)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(42.dp)
                    .padding(end = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(text = "70", color = textMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text(text = "50", color = textMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text(text = "30", color = textMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }

            // Divider vertical axis line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color(0xFFCAC4D0).copy(alpha = 0.5f))
            )

            // Plot Area Box for Dynamic RSI
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(dynamicPrices) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val anyPressed = event.changes.any { it.pressed }
                                if (!anyPressed) {
                                    activeIndex = null
                                    activeOffset = null
                                } else {
                                    val pointer = event.changes.firstOrNull()
                                    if (pointer != null) {
                                        val x = pointer.position.x
                                        val width = size.width.toFloat()

                                        val segmentWidth = width / (listSize - 1).coerceAtLeast(1)
                                        val rawIdx = (x / segmentWidth).roundToInt()
                                        val idx = rawIdx.coerceIn(0, listSize - 1)

                                        activeIndex = idx

                                        // Compute graph-fitted offset
                                        val fitX = idx * segmentWidth
                                        val fitY = size.height - ((dynamicPrices[idx] - minVal) / diff) * size.height
                                        activeOffset = Offset(fitX, fitY)
                                        pointer.consume()
                                    }
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val segmentWidth = width / (rsiValues.size - 1).coerceAtLeast(1)

                    val y70 = height * 0.3f
                    val y50 = height * 0.5f
                    val y30 = height * 0.7f

                    val limitStroke = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
                    val overboughtColor = accentRed.copy(alpha = 0.5f)
                    val oversoldColor = accentGreen.copy(alpha = 0.5f)
                    val neutralLineColor = Color(0xFFCAC4D0).copy(alpha = 0.3f)

                    // Draw oversold threshold fill shaded background band
                    drawRect(
                        color = Color(0xFFFFB4AB).copy(alpha = 0.08f),
                        topLeft = Offset(0f, 0f),
                        size = Size(width, y70)
                    )
                    drawRect(
                        color = Color(0xFFB4E6B4).copy(alpha = 0.08f),
                        topLeft = Offset(0f, y30),
                        size = Size(width, height - y30)
                    )

                    // Overbought line (70)
                    drawLine(
                        color = overboughtColor,
                        start = Offset(0f, y70),
                        end = Offset(width, y70),
                        strokeWidth = 1f,
                        pathEffect = limitStroke.pathEffect
                    )

                    // Neutral line (50)
                    drawLine(
                        color = neutralLineColor,
                        start = Offset(0f, y50),
                        end = Offset(width, y50),
                        strokeWidth = 1f,
                        pathEffect = limitStroke.pathEffect
                    )

                    // Oversold line (30)
                    drawLine(
                        color = oversoldColor,
                        start = Offset(0f, y30),
                        end = Offset(width, y30),
                        strokeWidth = 1f,
                        pathEffect = limitStroke.pathEffect
                    )

                    // Draw vertical guides matching the price columns
                    for (i in 0 until listSize) {
                        val gridX = i * segmentWidth
                        drawLine(
                            color = Color(0xFFCAC4D0).copy(alpha = 0.12f),
                            start = Offset(gridX, 0f),
                            end = Offset(gridX, height),
                            strokeWidth = 1f
                        )
                    }

                    // Map RSI values to Canvas coordinate Points
                    val rsiPoints = rsiValues.mapIndexed { idx, rsiVal ->
                        val x = idx * segmentWidth
                        val y = height - (rsiVal / 100f) * height
                        Offset(x, y)
                    }

                    val rsiPath = Path().apply {
                        if (rsiPoints.isNotEmpty()) {
                            moveTo(rsiPoints.first().x, rsiPoints.first().y)
                            for (i in 1 until rsiPoints.size) {
                                val pPrev = rsiPoints[i - 1]
                                val pCurr = rsiPoints[i]
                                val cpX1 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                                val cpY1 = pPrev.y
                                val cpX2 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                                val cpY2 = pCurr.y
                                cubicTo(cpX1, cpY1, cpX2, cpY2, pCurr.x, pCurr.y)
                            }
                        }
                    }

                    // Draw main RSI Trend track line
                    val rsiTrackColor = Color(0xFF6750A4)
                    drawPath(
                        path = rsiPath,
                        color = rsiTrackColor,
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )

                    // Highlights on interactive hover matching the dragging coordinate
                    val currentIdx = activeIndex
                    if (currentIdx != null) {
                        val activePoint = rsiPoints[currentIdx]

                        drawLine(
                            color = Color(0xFF6750A4).copy(alpha = 0.5f),
                            start = Offset(activePoint.x, 0f),
                            end = Offset(activePoint.x, height),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        drawCircle(
                            color = rsiTrackColor.copy(alpha = 0.2f),
                            radius = 12f,
                            center = activePoint
                        )
                        drawCircle(
                            color = rsiTrackColor,
                            radius = 5.5f,
                            center = activePoint
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5f,
                            center = activePoint
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. INTEGRATED VOLUME HISTOGRAM (RECHARTS STYLE)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color(0xFFE3F2FD), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.BarChart,
                        contentDescription = "Volume Icon",
                        tint = Color(0xFF0D47A1),
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Daily Trading Volume (Shares)",
                    color = Color(0xFF1D1B20),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            val activeVolume = activeIndex?.let { dynamicVolume.getOrNull(it) } ?: dynamicVolume.lastOrNull() ?: 1000000L
            Text(
                text = String.format("VOL: %,d", activeVolume),
                color = Color(0xFF0D47A1),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            verticalAlignment = Alignment.Top
        ) {
            val maxVol = dynamicVolume.maxOrNull() ?: 10_000_000L
            val yVolLabels = listOf(
                String.format("%.1fM", maxVol / 1000000f),
                String.format("%.1fM", (maxVol / 2f) / 1000000f),
                "0.0M"
            )

            // Left Y-Axis labels for volume chart
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(42.dp)
                    .padding(end = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                yVolLabels.forEach { label ->
                    Text(
                        text = label,
                        color = textMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Divider vertical axis line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color(0xFFCAC4D0).copy(alpha = 0.5f))
            )

            // Plot Area Box for Volume Bars
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(dynamicPrices) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val anyPressed = event.changes.any { it.pressed }
                                if (!anyPressed) {
                                    activeIndex = null
                                    activeOffset = null
                                } else {
                                    val pointer = event.changes.firstOrNull()
                                    if (pointer != null) {
                                        val x = pointer.position.x
                                        val width = size.width.toFloat()

                                        val segmentWidth = width / (listSize - 1).coerceAtLeast(1)
                                        val rawIdx = (x / segmentWidth).roundToInt()
                                        val idx = rawIdx.coerceIn(0, listSize - 1)

                                        activeIndex = idx

                                        // Compute graph-fitted offset
                                        val fitX = idx * segmentWidth
                                        val fitY = size.height - ((dynamicPrices[idx] - minVal) / diff) * size.height
                                        activeOffset = Offset(fitX, fitY)
                                        pointer.consume()
                                    }
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val segmentWidth = width / (listSize - 1).coerceAtLeast(1)
                    val barWidth = (segmentWidth * 0.7f).coerceAtLeast(2f)

                    // Draw vertical guides matching the price columns
                    for (i in 0 until listSize) {
                        val gridX = i * segmentWidth
                        drawLine(
                            color = Color(0xFFCAC4D0).copy(alpha = 0.12f),
                            start = Offset(gridX, 0f),
                            end = Offset(gridX, height),
                            strokeWidth = 1f
                        )
                    }

                    // Draw volume bar rectangles
                    historicalData.forEachIndexed { i, point ->
                        val barHeight = (point.volume.toFloat() / maxVol.coerceAtLeast(1L)) * height
                        val barX = i * segmentWidth
                        
                        // Decide bar color based on market gain/loss with transparency (Recharts best style)
                        val isHighlighted = activeIndex == i
                        val barColor = if (point.isUp) {
                            accentGreen.copy(alpha = if (isHighlighted) 0.9f else 0.5f)
                        } else {
                            accentRed.copy(alpha = if (isHighlighted) 0.9f else 0.5f)
                        }

                        drawRect(
                            color = barColor,
                            topLeft = Offset(barX - barWidth / 2f, height - barHeight),
                            size = Size(barWidth, barHeight)
                        )
                    }

                    // Highlight guide on hover
                    val currentIdx = activeIndex
                    if (currentIdx != null) {
                        val selectX = currentIdx * segmentWidth
                        
                        drawLine(
                            color = Color(0xFF6750A4).copy(alpha = 0.5f),
                            start = Offset(selectX, 0f),
                            end = Offset(selectX, height),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. MOMENTUM MACD INDICATOR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color(0xFFE8F5E9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("MACD", color = Color(0xFF1B5E20), fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MACD (12, 26, 9) Momentum",
                    color = Color(0xFF1D1B20),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            val activeIdx = activeIndex ?: (listSize - 1)
            val currentPt = historicalData.getOrNull(activeIdx)
            val mVal = currentPt?.macd ?: 0f
            val sVal = currentPt?.signal ?: 0f
            val hVal = currentPt?.histogram ?: 0f
            Text(
                text = String.format("MACD: %.2f | Sig: %.2f | Hist: %.2f", mVal, sVal, hVal),
                color = if (hVal >= 0) accentGreen else accentRed,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            verticalAlignment = Alignment.Top
        ) {
            val maxMacdAbs = remember(historicalData) {
                historicalData.flatMap { listOf(Math.abs(it.macd), Math.abs(it.signal), Math.abs(it.histogram)) }.maxOrNull() ?: 1f
            }
            val macdMaxVal = if (maxMacdAbs == 0f) 1f else maxMacdAbs

            val yMacdLabels = listOf(
                String.format("%.2f", macdMaxVal),
                "0.00",
                String.format("%.2f", -macdMaxVal)
            )

            // Left Y-Axis labels for MACD
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(42.dp)
                    .padding(end = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                yMacdLabels.forEach { label ->
                    Text(
                        text = label,
                        color = textMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Divider vertical axis line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color(0xFFCAC4D0).copy(alpha = 0.5f))
            )

            // Plot Area Box for MACD lines and bars
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(dynamicPrices) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val anyPressed = event.changes.any { it.pressed }
                                if (!anyPressed) {
                                    activeIndex = null
                                    activeOffset = null
                                } else {
                                    val pointer = event.changes.firstOrNull()
                                    if (pointer != null) {
                                        val x = pointer.position.x
                                        val width = size.width.toFloat()

                                        val segmentWidth = width / (listSize - 1).coerceAtLeast(1)
                                        val rawIdx = (x / segmentWidth).roundToInt()
                                        val idx = rawIdx.coerceIn(0, listSize - 1)

                                        activeIndex = idx

                                        // Compute graph-fitted offset
                                        val fitX = idx * segmentWidth
                                        val fitY = size.height - ((dynamicPrices[idx] - minVal) / diff) * size.height
                                        activeOffset = Offset(fitX, fitY)
                                        pointer.consume()
                                    }
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val segmentWidth = width / (listSize - 1).coerceAtLeast(1)

                    // Draw reference zero line
                    drawLine(
                        color = Color(0xFFCAC4D0).copy(alpha = 0.5f),
                        start = Offset(0f, height * 0.5f),
                        end = Offset(width, height * 0.5f),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    // Compute points
                    val macdPoints = historicalData.mapIndexed { idx, point ->
                        val x = idx * segmentWidth
                        val y = height * 0.5f - (point.macd / macdMaxVal) * (height * 0.5f)
                        Offset(x, y)
                    }

                    val signalPoints = historicalData.mapIndexed { idx, point ->
                        val x = idx * segmentWidth
                        val y = height * 0.5f - (point.signal / macdMaxVal) * (height * 0.5f)
                        Offset(x, y)
                    }

                    // 1. Draw Histogram
                    val barWidth = (segmentWidth * 0.6f).coerceAtLeast(1.5f)
                    historicalData.forEachIndexed { idx, point ->
                        val barX = idx * segmentWidth
                        val histY = height * 0.5f - (point.histogram / macdMaxVal) * (height * 0.5f)
                        val barColor = if (point.histogram >= 0) accentGreen.copy(alpha = 0.5f) else accentRed.copy(alpha = 0.5f)
                        
                        drawRect(
                            color = barColor,
                            topLeft = Offset(barX - barWidth / 2f, Math.min(height * 0.5f, histY)),
                            size = Size(barWidth, Math.abs(height * 0.5f - histY))
                        )
                    }

                    // 2. Draw MACD Line
                    val macdPath = Path().apply {
                        if (macdPoints.isNotEmpty()) {
                            moveTo(macdPoints.first().x, macdPoints.first().y)
                            for (i in 1 until macdPoints.size) {
                                val pPrev = macdPoints[i - 1]
                                val pCurr = macdPoints[i]
                                val cpX1 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                                val cpY1 = pPrev.y
                                val cpX2 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                                val cpY2 = pCurr.y
                                cubicTo(cpX1, cpY1, cpX2, cpY2, pCurr.x, pCurr.y)
                            }
                        }
                    }
                    drawPath(
                        path = macdPath,
                        color = Color(0xFF2196F3),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )

                    // 3. Draw Signal Line
                    val signalPath = Path().apply {
                        if (signalPoints.isNotEmpty()) {
                            moveTo(signalPoints.first().x, signalPoints.first().y)
                            for (i in 1 until signalPoints.size) {
                                val pPrev = signalPoints[i - 1]
                                val pCurr = signalPoints[i]
                                val cpX1 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                                val cpY1 = pPrev.y
                                val cpX2 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                                val cpY2 = pCurr.y
                                cubicTo(cpX1, cpY1, cpX2, cpY2, pCurr.x, pCurr.y)
                            }
                        }
                    }
                    drawPath(
                        path = signalPath,
                        color = Color(0xFFFF9800),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )

                    // Interaction highlighting vertical crosshair segment tracker
                    val currentIdx = activeIndex
                    if (currentIdx != null) {
                        val selectX = currentIdx * segmentWidth
                        drawLine(
                            color = Color(0xFF6750A4).copy(alpha = 0.5f),
                            start = Offset(selectX, 0f),
                            end = Offset(selectX, height),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }
                }
            }
        }

        // Horizontal X-Axis line
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(43.dp)) // Offset to align with standard Plot Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color(0xFFCAC4D0).copy(alpha = 0.5f))
            )
        }

        // Bottom X-Axis labels aligning with points
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.width(43.dp)) // Offset to align with Standard Plot Area
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Show 5 spaced dates on the X-Axis label bar to prevent overlap clutter
                val interval = (listSize - 1) / 4
                for (i in 0..4) {
                    val idx = (i * interval).coerceAtMost(listSize - 1)
                    val label = daysLabels.getOrNull(idx) ?: ""
                    Text(
                        text = label,
                        color = textMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Tap/Interact Instruction Banner
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .background(Color(0xFFF3EDF7), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.TouchApp,
                contentDescription = null,
                tint = Color(0xFF6750A4),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Hold & drag cursor to inspect price, RSI, & volume in real-time",
                color = Color(0xFF21005D),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun generateHistoricalData(
    symbol: String,
    timeframe: String,
    basePrices: List<Float>
): List<HistoricalChartPoint> {
    val seed = (symbol.hashCode() + timeframe.hashCode()).toLong()
    val random = java.util.Random(seed)
    
    // Determine the day step/spacing representing the timeframe
    val step = when (timeframe) {
        "1M" -> 1
        "3M" -> 3
        "6M" -> 6
        "1Y" -> 12
        "YTD" -> 5
        else -> 1
    }
    
    val numPoints = 30
    val displayDays = numPoints * step
    val numRaw = displayDays + 200 // 200 days prior for SMA200 calculation
    
    // Base price reference starts from the last or mean price in basePrices
    val basePrice = basePrices.lastOrNull() ?: 150f
    
    // Generate raw daily price walk
    val rawPrices = ArrayList<Float>()
    var currentPrice = basePrice * 0.9f // start a bit lower
    
    val volatility = when (timeframe) {
        "1M" -> 0.015f  // 1.5% max daily change
        "3M" -> 0.022f  // 2.2% max change
        "6M" -> 0.030f  // 3% max change
        "1Y" -> 0.045f  // 4.5% max change
        "YTD" -> 0.028f // 2.8% max change
        else -> 0.02f
    }
    
    val trendBias = if (random.nextBoolean()) 0.002f else -0.001f
    
    for (i in 0 until numRaw) {
        val changePct = (random.nextFloat() * 2f - 1f) * volatility + trendBias
        currentPrice *= (1f + changePct)
        rawPrices.add(currentPrice)
    }
    
    // Smooth raw prices so they end exactly at basePrice
    val finalDiff = basePrice - rawPrices.last()
    val adjustedRawPrices = rawPrices.mapIndexed { idx, p ->
        p + finalDiff * (idx.toFloat() / (numRaw - 1))
    }
    
    // Generate volume points for the entire sequence (usually between 1.5M and 12.5M shares)
    val rawVolumes = ArrayList<Long>()
    for (i in 0 until numRaw) {
        val pExpr = adjustedRawPrices[i]
        val prevPrice = if (i > 0) adjustedRawPrices[i - 1] else pExpr
        val deviation = Math.abs(pExpr - prevPrice) / prevPrice.coerceAtLeast(0.01f)
        val baseVol = 3000000 + random.nextInt(5000000)
        val volBonus = (deviation * 15000000).toLong()
        rawVolumes.add(baseVol + volBonus)
    }

    val calculateEMA = { prices: List<Float>, period: Int ->
        val ema = ArrayList<Float>()
        if (prices.isNotEmpty()) {
            val k = 2f / (period + 1)
            var currentEma = prices[0]
            ema.add(currentEma)
            for (idx in 1 until prices.size) {
                currentEma = prices[idx] * k + currentEma * (1f - k)
                ema.add(currentEma)
            }
        }
        ema
    }

    val ema12 = calculateEMA(adjustedRawPrices, 12)
    val ema26 = calculateEMA(adjustedRawPrices, 26)
    val macdValues = List(adjustedRawPrices.size) { idx ->
        if (idx < ema12.size && idx < ema26.size) ema12[idx] - ema26[idx] else 0f
    }
    val signalValues = calculateEMA(macdValues, 9)
    val histValues = List(adjustedRawPrices.size) { idx ->
        if (idx < macdValues.size && idx < signalValues.size) macdValues[idx] - signalValues[idx] else 0f
    }
    
    // Now construct the 30 display points from the last `displayDays` slice
    val points = ArrayList<HistoricalChartPoint>()
    for (i in 0 until numPoints) {
        val rawIdx = 200 + (i * step)
        val pExpr = adjustedRawPrices[rawIdx]
        val prevPrice = if (rawIdx > 0) adjustedRawPrices[rawIdx - 1] else pExpr
        val isUp = pExpr >= prevPrice
        val finalVolume = rawVolumes[rawIdx]
        val macdVal = macdValues.getOrElse(rawIdx) { 0f }
        val signalVal = signalValues.getOrElse(rawIdx) { 0f }
        val histVal = histValues.getOrElse(rawIdx) { 0f }
        
        // Compute SMA50
        var sum50 = 0f
        for (k in (rawIdx - 49)..rawIdx) {
            sum50 += adjustedRawPrices[k]
        }
        val sma50 = sum50 / 50f
        
        // Compute SMA200
        var sum200 = 0f
        for (k in (rawIdx - 199)..rawIdx) {
            sum200 += adjustedRawPrices[k]
        }
        val sma200 = sum200 / 200f
        
        // Generate beautiful date labels depending on index
        val label = when (timeframe) {
            "1M" -> {
                val day = 6 + i
                if (day <= 31) "May $day" else "Jun ${day - 31}"
            }
            "3M" -> {
                val totalDays = i * 3
                if (totalDays < 25) "Mar ${totalDays + 10}"
                else if (totalDays < 55) "Apr ${totalDays - 25}"
                else if (totalDays < 86) "May ${totalDays - 55}"
                else "Jun ${totalDays - 85}"
            }
            "6M" -> {
                val totalDays = i * 6
                if (totalDays < 21) "Dec ${totalDays + 10}"
                else if (totalDays < 52) "Jan ${totalDays - 21}"
                else if (totalDays < 80) "Feb ${totalDays - 52}"
                else if (totalDays < 111) "Mar ${totalDays - 80}"
                else if (totalDays < 141) "Apr ${totalDays - 111}"
                else if (totalDays < 172) "May ${totalDays - 141}"
                else "Jun ${totalDays - 171}"
            }
            "1Y" -> {
                val totalDays = i * 12
                val monthsList = listOf("Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar", "Apr", "May")
                val monthIdx = (totalDays / 30).coerceAtMost(11)
                val dayVal = (totalDays % 30) + 1
                "${monthsList[monthIdx]} $dayVal"
            }
            "YTD" -> {
                val totalDays = i * 5
                if (totalDays < 31) "Jan ${totalDays + 1}"
                else if (totalDays < 59) "Feb ${totalDays - 30}"
                else if (totalDays < 90) "Mar ${totalDays - 58}"
                else if (totalDays < 120) "Apr ${totalDays - 89}"
                else if (totalDays < 151) "May ${totalDays - 119}"
                else "Jun ${totalDays - 150}"
            }
            else -> "Day ${i + 1}"
        }
        
        val closeVal = pExpr
        val openVal = prevPrice
        val rangeVariance = basePrice * volatility * 0.4f
        val highVal = maxOf(openVal, closeVal) + random.nextFloat() * rangeVariance
        val lowVal = minOf(openVal, closeVal) - random.nextFloat() * rangeVariance

        points.add(
            HistoricalChartPoint(
                label = label,
                price = pExpr,
                open = openVal,
                high = highVal,
                low = lowVal,
                close = closeVal,
                volume = finalVolume,
                isUp = isUp,
                sma50 = sma50,
                sma200 = sma200,
                macd = macdVal,
                signal = signalVal,
                histogram = histVal
            )
        )
    }
    return points
}

private fun calculateMiniRSI(prices: List<Float>): List<Float> {
    if (prices.size < 2) return List(prices.size) { 50f }
    val rsi = ArrayList<Float>()
    for (i in prices.indices) {
        if (i == 0) {
            rsi.add(50f)
            continue
        }
        var gains = 0f
        var losses = 0f
        var count = 0
        val start = (i - 3).coerceAtLeast(0)
        for (j in start until i) {
            val change = prices[j + 1] - prices[j]
            if (change > 0) {
                gains += change
            } else {
                losses += -change
            }
            count++
        }
        if (count == 0) {
            rsi.add(50f)
        } else {
            val avgGain = gains / count
            val avgLoss = losses / count
            if (avgLoss == 0f) {
                rsi.add(if (avgGain > 0f) 100f else 50f)
            } else {
                val rs = avgGain / avgLoss
                val value = 100f - (100f / (1f + rs))
                rsi.add(value)
            }
        }
    }
    return rsi
}

// --- Gauge score wheel ---
@Composable
fun ProGaugeScoreWheel(
    title: String,
    score: Int,
    color: Color,
    cardBg: Color,
    borderLight: Color,
    textMuted: Color,
    textPrimary: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(cardBg, RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, borderLight), RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Outer track circle
                    drawCircle(
                        color = Color(0xFFF3EDF7),
                        style = Stroke(width = 10f)
                    )
                    // Filled progress sweep
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = (score / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(width = 10f, cap = strokeCap)
                    )
                }
                Text(
                    text = "$score",
                    color = textPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
private val strokeCap = androidx.compose.ui.graphics.StrokeCap.Round

// --- Detailed Analysis View Sheet ---
@Composable
fun DetailedAnalysisView(
    analysis: StockAnalysisResult,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
    accentGreen: Color,
    accentRed: Color,
    cardBg: Color,
    borderLight: Color,
    textMuted: Color,
    textPrimary: Color,
    violetDeep: Color,
    lavenderPrimary: Color,
    lavenderSoft: Color,
    navBg: Color,
    viewModel: StockViewModel
) {
    var activeSubTab by remember { mutableStateOf(0) } // 0: Verdict, 1: Strategic Pros/Cons, 2: Valuation

    val currentPriceNumeric = remember(analysis.price) {
        analysis.price.replace("$", "").replace(",", "").toFloatOrNull() ?: 100f
    }
    var targetPriceInput by remember(analysis.symbol) { mutableStateOf(String.format("%.2f", currentPriceNumeric)) }
    var isAboveTrigger by remember { mutableStateOf(true) }

    val alertsFlow = remember(analysis.symbol) { viewModel.getAlertsForStock(analysis.symbol) }
    val stockAlerts by alertsFlow.collectAsState(initial = emptyList())

    val newsState by viewModel.newsState.collectAsState(initial = NewsUiState.Idle)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("detailed_analysis_sheet"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Simple Back Control Panel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(cardBg, CircleShape)
                        .border(1.dp, borderLight, CircleShape)
                        .size(40.dp)
                        .testTag("back_to_explorer_button")
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = violetDeep)
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFFAEB), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFFFCC00), RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = Color(0xFFC79200), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PRO REVIEW", color = Color(0xFFC79200), fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .background(cardBg, CircleShape)
                            .border(1.dp, borderLight, CircleShape)
                            .size(40.dp)
                            .testTag("detail_favorite_toggle")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarOutline,
                            contentDescription = "Watchlist toggle",
                            tint = if (isFavorite) Color(0xFFFFCC00) else violetDeep
                        )
                    }
                }
            }
        }

        // Ticker & Bio Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = analysis.symbol,
                                    color = textPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(if (analysis.isPositive) Color(0xFFE2F4E5) else Color(0xFFFBEEEE), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = analysis.recommendation.uppercase(),
                                        color = if (analysis.isPositive) accentGreen else accentRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = analysis.name,
                                color = textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        // Rightside visual price tag
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = analysis.price,
                                color = textPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (analysis.isPositive) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                    tint = if (analysis.isPositive) accentGreen else accentRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = analysis.changePercent,
                                    color = if (analysis.isPositive) accentGreen else accentRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = analysis.summary, color = textPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = borderLight)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Sector: ${analysis.sector}", color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Region: ${analysis.region}", color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Custom canvas stock line chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StockPerformanceChart(
                    prices = analysis.chartPrices,
                    support = analysis.supportLevel,
                    resistance = analysis.resistanceLevel,
                    isPositive = analysis.isPositive,
                    accentGreen = accentGreen,
                    accentRed = accentRed,
                    textMuted = textMuted,
                    symbol = analysis.symbol,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Target Price Alerts Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Alerts Icon",
                                tint = violetDeep,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pro Price Alert Rules",
                                color = textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(lavenderSoft, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("DATABASE SYNC", color = violetDeep, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Current stock price tracking indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3EDF7), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Price Target", color = textMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(analysis.price, color = textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                        
                        // Automatically recommend target price (+5% default)
                        val autoPriceDefault = (currentPriceNumeric * 1.05f)
                        TextButton(
                            onClick = {
                                targetPriceInput = String.format("%.2f", autoPriceDefault)
                                isAboveTrigger = true
                            },
                            modifier = Modifier.testTag("alert_recommend_btn")
                        ) {
                            Icon(Icons.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Set +5% Peak", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Price Input & Trigger Select Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Numeric price text field
                        OutlinedTextField(
                            value = targetPriceInput,
                            onValueChange = { newVal ->
                                if (newVal.all { it.isDigit() || it == '.' }) {
                                    targetPriceInput = newVal
                                }
                            },
                            label = { Text("Alert Price ($)", fontSize = 11.sp) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6750A4),
                                unfocusedBorderColor = borderLight
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(56.dp)
                                .testTag("alert_price_input")
                        )

                        // Condition segment buttons
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .height(56.dp)
                                .background(Color(0xFFF3EDF7), RoundedCornerShape(8.dp))
                                .border(1.dp, borderLight, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // "Above" Button
                                Card(
                                    onClick = { isAboveTrigger = true },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isAboveTrigger) Color.White else Color.Transparent
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = if (isAboveTrigger) 1.dp else 0.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .testTag("alert_above_btn")
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            "Above ↗",
                                            fontSize = 11.sp,
                                            fontWeight = if (isAboveTrigger) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (isAboveTrigger) Color(0xFF6750A4) else textMuted
                                        )
                                    }
                                }

                                // "Below" Button
                                Card(
                                    onClick = { isAboveTrigger = false },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (!isAboveTrigger) Color.White else Color.Transparent
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = if (!isAboveTrigger) 1.dp else 0.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .testTag("alert_below_btn")
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            "Below ↘",
                                            fontSize = 11.sp,
                                            fontWeight = if (!isAboveTrigger) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (!isAboveTrigger) Color(0xFF6750A4) else textMuted
                                        )
                                    }
                                }
                            }
                        }

                        // Set Alert Submit Button
                        Button(
                            onClick = {
                                val enteredVal = targetPriceInput.toFloatOrNull()
                                if (enteredVal != null && enteredVal > 0f) {
                                    viewModel.addAlert(analysis.symbol, enteredVal, isAboveTrigger)
                                    targetPriceInput = String.format("%.2f", currentPriceNumeric)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(56.dp)
                                .testTag("set_alert_button")
                        ) {
                            Text("Set Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Active alerts list header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Set Price Target Alerts (${stockAlerts.size})",
                            color = textMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (stockAlerts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(Color(0xFFCAC4D0).copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No active alerts set for ${analysis.symbol}. Enter target price above to monitor.",
                                color = textMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            stockAlerts.forEach { alert ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, borderLight, RoundedCornerShape(8.dp))
                                        .background(if (alert.isActive) Color.White else Color(0xFFCAC4D0).copy(alpha = 0.1f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Notifications,
                                            contentDescription = null,
                                            tint = if (alert.isActive) (if (alert.isAbove) accentGreen else Color(0xFF2196F3)) else textMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = String.format("$%.2f", alert.targetPrice),
                                                color = if (alert.isActive) textPrimary else textMuted,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = if (alert.isAbove) "Triggers when price goes Above" else "Triggers when price falls Below",
                                                color = textMuted,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Active/Inactive toggle Switch
                                        Switch(
                                            checked = alert.isActive,
                                            onCheckedChange = { isChecked ->
                                                viewModel.toggleAlertActive(alert.id, isChecked)
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFF6750A4),
                                                uncheckedThumbColor = textMuted,
                                                uncheckedTrackColor = Color(0xFFCAC4D0).copy(alpha = 0.2f)
                                            ),
                                            modifier = Modifier
                                                .scale(0.8f)
                                                .testTag("alert_toggle_${alert.id}")
                                        )

                                        // Delete button
                                        IconButton(
                                            onClick = { viewModel.deleteAlert(alert.id) },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .testTag("alert_delete_${alert.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "Delete Alert",
                                                tint = accentRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Side-by-Side Scoring Gauge Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProGaugeScoreWheel(
                    title = "TECHNICAL INDICATOR",
                    score = analysis.technicalScore,
                    color = if (analysis.technicalScore >= 70) accentGreen else if (analysis.technicalScore >= 45) Color(0xFFFF9500) else accentRed,
                    cardBg = cardBg,
                    borderLight = borderLight,
                    textMuted = textMuted,
                    textPrimary = textPrimary,
                    modifier = Modifier.weight(1f)
                )
                ProGaugeScoreWheel(
                    title = "FUNDAMENTAL HEALTH",
                    score = analysis.fundamentalScore,
                    color = if (analysis.fundamentalScore >= 70) accentGreen else if (analysis.fundamentalScore >= 45) Color(0xFFFF9500) else accentRed,
                    cardBg = cardBg,
                    borderLight = borderLight,
                    textMuted = textMuted,
                    textPrimary = textPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Technical Indicator Status Grid Drawer
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Technical Dashboard", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Box(
                            modifier = Modifier
                                .background(lavenderSoft, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("1D INTERVAL", color = violetDeep, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        analysis.technicalIndicators.forEach { ind ->
                            val indicatorColor = when (ind.signal.uppercase()) {
                                "BULLISH" -> accentGreen
                                "BEARISH" -> accentRed
                                else -> Color(0xFFFF9500)
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(navBg, RoundedCornerShape(8.dp))
                                    .border(BorderStroke(1.dp, borderLight), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(text = ind.name, color = textMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = ind.value, color = textPrimary, fontWeight = FontWeight.Black, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = ind.signal, color = indicatorColor, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Analysis Tabs Switcher Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(navBg)
                    ) {
                        val subTabs = listOf("Research Verdicts", "Pros & Risks", "Valuation / Target", "Real-Time News")
                        subTabs.forEachIndexed { index, title ->
                            val isSelected = activeSubTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { activeSubTab = index }
                                    .drawBehind {
                                        if (isSelected) {
                                            drawRect(
                                                color = lavenderPrimary,
                                                topLeft = Offset(0f, size.height - 8f),
                                                size = Size(size.width, 8f)
                                            )
                                        }
                                    }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    color = if (isSelected) violetDeep else textMuted,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        when (activeSubTab) {
                            0 -> { // Verdicts with espectacular High-Density Royal Purple Card
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.QueryStats, contentDescription = null, tint = lavenderPrimary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("TECHNICAL ANALYSIS REPORT", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = analysis.technicalText, color = textMuted, fontSize = 13.sp, lineHeight = 18.sp)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // ROYAL DEEP VIOLET LUXURY CARD Matches Design HTML for practical report
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(violetDeep)
                                            .padding(16.dp)
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .background(Color(0xFFD0BCFF), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("AI", color = violetDeep, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "PRACTICAL ANALYSIS REPORT",
                                                    color = Color(0xFFEADDFF),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    letterSpacing = 1.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = analysis.practicalText,
                                                color = Color(0xFFEADDFF),
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp
                                            )
                                            Spacer(modifier = Modifier.height(14.dp))
                                            HorizontalDivider(color = Color(0x22EADDFF))
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "DB: GLOBAL_V4.2.1-LIVE",
                                                    color = Color(0x66EADDFF),
                                                    fontSize = 9.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF34A853), CircleShape))
                                                    Text("REAL-TIME DATA", color = Color(0xFFEADDFF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> { // Pros/Cons Lists
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Strategic Pro Outlook", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    analysis.pros.forEach { pro ->
                                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = accentGreen, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = pro, color = textMuted, fontSize = 13.sp, lineHeight = 17.sp)
                                        }
                                    }

                                    Divider(color = borderLight)

                                    Text("Corporate Risks & Downsides", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    analysis.cons.forEach { con ->
                                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                                            Icon(Icons.Filled.Error, contentDescription = null, tint = accentRed, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = con, color = textMuted, fontSize = 13.sp, lineHeight = 17.sp)
                                        }
                                    }
                                }
                            }
                            2 -> { // Valuation Consensus
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color(0xFFE2F4E5), RoundedCornerShape(8.dp))
                                            .border(BorderStroke(1.dp, Color(0xFFC6EAD0)), RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("1-YEAR TARGET", color = accentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = analysis.targetPrice,
                                            color = accentGreen,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 20.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(navBg, RoundedCornerShape(8.dp))
                                            .border(BorderStroke(1.dp, borderLight), RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("VALUATION TICKET", color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = analysis.valuation,
                                            color = textPrimary,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(lavenderSoft, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CompassCalibration, contentDescription = null, tint = violetDeep, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Based on consensus, professionals label this stock as a clear '${analysis.recommendation}' target.",
                                            color = violetDeep,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            3 -> {
                                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                                when (val news = newsState) {
                                    is NewsUiState.Idle -> {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("No news loaded. Click below to load.", color = textMuted, fontSize = 13.sp)
                                        }
                                    }
                                    is NewsUiState.Loading -> {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = lavenderPrimary,
                                                modifier = Modifier.size(36.dp),
                                                strokeWidth = 3.dp
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Searching Google & grounding sources...",
                                                color = textMuted,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    is NewsUiState.Error -> {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Warning,
                                                contentDescription = null,
                                                tint = accentRed,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = news.message,
                                                color = textPrimary,
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Button(
                                                onClick = { viewModel.fetchNews(analysis.symbol) },
                                                colors = ButtonDefaults.buttonColors(containerColor = violetDeep),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Retry Google Search", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                    is NewsUiState.Success -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Newspaper,
                                                        contentDescription = null,
                                                        tint = lavenderPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "GROUNDED GOOGLE HEADLINES",
                                                        color = textPrimary,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 11.sp,
                                                        letterSpacing = 0.8.sp
                                                    )
                                                }
                                                // Real-time badge
                                                Box(
                                                    modifier = Modifier
                                                        .background(accentGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Box(modifier = Modifier.size(6.dp).background(accentGreen, CircleShape))
                                                        Text("LIVE SEARCH", color = accentGreen, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                    }
                                                }
                                            }

                                            news.news.articles.forEach { article ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = navBg.copy(alpha = 0.3f)),
                                                    border = BorderStroke(1.dp, borderLight.copy(alpha = 0.5f)),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            article.url?.let {
                                                                try {
                                                                    uriHandler.openUri(it)
                                                                } catch (e: Exception) {
                                                                    // Handle gracefully
                                                                }
                                                            }
                                                        }
                                                        .testTag("news_article_card")
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = article.source,
                                                                color = lavenderPrimary,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                letterSpacing = 0.5.sp,
                                                                fontFamily = FontFamily.Monospace
                                                            )
                                                            Text(
                                                                text = article.date,
                                                                color = textMuted,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = article.title,
                                                            color = textPrimary,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            lineHeight = 17.sp
                                                        )
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = article.summary,
                                                            color = textMuted,
                                                            fontSize = 11.sp,
                                                            lineHeight = 15.sp
                                                        )
                                                        
                                                        article.url?.let { link ->
                                                            Spacer(modifier = Modifier.height(10.dp))
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.End,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                                    modifier = Modifier
                                                                        .background(violetDeep.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Filled.Launch,
                                                                        contentDescription = "Read source",
                                                                        tint = violetDeep,
                                                                        modifier = Modifier.size(10.dp)
                                                                    )
                                                                    Text(
                                                                        text = "SOURCE LINK",
                                                                        color = violetDeep,
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Black
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Error display view ---
@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    accentRed: Color,
    cardBg: Color,
    borderLight: Color,
    textMuted: Color,
    textPrimary: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = "Error Connection",
            tint = accentRed,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "EXCHANGES DISCONNECTED",
            color = textPrimary,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = textMuted,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary),
                border = BorderStroke(1.dp, borderLight)
            ) {
                Text("Back to Explorer", fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentRed)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retry", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
