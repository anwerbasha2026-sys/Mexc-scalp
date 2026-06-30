package com.example.data.repository

import android.util.Log
import com.example.data.api.GeminiApiService
import com.example.data.api.GeminiEvaluationResult
import com.example.data.api.MexcApiService
import com.example.data.database.SignalDao
import com.example.data.database.SignalEntity
import com.example.data.engine.IndicatorEngine
import com.example.data.engine.IndicatorResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TradeRepository(private val signalDao: SignalDao) {
    private val TAG = "TradeRepository"

    val allSignals: Flow<List<SignalEntity>> = signalDao.getAllSignals()
    val activeSignals: Flow<List<SignalEntity>> = signalDao.getActiveSignals()

    suspend fun insertSignal(signal: SignalEntity): Long {
        return signalDao.insertSignal(signal)
    }

    suspend fun updateSignal(signal: SignalEntity) {
        signalDao.updateSignal(signal)
    }

    suspend fun deleteSignal(signal: SignalEntity) {
        signalDao.deleteSignal(signal)
    }

    suspend fun clearAllSignals() {
        signalDao.clearAllSignals()
    }

    /**
     * Executes the complete indicator-calculation and AI-evaluation pipeline.
     * Returns the full evaluation results and inserts into Room ONLY if score exceeds 90.
     */
    suspend fun scanAndEvaluate(
        symbol: String,
        timeframe: String = "15m",
        forceHighConviction: Boolean = false, // If true, adjusts values to ensure score > 90 (great for testing!)
        isMockMode: Boolean = false
    ): Pair<IndicatorResult, GeminiEvaluationResult> {
        Log.d(TAG, "Scanning symbol $symbol with timeframe $timeframe...")
        
        // 1. Fetch real candles from MEXC (or simulated fallback)
        val klines = MexcApiService.fetchKLines(symbol, interval = timeframe)
        
        // 2. Calculate actual mathematical indicators
        val indicators = IndicatorEngine.calculate(symbol, klines)
        
        // 3. Evaluate using Gemini (or simulated fallback)
        var evaluation = GeminiApiService.evaluateTrade(indicators, isMockMode = isMockMode)

        // Force high conviction score (>90) for demo/testing if requested
        if (forceHighConviction && evaluation.score <= 90) {
            val adjustedTrend = maxOf(evaluation.trendScore, 92)
            val adjustedVolume = maxOf(evaluation.volumeScore, 91)
            val adjustedMomentum = maxOf(evaluation.momentumScore, 93)
            val adjustedLiquidity = maxOf(evaluation.liquidityScore, 90)
            val adjustedVolatility = maxOf(evaluation.volatilityScore, 92)
            val adjustedOrderBook = maxOf(evaluation.orderBookScore, 94)
            val adjustedWhale = maxOf(evaluation.whaleActivityScore, 95)

            // Re-calculate weighted score:
            val weightedScore = (adjustedTrend * 0.20) + 
                                (adjustedVolume * 0.20) + 
                                (adjustedMomentum * 0.15) + 
                                (adjustedLiquidity * 0.15) + 
                                (adjustedVolatility * 0.10) + 
                                (adjustedOrderBook * 0.10) + 
                                (adjustedWhale * 0.10)

            val roundedScore = weightedScore.toInt()
            
            val updatedReasoning = "🎯 [إشارة عالية الدقة - للتجربة والمحاكاة]\n" + evaluation.reasoning

            evaluation = evaluation.copy(
                score = roundedScore,
                trendScore = adjustedTrend,
                volumeScore = adjustedVolume,
                momentumScore = adjustedMomentum,
                liquidityScore = adjustedLiquidity,
                volatilityScore = adjustedVolatility,
                orderBookScore = adjustedOrderBook,
                whaleActivityScore = adjustedWhale,
                reasoning = updatedReasoning
            )
        }

        // 4. Save to Room database ONLY if evaluation score > 90
        if (evaluation.score > 90) {
            // Check active signals limit: "Max active trades: 3"
            val activeList = activeSignals.first()
            if (activeList.size < 3) {
                val entity = SignalEntity(
                    symbol = symbol.uppercase().trim(),
                    direction = evaluation.direction,
                    entryPrice = evaluation.entryPrice,
                    stopLoss = evaluation.stopLoss,
                    takeProfit = evaluation.takeProfit,
                    score = evaluation.score,
                    reasoning = evaluation.reasoning,
                    status = "ACTIVE",
                    trendScore = evaluation.trendScore,
                    volumeScore = evaluation.volumeScore,
                    momentumScore = evaluation.momentumScore,
                    liquidityScore = evaluation.liquidityScore,
                    volatilityScore = evaluation.volatilityScore,
                    orderBookScore = evaluation.orderBookScore,
                    whaleActivityScore = evaluation.whaleActivityScore
                )
                insertSignal(entity)
                Log.d(TAG, "High conviction signal generated! Saved to Room database.")
            } else {
                Log.w(TAG, "Signal score is > 90, but active trades count limit (3) is reached.")
            }
        } else {
            Log.d(TAG, "Signal score (${evaluation.score}) is not > 90. Filtered out according to system configuration.")
        }

        return Pair(indicators, evaluation)
    }

    /**
     * Simulates the outcome of an active trade (Take Profit or Stop Loss hit or closed manually).
     */
    suspend fun simulateOutcome(signalId: Int, outcome: String) {
        // Query signal by ID is done by filtering from the list
        val signals = allSignals.first()
        val signal = signals.find { it.id == signalId }
        if (signal != null) {
            val updated = signal.copy(status = outcome)
            updateSignal(updated)
        }
    }
}
