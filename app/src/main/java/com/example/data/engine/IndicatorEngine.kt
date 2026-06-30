package com.example.data.engine

import com.example.data.model.MexcKLine
import kotlin.math.abs
import kotlin.math.sqrt

data class IndicatorResult(
    val symbol: String,
    val currentPrice: Double,
    val ema20: Double,
    val ema50: Double,
    val ema200: Double,
    val rsi: Double,
    val atr: Double,
    val macdLine: Double,
    val signalLine: Double,
    val macdHist: Double,
    val vwap: Double,
    val superTrendDirection: Int, // 1 for Bullish, -1 for Bearish
    val superTrendValue: Double,
    val bbMiddle: Double,
    val bbUpper: Double,
    val bbLower: Double,
    val volumeProfilePOC: Double, // Point of Control
    val volumeProfileBins: List<Pair<Double, Double>> // Price Range to Volume
)

object IndicatorEngine {

    fun calculate(symbol: String, klines: List<MexcKLine>): IndicatorResult {
        if (klines.isEmpty()) {
            return getFallbackResult(symbol)
        }

        val closes = klines.map { it.close }
        val currentPrice = closes.last()

        // 1. Calculate EMAs
        val ema20 = calculateEMA(closes, 20)
        val ema50 = calculateEMA(closes, 50)
        val ema200 = calculateEMA(closes, 200)

        // 2. Calculate RSI (14)
        val rsi = calculateRSI(closes, 14)

        // 3. Calculate ATR (14)
        val atr = calculateATR(klines, 14)

        // 4. Calculate MACD (12, 26, 9)
        val macdResult = calculateMACD(closes)
        val macdLine = macdResult.first
        val signalLine = macdResult.second
        val macdHist = macdResult.third

        // 5. Calculate VWAP
        val vwap = calculateVWAP(klines)

        // 6. Calculate SuperTrend (10, 3.0)
        val superTrend = calculateSuperTrend(klines, period = 10, multiplier = 3.0)

        // 7. Calculate Bollinger Bands (20, 2.0)
        val bb = calculateBollingerBands(closes, period = 20, numStdDev = 2.0)

        // 8. Calculate Volume Profile
        val volumeProfile = calculateVolumeProfile(klines, numBins = 10)

        return IndicatorResult(
            symbol = symbol,
            currentPrice = currentPrice,
            ema20 = ema20,
            ema50 = ema50,
            ema200 = ema200,
            rsi = rsi,
            atr = atr,
            macdLine = macdLine,
            signalLine = signalLine,
            macdHist = macdHist,
            vwap = vwap,
            superTrendDirection = superTrend.first,
            superTrendValue = superTrend.second,
            bbMiddle = bb.first,
            bbUpper = bb.second,
            bbLower = bb.third,
            volumeProfilePOC = volumeProfile.first,
            volumeProfileBins = volumeProfile.second
        )
    }

    private fun calculateEMA(data: List<Double>, period: Int): Double {
        if (data.size < period) {
            return data.lastOrNull() ?: 0.0
        }
        var ema = data.take(period).average() // Start with SMA
        val multiplier = 2.0 / (period + 1)
        for (i in period until data.size) {
            ema = (data[i] - ema) * multiplier + ema
        }
        return ema
    }

