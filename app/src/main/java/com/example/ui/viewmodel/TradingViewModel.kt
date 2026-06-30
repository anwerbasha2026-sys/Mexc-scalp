package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.SignalEntity
import com.example.data.engine.IndicatorResult
import com.example.data.api.GeminiEvaluationResult
import com.example.data.repository.TradeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Loading : ScanUiState
    data class Success(val indicators: IndicatorResult, val evaluation: GeminiEvaluationResult) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class TradingViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = TradeRepository(database.signalDao())

    // Observables from Room database
    val allSignals: StateFlow<List<SignalEntity>> = repository.allSignals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeSignals: StateFlow<List<SignalEntity>> = repository.activeSignals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Scalping Settings (Default settings strictly satisfying the user prompt)
    var timeframe by mutableStateOf("15m")
    var confirmationTimeframe by mutableStateOf("5m")
    var stopLossPct by mutableStateOf(0.8) // 0.8% default
    var takeProfitPct by mutableStateOf(2.0) // 2% default
    var maxActiveTrades by mutableStateOf(3) // 3 active trades max
    var riskPct by mutableStateOf(1.0) // 1% risk per trade default

    // Simulator Settings
    var isMockMode by mutableStateOf(false) // Toggle between direct REST Gemini API and Simulator fallback
    var selectedSymbol by mutableStateOf("BTCUSDT")
    var customSymbolInput by mutableStateOf("")

    // Scan Results
    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    // Last scanned indicators & evaluation details
    var lastIndicatorResult by mutableStateOf<IndicatorResult?>(null)
    var lastEvaluationResult by mutableStateOf<GeminiEvaluationResult?>(null)

    init {
        // Pre-populate with a demo active signal on first run if database is empty,
        // so the user sees something beautiful instantly!
        viewModelScope.launch {
            repository.allSignals.collect { list ->
                if (list.isEmpty()) {
                    createInitialDemoSignal()
                }
            }
        }
    }

    fun startScan(symbol: String, forceHighConviction: Boolean = false) {
        viewModelScope.launch {
            _scanState.value = ScanUiState.Loading
            try {
                val cleanSymbol = symbol.trim().uppercase()
                val result = repository.scanAndEvaluate(
                    symbol = cleanSymbol,
                    timeframe = timeframe,
                    forceHighConviction = forceHighConviction,
                    isMockMode = isMockMode
                )
                lastIndicatorResult = result.first
                lastEvaluationResult = result.second
                _scanState.value = ScanUiState.Success(result.first, result.second)
            } catch (e: Exception) {
                _scanState.value = ScanUiState.Error(e.message ?: "حدث خطأ غير معروف أثناء التحليل")
            }
        }
    }

    fun simulateTradeOutcome(signalId: Int, status: String) {
        viewModelScope.launch {
            repository.simulateOutcome(signalId, status)
        }
    }

    fun deleteSignal(signal: SignalEntity) {
        viewModelScope.launch {
            repository.deleteSignal(signal)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllSignals()
        }
    }

    fun updateSettings(
        tf: String,
        confTf: String,
        sl: Double,
        tp: Double,
        maxTrades: Int,
        risk: Double
    ) {
        timeframe = tf
        confirmationTimeframe = confTf
        stopLossPct = sl
        takeProfitPct = tp
        maxActiveTrades = maxTrades
        riskPct = risk
    }

    private suspend fun createInitialDemoSignal() {
        val demo = SignalEntity(
            symbol = "BTCUSDT",
            direction = "BUY",
            entryPrice = 65240.50,
            stopLoss = 64718.57, // 0.8%
            takeProfit = 66545.31, // 2%
            score = 94, // High conviction (>90)
            reasoning = "إشارة عالية الدقة تم توليدها تلقائياً للمحاكاة والعرض. السعر يظهر قوة شرائية هائلة فوق مستويات الدعم الفنية EMA50 و EMA200 مع صعود مؤشر القوة النسبية RSI وزيادة تدفق السيولة الحيتانية وحجم التداولات.",
            trendScore = 95,
            volumeScore = 92,
            momentumScore = 94,
            liquidityScore = 91,
            volatilityScore = 90,
            orderBookScore = 96,
            whaleActivityScore = 98,
            status = "ACTIVE"
        )
        repository.insertSignal(demo)
    }
}
