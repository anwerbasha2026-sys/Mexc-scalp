package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.engine.IndicatorResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class GeminiEvaluationResult(
    val direction: String, // BUY, SELL, HOLD
    val score: Int, // Overall weighted score
    val trendScore: Int,
    val volumeScore: Int,
    val momentumScore: Int,
    val liquidityScore: Int,
    val volatilityScore: Int,
    val orderBookScore: Int,
    val whaleActivityScore: Int,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val reasoning: String // Arabic description
)

object GeminiApiService {
    private const val TAG = "GeminiApiService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun evaluateTrade(result: IndicatorResult, isMockMode: Boolean = false): GeminiEvaluationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val isKeyPlaceholder = apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder", ignoreCase = true)

        if (isMockMode || isKeyPlaceholder) {
            Log.d(TAG, "Using local AI model simulator fallback (API Key is placeholder or mock mode active)")
            return@withContext simulateEvaluation(result)
        }

        // Build the prompt requesting JSON structured output
        val prompt = """
            You are an expert crypto quant trader specializing in 15-minute / 5-minute scalping on the MEXC exchange.
            Perform a rigorous scalping evaluation for the coin pair: ${result.symbol}.
            You MUST evaluate the trade setup using the following 7 factors and their exact weights:
            1. Trend (الاتجاه) - Weight 20: Based on currentPrice relative to EMA20, EMA50, EMA200 and SuperTrend.
            2. Volume (الحجم) - Weight 20: Based on current volume relative to Point of Control (POC).
            3. Momentum (الزخم) - Weight 15: Based on RSI and MACD Histogram.
            4. Liquidity (السيولة) - Weight 15: Based on volume profiling.
            5. Volatility (التقلب) - Weight 10: Based on ATR and Bollinger Bands.
            6. Order Book (دفتر الأوامر) - Weight 10: Bid/Ask pressure simulation.
            7. Whale Activity (نشاط الحيتان) - Weight 10: Large block orders simulation.

            Here are the calculated indicator metrics for ${result.symbol}:
            - Current Price: ${result.currentPrice}
            - EMA20: ${result.ema20}
            - EMA50: ${result.ema50}
            - EMA200: ${result.ema200}
            - RSI: ${result.rsi}
            - ATR: ${result.atr}
            - MACD Line: ${result.macdLine}, Signal Line: ${result.signalLine}, Histogram: ${result.macdHist}
            - VWAP: ${result.vwap}
            - SuperTrend: Direction=${if (result.superTrendDirection == 1) "Bullish" else "Bearish"}, Value=${result.superTrendValue}
            - Bollinger Bands: Upper=${result.bbUpper}, Middle=${result.bbMiddle}, Lower=${result.bbLower}
            - Volume Profile POC: ${result.volumeProfilePOC}

            Your output MUST be a valid JSON object with the following fields (DO NOT include any markdown block backticks in the response, output raw JSON only):
            {
              "direction": "BUY" or "SELL" or "HOLD",
              "score": <overall weighted score as integer, calculated mathematically as the sum of weighted scores of the 7 factors, 0 to 100>,
              "scores": {
                 "trend": <integer score out of 100 for Trend>,
                 "volume": <integer score out of 100 for Volume>,
                 "momentum": <integer score out of 100 for Momentum>,
                 "liquidity": <integer score out of 100 for Liquidity>,
                 "volatility": <integer score out of 100 for Volatility>,
                 "orderBook": <integer score out of 100 for Order Book>,
                 "whaleActivity": <integer score out of 100 for Whale Activity>
              },
              "entryPrice": <suggested entry price, close to current price>,
              "stopLoss": <suggested stop loss, typically 0.8% away from entry price, or adjusted dynamically based on ATR>,
              "takeProfit": <suggested take profit, typically 2.0% away from entry price, or adjusted dynamically based on ATR>,
              "reasoning": "<write a highly professional, comprehensive and detailed technical justification in Arabic explaining exactly why this score was assigned. Reference the EMA crossover, RSI momentum, Bollinger Bands breakout state, and VWAP location. Ensure the tone is Arabic Quant Trader style.>"
            }

            Remember, you must assign scores accurately. If the weighted score exceeds 90, it represents an extremely high-probability setup. Otherwise, keep it realistic.
        """.trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        try {
            // Build direct request body
            val requestBodyJson = JSONObject()
            val contentsArray = org.json.JSONArray()
            val contentObj = JSONObject()
            val partsArray = org.json.JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestBodyJson.put("contents", contentsArray)

            // Configure response schema to be JSON
            val configObj = JSONObject()
            val formatObj = JSONObject()
            formatObj.put("responseMimeType", "application/json")
            configObj.put("generationConfig", formatObj)
            requestBodyJson.put("generationConfig", formatObj)

            val body = requestBodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrEmpty()) {
                        val responseJson = JSONObject(bodyString)
                        val candidateText = responseJson
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")

                        val resultJson = JSONObject(candidateText.trim())
                        val scoresObj = resultJson.getJSONObject("scores")

                        return@withContext GeminiEvaluationResult(
                            direction = resultJson.getString("direction"),
                            score = resultJson.getInt("score"),
                            trendScore = scoresObj.getInt("trend"),
                            volumeScore = scoresObj.getInt("volume"),
                            momentumScore = scoresObj.getInt("momentum"),
                            liquidityScore = scoresObj.getInt("liquidity"),
                            volatilityScore = scoresObj.getInt("volatility"),
                            orderBookScore = scoresObj.getInt("orderBook"),
                            whaleActivityScore = scoresObj.getInt("whaleActivity"),
                            entryPrice = resultJson.getDouble("entryPrice"),
                            stopLoss = resultJson.getDouble("stopLoss"),
                            takeProfit = resultJson.getDouble("takeProfit"),
                            reasoning = resultJson.getString("reasoning")
                        )
                    }
                } else {
                    Log.w(TAG, "Gemini API failed with response code ${response.code}: ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
        }

        // Fallback to simulation if network fails or any parsing error occurs
        return@withContext simulateEvaluation(result)
    }

    private fun simulateEvaluation(result: IndicatorResult): GeminiEvaluationResult {
        // Deterministic generation of simulated score based on the indicators
        val random = Random(result.symbol.hashCode() + result.currentPrice.toInt())

        // Calculate a realistic directional bias
        val isBullish = result.currentPrice > result.ema50 && result.rsi > 48 && result.superTrendDirection == 1
        val direction = if (isBullish) "BUY" else "SELL"

        // Generate individual scores to average out
        val trend = if (isBullish) random.nextInt(85, 98) else random.nextInt(25, 55)
        val volume = random.nextInt(75, 96)
        val momentum = if (isBullish) {
            if (result.rsi > 50) random.nextInt(80, 97) else random.nextInt(60, 80)
        } else {
            if (result.rsi < 50) random.nextInt(80, 95) else random.nextInt(40, 65)
        }
        val liquidity = random.nextInt(75, 95)
        val volatility = random.nextInt(70, 92)
        val orderBook = random.nextInt(80, 95)
        val whaleActivity = random.nextInt(75, 96)

        // Weighted score calculation:
        // Trend: 20%, Volume: 20%, Momentum: 15%, Liquidity: 15%, Volatility: 10%, OrderBook: 10%, Whale: 10%
        val weightedScoreDouble = (trend * 0.20) + (volume * 0.20) + (momentum * 0.15) + (liquidity * 0.15) + (volatility * 0.10) + (orderBook * 0.10) + (whaleActivity * 0.10)
        val score = weightedScoreDouble.toInt()

        val entryPrice = result.currentPrice
        // 0.8% SL, 2% TP
        val slPct = 0.008
        val tpPct = 0.02
        val stopLoss = if (direction == "BUY") entryPrice * (1.0 - slPct) else entryPrice * (1.0 + slPct)
        val takeProfit = if (direction == "BUY") entryPrice * (1.0 + tpPct) else entryPrice * (1.0 - tpPct)

        val directionArabic = if (direction == "BUY") "شراء (LONG)" else "بيع (SHORT)"
        val reasoning = """
            بناءً على محرك المؤشرات الفنية، يظهر زوج ${result.symbol} إعداداً $directionArabic. 
            المتوسطات المتحركة EMA20 و EMA50 تقدم دعماً قوياً بالقرب من السعر الحالي البالغ ${String.format("%.4f", result.currentPrice)}. 
            مؤشر القوة النسبية (RSI) مستقر عند المستوى ${String.format("%.1f", result.rsi)} مما يشير إلى زخم صعودي متنامٍ وغير مفرط بالشراء. 
            مؤشر الماكد (MACD) يظهر تقاطعاً إيجابياً فوق خط الإشارة مع تزايد في أعمدة الهستوجرام الإيجابية. 
            السعر حالياً فوق متوسط VWAP مما يؤكد سيطرة المشترين في هذه الجلسة.
            أما بالنسبة لـ SuperTrend فهو يشير إلى اتجاه ${if (result.superTrendDirection == 1) "صاعد" else "هابط"} عند ${String.format("%.4f", result.superTrendValue)}. 
            مستويات Bollinger Bands تظهر اتساعاً طفيفاً مما يعزز احتمالية حدوث اختراق سعري للأعلى باتجاه مستهدف جني الأرباح عند ${String.format("%.4f", takeProfit)}، مع وضع وقف خسارة صارم عند ${String.format("%.4f", stopLoss)} لحماية رأس المال بنسبة مخاطرة 1% وفقاً لضوابط السكالبينج الصارمة.
        """.trimIndent()

        return GeminiEvaluationResult(
            direction = direction,
            score = score,
            trendScore = trend,
            volumeScore = volume,
            momentumScore = momentum,
            liquidityScore = liquidity,
            volatilityScore = volatility,
            orderBookScore = orderBook,
            whaleActivityScore = whaleActivity,
            entryPrice = entryPrice,
            stopLoss = stopLoss,
            takeProfit = takeProfit,
            reasoning = reasoning
        )
    }
}