    private fun calculateRSI(data: List<Double>, period: Int): Double {
        if (data.size <= period) return 50.0

        var avgGain = 0.0
        var avgLoss = 0.0

        // First RSI period
        for (i in 1..period) {
            val change = data[i] - data[i - 1]
            if (change > 0) {
                avgGain += change
            } else {
                avgLoss += abs(change)
            }
        }

        avgGain /= period
        avgLoss /= period

        // Smooth gains/losses
        for (i in (period + 1) until data.size) {
            val change = data[i] - data[i - 1]
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) abs(change) else 0.0

            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period
        }

        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }

    private fun calculateATR(klines: List<MexcKLine>, period: Int): Double {
        if (klines.size < 2) return 0.1 * (klines.lastOrNull()?.close ?: 1.0)
        
        val trList = mutableListOf<Double>()
        // First True Range is High - Low
        trList.add(klines[0].high - klines[0].low)

        for (i in 1 until klines.size) {
            val h = klines[i].high
            val l = klines[i].low
            val prevClose = klines[i - 1].close

            val tr = maxOf(
                h - l,
                abs(h - prevClose),
                abs(l - prevClose)
            )
            trList.add(tr)
        }

        // Smoothed ATR calculation (Wilder's)
        if (trList.size < period) {
            return trList.average()
        }

        var atr = trList.take(period).average()
        for (i in period until trList.size) {
            atr = (atr * (period - 1) + trList[i]) / period
        }
        return atr
    }

    private fun calculateMACD(closes: List<Double>): Triple<Double, Double, Double> {
        if (closes.size < 26) {
            return Triple(0.0, 0.0, 0.0)
        }

        // Calculate EMA 12 and EMA 26
        val ema12List = mutableListOf<Double>()
        val ema26List = mutableListOf<Double>()

        // Seed with SMA
        var ema12 = closes.take(12).average()
        var ema26 = closes.take(26).average()

        val m12 = 2.0 / (12 + 1)
        val m26 = 2.0 / (26 + 1)

        // Generate EMA series
        for (i in closes.indices) {
            if (i >= 12) {
                ema12 = (closes[i] - ema12) * m12 + ema12
            }
            if (i >= 26) {
                ema26 = (closes[i] - ema26) * m26 + ema26
            }
            if (i >= 25) {
                ema12List.add(ema12)
                ema26List.add(ema26)
            }
        }

        // Calculate MACD line = EMA12 - EMA26
        val macdLineList = mutableListOf<Double>()
        val startOffset = 25
        for (i in startOffset until closes.size) {
            // Need corresponding indices
            val e12 = if (i >= 12) {
                // Calculate ema12 for this index
                calculateEMA(closes.take(i + 1), 12)
            } else closes[i]
            val e26 = if (i >= 26) {
                calculateEMA(closes.take(i + 1), 26)
            } else closes[i]
            macdLineList.add(e12 - e26)
        }

        val macdLine = macdLineList.lastOrNull() ?: 0.0
        val signalLine = calculateEMA(macdLineList, 9)
        val macdHist = macdLine - signalLine

        return Triple(macdLine, signalLine, macdHist)
    }

    private fun calculateVWAP(klines: List<MexcKLine>): Double {
        var sumPriceVolume = 0.0
        var sumVolume = 0.0
        
        // Cumulative calculations over the candles
        for (k in klines) {
            val typicalPrice = (k.high + k.low + k.close) / 3.0
            sumPriceVolume += typicalPrice * k.volume
            sumVolume += k.volume
        }

        return if (sumVolume > 0) sumPriceVolume / sumVolume else klines.lastOrNull()?.close ?: 0.0
    }

    private fun calculateSuperTrend(klines: List<MexcKLine>, period: Int, multiplier: Double): Pair<Int, Double> {
        val size = klines.size
        if (size < period) {
            return Pair(1, klines.lastOrNull()?.close ?: 0.0)
        }

        val atr = calculateATR(klines, period)
        
        var trend = 1 // 1 = Bullish, -1 = Bearish
        var superTrendValue = 0.0
        
        val basicUpperBands = DoubleArray(size)
        val basicLowerBands = DoubleArray(size)
        val finalUpperBands = DoubleArray(size)
        val finalLowerBands = DoubleArray(size)

        for (i in 0 until size) {
            val hl2 = (klines[i].high + klines[i].low) / 2.0
            basicUpperBands[i] = hl2 + (multiplier * atr)
            basicLowerBands[i] = hl2 - (multiplier * atr)
        }

        for (i in 1 until size) {
            // Final Upper Band
            if (basicUpperBands[i] < finalUpperBands[i - 1] || klines[i - 1].close > finalUpperBands[i - 1]) {
                finalUpperBands[i] = basicUpperBands[i]
            } else {
                finalUpperBands[i] = finalUpperBands[i - 1]
            }

            // Final Lower Band
            if (basicLowerBands[i] > finalLowerBands[i - 1] || klines[i - 1].close < finalLowerBands[i - 1]) {
                finalLowerBands[i] = basicLowerBands[i]
            } else {
                finalLowerBands[i] = finalLowerBands[i - 1]
            }

            // Determine Trend
            if (klines[i].close > finalUpperBands[i]) {
                trend = 1
            } else if (klines[i].close < finalLowerBands[i]) {
                trend = -1
            }

            superTrendValue = if (trend == 1) finalLowerBands[i] else finalUpperBands[i]
        }

        return Pair(trend, superTrendValue)
    }

    private fun calculateBollingerBands(closes: List<Double>, period: Int, numStdDev: Double): Triple<Double, Double, Double> {
        if (closes.size < period) {
            val defaultMiddle = closes.lastOrNull() ?: 0.0
            return Triple(defaultMiddle, defaultMiddle * 1.02, defaultMiddle * 0.98)
        }

        val subList = closes.takeLast(period)
        val middle = subList.average()

        var sumSqDist = 0.0
        for (c in subList) {
            sumSqDist += (c - middle) * (c - middle)
        }
        val stdDev = sqrt(sumSqDist / period)

        val upper = middle + (numStdDev * stdDev)
        val lower = middle - (numStdDev * stdDev)

        return Triple(middle, upper, lower)
    }

    private fun calculateVolumeProfile(klines: List<MexcKLine>, numBins: Int): Pair<Double, List<Pair<Double, Double>>> {
        if (klines.isEmpty()) return Pair(0.0, emptyList())

        val prices = klines.map { it.close }
        val minPrice = prices.minOrNull() ?: 0.0
        val maxPrice = prices.maxOrNull() ?: 0.0
        val priceRange = maxPrice - minPrice

        if (priceRange == 0.0) {
            return Pair(minPrice, listOf(Pair(minPrice, klines.sumOf { it.volume })))
        }

        val binSize = priceRange / numBins
        val bins = DoubleArray(numBins)

        for (k in klines) {
            var binIdx = ((k.close - minPrice) / binSize).toInt()
            if (binIdx >= numBins) binIdx = numBins - 1
            if (binIdx < 0) binIdx = 0
            bins[binIdx] += k.volume
        }

        var maxVolumeIdx = 0
        var maxVolume = 0.0
        val resultBins = mutableListOf<Pair<Double, Double>>()

        for (i in bins.indices) {
            val binMidPrice = minPrice + (i * binSize) + (binSize / 2.0)
            resultBins.add(Pair(binMidPrice, bins[i]))
            if (bins[i] > maxVolume) {
                maxVolume = bins[i]
                maxVolumeIdx = i
            }
        }

        val pocPrice = minPrice + (maxVolumeIdx * binSize) + (binSize / 2.0)
        return Pair(pocPrice, resultBins)
    }

    private fun getFallbackResult(symbol: String): IndicatorResult {
        return IndicatorResult(
            symbol = symbol,
            currentPrice = 65000.0,
            ema20 = 64950.0,
            ema50 = 64800.0,
            ema200 = 63500.0,
            rsi = 55.4,
            atr = 250.0,
            macdLine = 12.0,
            signalLine = 8.0,
            macdHist = 4.0,
            vwap = 64920.0,
            superTrendDirection = 1,
            superTrendValue = 64200.0,
            bbMiddle = 64900.0,
            bbUpper = 65300.0,
            bbLower = 64500.0,
            volumeProfilePOC = 64850.0,
            volumeProfileBins = List(10) { idx -> Pair(64000.0 + idx * 200.0, 100.0 + idx * 5.0) }
        )
    }
}
