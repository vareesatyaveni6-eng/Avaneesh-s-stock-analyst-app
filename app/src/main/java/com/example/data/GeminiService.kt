package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiNetworkClient {
    private const val TAG = "GeminiNetworkClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun getProStockAnalysis(symbol: String, companyName: String? = null): StockAnalysisResult? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is empty or placeholder! Verify in AI Studio Secrets.")
            return null
        }

        val prompt = """
            Perform a pro technical and practical analysis of the stock with ticker '$symbol' ${if (companyName != null) "($companyName)" else ""}.
            
            You must return a valid, strictly formatted JSON matching this exact structure:
            {
              "symbol": "$symbol",
              "name": "Full Company Name (e.g. Apple Inc.)",
              "price": "Current stock price in USD format, or local currency if applicable (e.g., $180.50)",
              "change": "Numeric change with sign (e.g., +2.45)",
              "changePercent": "Percent change with sign and label (e.g., +1.38%)",
              "isPositive": true (if price increased, otherwise false),
              "sector": "Sector (e.g., Technology)",
              "region": "Corporate region/market (e.g., US, India, Europe, Global)",
              "summary": "1-2 sentence pro market overview summary.",
              "technicalScore": numeric score 0-100 indicating technical bullishness (e.g., 78),
              "fundamentalScore": numeric score 0-100 indicating fundamental strength (e.g., 85),
              "technicalIndicators": [
                { "name": "RSI (14)", "value": "e.g., 62.1", "signal": "Bullish, Bearish, or Neutral" },
                { "name": "MACD", "value": "e.g., Bullish Crossover", "signal": "Bullish" },
                { "name": "SMA (50/200)", "value": "e.g., Above 50-day SMA", "signal": "Bullish" }
              ],
              "technicalText": "Detailed professional analysis of technical patterns, support, resistance, moving averages, and volumes. Approximately 2-3 dense sentences.",
              "practicalText": "Practical & fundamental outlook on business model, market placement, customer metrics, balance sheet status, and future product pipelines. Approximately 2-3 dense sentences.",
              "pros": [
                "Detailed positive bullet point 1",
                "Detailed positive bullet point 2"
              ],
              "cons": [
                "Risk/downside bullet point 1",
                "Risk/downside bullet point 2"
              ],
              "valuation": "Commentary on current valuation (e.g., Fairly Valued, Premium valuation, etc.)",
              "targetPrice": "Analyst 1-year consensus price target (e.g., ${'$'}205.00)",
              "recommendation": "Overall consensus advice (e.g., Outperform, Hold, Buy)",
              "chartPrices": [6-7 numeric float values showing recent 7-day trend history concluding with the current price, to render on an interactive screen chart. Must be floats like: [175.2, 177.1, 178.5, 176.4, 179.2, 180.5]] (avoid zero values, ensure smooth trend),
              "supportLevel": "Technical support price level (e.g., ${'$'}174.00)",
              "resistanceLevel": "Technical resistance price level (e.g., ${'$'}185.20)"
            }

            STRICTLY IMPORTANT RULES:
            - Do not include any HTML markdown wrappers like ```json or ``` in the response. Return ONLY the raw JSON string.
            - Provide highly realistic data based on your professional historical and real-time training up to 2026.
            - If are asked about a fictional, joke, or invalid ticker, return a JSON output where 'name' is 'Unknown stock ticker', and populate a friendly explanation in the 'summary' starting with "Invalid Stock: ..." so the UI can detect and show an error.
            - Double-check JSON validity, assure quotes are correctly matched, and avoid trailing commas.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        return try {
            val response = service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                Log.d(TAG, "Raw response: $jsonText")
                // Clean the response if Gemini included markdown wrappers despite instructions
                val cleanedJson = cleanJsonString(jsonText)
                val adapter = moshi.adapter(StockAnalysisResult::class.java)
                adapter.fromJson(cleanedJson)
            } else {
                Log.e(TAG, "Empty Gemini response body.")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Gemini stock analysis: ${e.message}", e)
            null
        }
    }

    suspend fun getRecentNewsForTicker(symbol: String, companyName: String? = null): NewsFeedResponse? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is empty or placeholder! Verify in AI Studio Secrets.")
            return null
        }

        val prompt = """
            Search for the latest, most recent (up to June 2026) financial and corporate news for the stock ticker '$symbol' ${if (companyName != null) "($companyName)" else ""}.
            You must use the Google Search tool to fetch highly accurate Real-Time headlines.
            
            Return a valid, strictly formatted JSON matching this exact structure:
            {
              "symbol": "$symbol",
              "articles": [
                {
                  "title": "Clean, informative headline of the news article",
                  "source": "News source publishing it (e.g., Bloomberg, Motley Fool, CNBC, Reuters, WSJ)",
                  "date": "Publication time (e.g., Jun 4, 2026, or 3 hours ago)",
                  "summary": "A 1-2 sentence descriptive summary of the article's core financial elements",
                  "url": "Associated source URL or reference link"
                }
              ]
            }

            STRICTLY IMPORTANT RULES:
            - Do not include any HTML markdown wrappers like ```json or ``` in the response. Return ONLY the raw JSON string.
            - Focus only on legitimate financial or business topics regarding the requested asset.
            - Ensure there are at least 3-5 highly relevant news articles returned.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.4f
            ),
            tools = listOf(GeminiTool(googleSearchRetrieval = GoogleSearchRetrieval()))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                Log.d(TAG, "Raw News response: $jsonText")
                val cleanedJson = cleanJsonString(jsonText)
                val adapter = moshi.adapter(NewsFeedResponse::class.java)
                adapter.fromJson(cleanedJson)
            } else {
                Log.e(TAG, "Empty Gemini news response body.")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Gemini stock news: ${e.message}", e)
            null
        }
    }

    private fun cleanJsonString(rawJson: String): String {
        var str = rawJson.trim()
        if (str.startsWith("```")) {
            str = str.substringAfter("\n")
            if (str.endsWith("```")) {
                str = str.substringBeforeLast("```")
            }
        }
        return str.trim()
    }
}
