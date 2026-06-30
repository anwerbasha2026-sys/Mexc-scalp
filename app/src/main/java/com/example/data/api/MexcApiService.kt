package com.example.data.api

import android.util.Log
import com.example.data.model.MexcKLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object MexcApiService {
    private const val TAG = "MexcApiService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Map common symbols to typical prices for simulation fallback
    private val symbolBasePrices = mapOf(
        "BTCUSDT" to 65000.0,
        "ETHUSDT" to 3450.0,
        "SOLUSDT" to 142.0,
        "XRPUSDT" to 0.52,
        "ADAUSDT" to 0.38,
        "DOGEUSDT" to 0.12,
        "BNBUSDT" to 580.0,
        "DOTUSDT" to 6.20
    )

    suspend fun fetchKLines(symbol: String, interval: String = "15m", limit: Int = 200): List<MexcKLine> = withContext(Dispatchers.IO) {
        val cleanSymbol = symbol.uppercase().trim()
        val url = "https://api.mexc.com/api/v3/klines?symbol=$cleanSymbol&interval=$interval&limit=$limit"

        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrEmpty()) {
                        val parsed = parseKLinesJson(bodyString)
                        if (parsed.isNotEmpty()) {
                            Log.d(TAG, "Successfully fetched ${parsed.size} real KLines from MEXC for $cleanSymbol ($interval)")
                            return@withContext parsed
                        }
                    }
                } else {
                    Log.w(TAG, "Failed MEXC API response code: ${response.code} for $cleanSymbol. Using fallback data.")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching KLines from MEXC: ${e.message}. Using fallback data.")
        }

        // Fallback simulated data
        return@withContext generateSimulatedKLines(cleanSymbol, limit)
    }

    private fun parseKLinesJson(jsonString: String): List<MexcKLine> {
        val list = mutableListOf<MexcKLine>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val candleArray = jsonArray.getJSONArray(i)
                // MEXC format:
                // [
                //   0: Open time (Long)
                //   1: Open (String)
                //   2: High (String)
                //   3: Low (String)
                //   4: Close (String)
                //   5: Volume (String)
                //   6: Close time (Long)
                //   ...
                // ]
                val openTime = candleArray.getLong(0)
                val open = candleArray.getString(1).toDouble()
                val high = candleArray.getString(2).toDouble()
                val low = candleArray.getString(3).toDouble()
                val close = candleArray.getString(4).toDouble()
                val volume = candleArray.getString(5).toDouble()

                list.add(
                    MexcKLine(
                        openTime = openTime,
                        open = open,
                        high = high,
                        low = low,
                        close = close,
                        volume = volume
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing KLines JSON: ${e.message}")
        }
        return list
    }

    fun generateSimulatedKLines(symbol: String, count: Int): List<MexcKLine> {
        val list = mutableListOf<MexcKLine>()
        val basePrice = symbolBasePrices[symbol] ?: 10.0
        val random = Random(symbol.hashCode()) // Deterministic based on symbol name
        
        var currentPrice = basePrice
        var currentTime = System.currentTimeMillis() - (count * 15 * 60 * 1000)

        for (i in 0 until count) {
            val pctChange = (random.nextDouble() - 0.49) * 0.015 // Slight upward/downward drift
            val open = currentPrice
            val close = currentPrice * (1.0 + pctChange)
            
            val spread = open * 0.005 * random.nextDouble()
            val high = maxOf(open, close) + spread
            val low = minOf(open, close) - spread
            val volume = 10000.0 * random.nextDouble() + 500.0

            list.add(
                MexcKLine(
                    openTime = currentTime,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = volume
                )
            )

            currentPrice = close
            currentTime += 15 * 60 * 1000 // Add 15 minutes
        }
        return list
    }
}
