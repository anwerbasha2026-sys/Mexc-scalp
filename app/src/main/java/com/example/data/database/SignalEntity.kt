package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signals")
data class SignalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val direction: String, // BUY, SELL, HOLD
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val score: Int,
    val reasoning: String, // Arabic details
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE", // ACTIVE, TP_HIT, SL_HIT, CLOSED
    
    // Detailed AI Score parameters
    val trendScore: Int,
    val volumeScore: Int,
    val momentumScore: Int,
    val liquidityScore: Int,
    val volatilityScore: Int,
    val orderBookScore: Int,
    val whaleActivityScore: Int
)
