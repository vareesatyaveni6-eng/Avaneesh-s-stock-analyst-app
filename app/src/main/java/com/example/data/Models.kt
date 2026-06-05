package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- Gemini API Request Models ---

@JsonClass(generateAdapter = true)
data class GeminiTool(
    @Json(name = "googleSearchRetrieval") val googleSearchRetrieval: GoogleSearchRetrieval? = null
)

@JsonClass(generateAdapter = true)
class GoogleSearchRetrieval

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "tools") val tools: List<GeminiTool>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

// --- Gemini API Response Models ---

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)


// --- Structured Stock Pro Analysis ---

@JsonClass(generateAdapter = true)
data class StockAnalysisResult(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "name") val name: String,
    @Json(name = "price") val price: String,
    @Json(name = "change") val change: String,
    @Json(name = "changePercent") val changePercent: String,
    @Json(name = "isPositive") val isPositive: Boolean,
    @Json(name = "sector") val sector: String,
    @Json(name = "region") val region: String,
    @Json(name = "summary") val summary: String,
    
    // Scores out of 100
    @Json(name = "technicalScore") val technicalScore: Int,
    @Json(name = "fundamentalScore") val fundamentalScore: Int,
    
    // Detailed Lists
    @Json(name = "technicalIndicators") val technicalIndicators: List<IndicatorState>,
    @Json(name = "technicalText") val technicalText: String,
    
    @Json(name = "practicalText") val practicalText: String,
    @Json(name = "pros") val pros: List<String>,
    @Json(name = "cons") val cons: List<String>,
    
    @Json(name = "valuation") val valuation: String,
    @Json(name = "targetPrice") val targetPrice: String,
    @Json(name = "recommendation") val recommendation: String, // "Buy", "Hold", "Sell", "Strong Buy", etc.
    
    // A simulated 7-day historic trend data points for chart drawing
    @Json(name = "chartPrices") val chartPrices: List<Float>,
    
    // Support and resistance indicators for professional trader analysis
    @Json(name = "supportLevel") val supportLevel: String,
    @Json(name = "resistanceLevel") val resistanceLevel: String
)

@JsonClass(generateAdapter = true)
data class IndicatorState(
    @Json(name = "name") val name: String,         // e.g., "RSI (14)", "MACD Histogram", "SMA (50/200)"
    @Json(name = "value") val value: String,       // e.g., "64.2", "Bullish Crossover", "Golden Cross"
    @Json(name = "signal") val signal: String      // "Bullish", "Bearish", "Neutral"
)

@JsonClass(generateAdapter = true)
data class NewsFeedResponse(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "articles") val articles: List<NewsArticle>
)

@JsonClass(generateAdapter = true)
data class NewsArticle(
    @Json(name = "title") val title: String,
    @Json(name = "source") val source: String,
    @Json(name = "date") val date: String,
    @Json(name = "summary") val summary: String,
    @Json(name = "url") val url: String? = null
)
