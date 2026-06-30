package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.AnnotatedString
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.SignalEntity
import com.example.data.engine.IndicatorResult
import com.example.data.api.GeminiEvaluationResult
import com.example.ui.viewmodel.ScanUiState
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.*

// Colors matching High Density theme
val DeepBlack = Color(0xFF1A1C1E)
val SlateGray = Color(0xFF2D3033)
val CardBackground = Color(0xFF2D3033)
val NeonGreen = Color(0xFFC4EFD0)
val NeonRed = Color(0xFFBA1A1A)
val GlowGold = Color(0xFFD1E4FF)
val TextLight = Color(0xFFE2E2E6)
val TextMuted = Color(0xFFBEC6DC)
val HighDensityPrimary = Color(0xFF00497D)
val HighDensityOnPrimaryContainer = Color(0xFF003258)
val HighDensityBorder = Color(0xFF3F474E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTradingScreen(viewModel: TradingViewModel) {
    val allSignals by viewModel.allSignals.collectAsStateWithLifecycle()
    val activeSignals by viewModel.activeSignals.collectAsStateWithLifecycle()
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Scanner, 2: AI Weights, 3: Settings
    var showForceHighConviction by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "MEXC CONNECTED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowGold,
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Scalper Pro",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "AI ENGINE v2",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(NeonGreen, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CardBackground, CircleShape)
                                .clickable { activeTab = 3 },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = TextLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBlack,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DeepBlack,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("لوحة التحكم", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GlowGold,
                        selectedTextColor = GlowGold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = HighDensityPrimary
                    ),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Scanner") },
                    label = { Text("المسح والتحليل", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GlowGold,
                        selectedTextColor = GlowGold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = HighDensityPrimary
                    ),
                    modifier = Modifier.testTag("nav_tab_scanner")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.List, contentDescription = "AI Weights") },
                    label = { Text("AI Score", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GlowGold,
                        selectedTextColor = GlowGold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = HighDensityPrimary
                    ),
                    modifier = Modifier.testTag("nav_tab_ai_weights")
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("الإعدادات", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GlowGold,
                        selectedTextColor = GlowGold,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = HighDensityPrimary
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        },
        containerColor = DeepBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(DeepBlack)
        ) {
            // Summary Banner containing currently configured parameters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateGray)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الفريم الحالي: ${viewModel.timeframe} | تأكيد: ${viewModel.confirmationTimeframe}",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "الصفقات النشطة: ${activeSignals.size}/${viewModel.maxActiveTrades}",
                    color = if (activeSignals.size >= viewModel.maxActiveTrades) NeonRed else NeonGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "TabContent"
            ) { targetTab ->
                when (targetTab) {
                    0 -> DashboardTab(
                        viewModel = viewModel,
                        activeSignals = activeSignals,
                        allSignals = allSignals
                    )
                    1 -> ScannerTab(
                        viewModel = viewModel,
                        scanState = scanState,
                        showForceHighConviction = showForceHighConviction,
                        onForceHighConvictionChange = { showForceHighConviction = it }
                    )
                    2 -> AiWeightsTab()
                    3 -> SettingsTab(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun DashboardTab(
    viewModel: TradingViewModel,
    activeSignals: List<SignalEntity>,
    allSignals: List<SignalEntity>
) {
    val closedSignals = allSignals.filter { it.status != "ACTIVE" }
    
    // Performance Calculations
    val winCount = closedSignals.count { it.status == "TP_HIT" }
    val lossCount = closedSignals.count { it.status == "SL_HIT" }
    val totalClosed = closedSignals.size
    val winRate = if (totalClosed > 0) (winCount.toDouble() / totalClosed * 100).toInt() else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Stats Panel
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlateGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("معدل النجاح", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("$winRate%", color = NeonGreen, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp),
                        color = CardBackground
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الصفقات المغلقة", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("$totalClosed", color = TextLight, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp),
                        color = CardBackground
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الربح (TP)", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("$winCount", color = NeonGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp),
                        color = CardBackground
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الخسارة (SL)", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("$lossCount", color = NeonRed, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section Title: Active Signals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الإشارات النشطة (${activeSignals.size})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (activeSignals.size >= viewModel.maxActiveTrades) {
                    Text(
                        text = "الحد الأقصى نشط ⚠️",
                        color = GlowGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (activeSignals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBackground, RoundedCornerShape(12.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "No active trades",
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد صفقات سكالبينج نشطة حالياً.",
                            color = TextLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "انتقل إلى مسح السوق لتوليد وتحليل صفقات تفوق 90/100.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(activeSignals, key = { it.id }) { signal ->
                ActiveSignalCard(signal = signal, viewModel = viewModel)
            }
        }

        // Section Title: Completed Signals History
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجل الإشارات السابقة",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (closedSignals.isNotEmpty()) {
                    Text(
                        text = "مسح الكل",
                        color = NeonRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .testTag("clear_history_button")
                            .clickable { viewModel.clearAllHistory() }
                    )
                }
            }
        }

        if (closedSignals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "السجل فارغ. صفقات المحاكاة المكتملة ستظهر هنا.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(closedSignals, key = { it.id }) { signal ->
                ClosedSignalCard(signal = signal, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ActiveSignalCard(signal: SignalEntity, viewModel: TradingViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val isHighScore = signal.score >= 90

    val cardBg = if (isHighScore) Color(0xFFD1E4FF) else Color(0xFF2D3033)
    val contentColor = if (isHighScore) Color(0xFF003258) else Color(0xFFE2E2E6)
    val mutedColor = if (isHighScore) Color(0xFF00497D).copy(alpha = 0.8f) else Color(0xFFBEC6DC)
    val factorCardBg = if (isHighScore) Color(0xFF00497D).copy(alpha = 0.1f) else Color(0xFF1A1C1E)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isHighScore) Color(0xFF00497D).copy(alpha = 0.3f) else Color(0xFF3F474E),
                RoundedCornerShape(24.dp)
            )
            .testTag("active_signal_card_${signal.symbol}")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (isHighScore) Color(0xFF00497D) else (if (signal.direction == "BUY") NeonGreen.copy(alpha = 0.15f) else NeonRed.copy(alpha = 0.15f)),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (signal.direction == "BUY") "LONG" else "SHORT",
                            color = if (isHighScore) Color.White else (if (signal.direction == "BUY") NeonGreen else NeonRed),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = signal.symbol,
                        fontWeight = FontWeight.Black,
                        color = contentColor,
                        fontSize = 18.sp
                    )
                }

                // AI Score Badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(if (isHighScore) Color.White else Color(0xFF1A1C1E), CircleShape)
                        .border(1.5.dp, if (isHighScore) Color(0xFF003258) else GlowGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = signal.score.toString(),
                            fontWeight = FontWeight.Black,
                            color = if (isHighScore) Color(0xFF003258) else GlowGold,
                            fontSize = 15.sp,
                            lineHeight = 15.sp
                        )
                        Text(
                            text = "AI",
                            fontSize = 8.sp,
                            color = if (isHighScore) Color(0xFF003258).copy(alpha = 0.7f) else GlowGold.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtext: Entry price range
            Text(
                text = "نطاق الدخول: ${String.format("%.4f", signal.entryPrice)}",
                color = mutedColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Trading Parameters Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // TP Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(factorCardBg, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Text("جني الأرباح (TP)", color = mutedColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%.4f", signal.takeProfit),
                        color = if (isHighScore) Color(0xFF00497D) else NeonGreen,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                // SL Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isHighScore) Color(0xFFBA1A1A).copy(alpha = 0.1f) else factorCardBg, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Text("وقف الخسارة (SL)", color = mutedColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%.4f", signal.stopLoss),
                        color = NeonRed,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                // Risk Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(factorCardBg, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Text("المخاطرة", color = mutedColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "1.0%",
                        color = contentColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expandable Technical Report Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .background(factorCardBg, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "التقرير الفني وعوامل معايرة AI",
                    color = contentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = signal.reasoning,
                        color = contentColor.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Right
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "الأوزان وعوامل التقييم المعتمدة:",
                        color = if (isHighScore) Color(0xFF003258) else GlowGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.background(factorCardBg, RoundedCornerShape(8.dp)).padding(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الاتجاه (20%): ${signal.trendScore}/100", color = mutedColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("الحجم (20%): ${signal.volumeScore}/100", color = mutedColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الزخم (15%): ${signal.momentumScore}/100", color = mutedColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("السيولة (15%): ${signal.liquidityScore}/100", color = mutedColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("التقلب (10%): ${signal.volatilityScore}/100", color = mutedColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("الأوامر (10%): ${signal.orderBookScore}/100", color = mutedColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("الحيتان (10%): ${signal.whaleActivityScore}/100", color = mutedColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Execute Button for high score signals
            if (isHighScore) {
                Button(
                    onClick = { viewModel.simulateTradeOutcome(signal.id, "TP_HIT") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00497D), contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Execute", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تنفيذ فوري على MEXC", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Trade Simulation Actions (To test the Room database & status updates)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.simulateTradeOutcome(signal.id, "TP_HIT") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHighScore) Color(0xFF003258).copy(alpha = 0.1f) else NeonGreen.copy(alpha = 0.15f),
                        contentColor = if (isHighScore) Color(0xFF003258) else NeonGreen
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("tp_hit_button_${signal.id}"),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("جني الأرباح ✔️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.simulateTradeOutcome(signal.id, "SL_HIT") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHighScore) Color(0xFFBA1A1A).copy(alpha = 0.15f) else NeonRed.copy(alpha = 0.15f),
                        contentColor = NeonRed
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("sl_hit_button_${signal.id}"),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("وقف الخسارة 🛑", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.simulateTradeOutcome(signal.id, "CLOSED") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHighScore) Color(0xFF00497D).copy(alpha = 0.15f) else Color(0xFF1A1C1E),
                        contentColor = contentColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إغلاق يدوي", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ClosedSignalCard(signal: SignalEntity, viewModel: TradingViewModel) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SlateGray),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBackground, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (signal.status) {
                        "TP_HIT" -> Icons.Default.CheckCircle
                        "SL_HIT" -> Icons.Default.Close
                        else -> Icons.Default.Warning
                    },
                    contentDescription = "Status icon",
                    tint = when (signal.status) {
                        "TP_HIT" -> NeonGreen
                        "SL_HIT" -> NeonRed
                        else -> TextMuted
                    },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = signal.symbol,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (signal.direction == "BUY") "LONG / شراء" else "SHORT / بيع",
                        color = if (signal.direction == "BUY") NeonGreen else NeonRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = when (signal.status) {
                        "TP_HIT" -> "أصاب الهدف (+2.0%)"
                        "SL_HIT" -> "ضرب الوقف (-0.8%)"
                        else -> "مغلق يدوياً"
                    },
                    color = when (signal.status) {
                        "TP_HIT" -> NeonGreen
                        "SL_HIT" -> NeonRed
                        else -> TextLight
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "AI Score: ${signal.score}/100",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            IconButton(
                onClick = { viewModel.deleteSignal(signal) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextMuted.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ScannerTab(
    viewModel: TradingViewModel,
    scanState: ScanUiState,
    showForceHighConviction: Boolean,
    onForceHighConvictionChange: (Boolean) -> Unit
) {
    val popularCoins = listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "XRPUSDT", "BNBUSDT", "ADAUSDT", "DOGEUSDT", "DOTUSDT")
    val keyboardController = LocalSoftwareKeyboardController.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selection card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlateGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "اختر العملة المراد فحصها وتحليلها:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Popular Coins Grid (FlowRow simulation)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val chunks = popularCoins.chunked(4)
                        for (chunk in chunks) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (coin in chunk) {
                                    val isSelected = viewModel.selectedSymbol == coin && viewModel.customSymbolInput.isEmpty()
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) NeonGreen.copy(alpha = 0.2f) else CardBackground,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) NeonGreen else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable {
                                                viewModel.selectedSymbol = coin
                                                viewModel.customSymbolInput = ""
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = coin.substringBefore("USDT"),
                                            color = if (isSelected) NeonGreen else TextLight,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom input
                    OutlinedTextField(
                        value = viewModel.customSymbolInput,
                        onValueChange = {
                            viewModel.customSymbolInput = it
                        },
                        label = { Text("أو أدخل رمزاً مخصصاً (مثال: LINKUSDT)", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = CardBackground,
                            focusedLabelColor = NeonGreen,
                            unfocusedLabelColor = TextMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_symbol_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                                val target = if (viewModel.customSymbolInput.isNotEmpty()) viewModel.customSymbolInput else viewModel.selectedSymbol
                                viewModel.startScan(target, forceHighConviction = showForceHighConviction)
                            }
                        )
                    )
                }
            }
        }

        // Scanner Engine Preferences
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlateGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("تفضيلات محاكاة الفحص السريع:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("توليد إشارة تفوق 90/100 تلقائياً", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("تعديل مؤشرات لتأكيد الدخول الفوري وصنع إشارة نشطة", color = TextMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = showForceHighConviction,
                            onCheckedChange = onForceHighConvictionChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonGreen,
                                checkedTrackColor = NeonGreen.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = CardBackground
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = CardBackground)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("استخدام محاكي الذكاء الاصطناعي المحلي", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("محاكاة سريعة ومجانية دون الحاجة لمفتاح Gemini API", color = TextMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = viewModel.isMockMode,
                            onCheckedChange = { viewModel.isMockMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GlowGold,
                                checkedTrackColor = GlowGold.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = CardBackground
                            )
                        )
                    }
                }
            }
        }

        // Scan button
        item {
            val symbolToScan = if (viewModel.customSymbolInput.isNotEmpty()) viewModel.customSymbolInput else viewModel.selectedSymbol
            Button(
                onClick = {
                    keyboardController?.hide()
                    viewModel.startScan(symbolToScan, forceHighConviction = showForceHighConviction)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("scan_button"),
                colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Scan", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "بدء الفحص والتحليل الفني لـ ${symbolToScan.uppercase()}",
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        // Live States Display
        item {
            when (scanState) {
                is ScanUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = NeonGreen)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "جاري قراءة البيانات من MEXC وحساب المؤشرات الـ 9...",
                                color = TextLight,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "تحليل ومعايرة AI Score بواسطة Gemini...",
                                color = TextMuted,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                is ScanUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NeonRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .background(NeonRed.copy(alpha = 0.05f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "حدث خطأ: ${(scanState as ScanUiState.Error).message}",
                            color = NeonRed,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                is ScanUiState.Success -> {
                    ScanResultLayout(
                        indicators = (scanState as ScanUiState.Success).indicators,
                        evaluation = (scanState as ScanUiState.Success).evaluation,
                        viewModel = viewModel
                    )
                }
                is ScanUiState.Idle -> {
                    // Show previous results if available
                    val lastInd = viewModel.lastIndicatorResult
                    val lastEval = viewModel.lastEvaluationResult
                    if (lastInd != null && lastEval != null) {
                        ScanResultLayout(indicators = lastInd, evaluation = lastEval, viewModel = viewModel)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CardBackground, RoundedCornerShape(12.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "اضغط على الزر في الأعلى لتشغيل المحرك وحساب جميع المؤشرات.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScanResultLayout(
    indicators: IndicatorResult,
    evaluation: GeminiEvaluationResult,
    viewModel: TradingViewModel
) {
    val isFiltered = evaluation.score <= 90

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // AI Score Badge Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateGray),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (isFiltered) TextMuted.copy(alpha = 0.3f) else GlowGold,
                    RoundedCornerShape(12.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "تقييم الذكاء الاصطناعي الإجمالي (AI Score)",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (isFiltered) TextMuted.copy(alpha = 0.05f) else GlowGold.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .border(3.dp, if (isFiltered) TextMuted else GlowGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${evaluation.score}/100",
                        fontWeight = FontWeight.Black,
                        color = if (isFiltered) TextLight else GlowGold,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isFiltered) "تم تصفية الإشارة ⚠️" else "إشارة شراء/بيع عالية الدقة 🚀",
                    color = if (isFiltered) NeonRed else NeonGreen,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isFiltered) {
                        "التقييم أقل من 90. نظام السكالبينج يتجاهل الصفقات منخفضة الدقة لحماية رأس مالك."
                    } else {
                        "تجاوز التقييم عتبة الـ 90! تم حفظ الإشارة بنجاح ومتاحة الآن للمحاكاة في لوحة التحكم."
                    },
                    color = TextLight,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // Live Technical Indicators Calculation Grid
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "حسابات محرك المؤشرات الـ 9 الفوري:",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Table of Indicators
                IndicatorRow("سعر السوق الحالي", String.format("%.4f", indicators.currentPrice), TextLight)
                Divider(color = CardBackground)
                IndicatorRow("EMA 20", String.format("%.4f", indicators.ema20), if (indicators.currentPrice > indicators.ema20) NeonGreen else NeonRed)
                Divider(color = CardBackground)
                IndicatorRow("EMA 50", String.format("%.4f", indicators.ema50), if (indicators.currentPrice > indicators.ema50) NeonGreen else NeonRed)
                Divider(color = CardBackground)
                IndicatorRow("EMA 200", String.format("%.4f", indicators.ema200), if (indicators.currentPrice > indicators.ema200) NeonGreen else NeonRed)
                Divider(color = CardBackground)
                IndicatorRow("RSI (14)", "${String.format("%.2f", indicators.rsi)} (${if (indicators.rsi > 70) "تشبع شراء" else if (indicators.rsi < 30) "تشبع بيع" else "متوازن"})", if (indicators.rsi in 30.0..70.0) NeonGreen else GlowGold)
                Divider(color = CardBackground)
                IndicatorRow("ATR (متوسط المدى الحقيقي)", String.format("%.4f", indicators.atr), TextLight)
                Divider(color = CardBackground)
                IndicatorRow("MACD Line / Signal / Hist", "${String.format("%.2f", indicators.macdLine)} / ${String.format("%.2f", indicators.signalLine)} / ${String.format("%.2f", indicators.macdHist)}", if (indicators.macdHist >= 0) NeonGreen else NeonRed)
                Divider(color = CardBackground)
                IndicatorRow("VWAP (متوسط السعر بحجم التداول)", String.format("%.4f", indicators.vwap), if (indicators.currentPrice > indicators.vwap) NeonGreen else NeonRed)
                Divider(color = CardBackground)
                IndicatorRow("SuperTrend (10, 3)", "${String.format("%.4f", indicators.superTrendValue)} (${if (indicators.superTrendDirection == 1) "صاعد 🟢" else "هابط 🔴"})", if (indicators.superTrendDirection == 1) NeonGreen else NeonRed)
                Divider(color = CardBackground)
                IndicatorRow("Bollinger Bands (20, 2)", "علو: ${String.format("%.2f", indicators.bbUpper)}\nوسط: ${String.format("%.2f", indicators.bbMiddle)}\nسفل: ${String.format("%.2f", indicators.bbLower)}", TextLight)
                Divider(color = CardBackground)
                IndicatorRow("Volume Profile POC (نقطة التحكم)", String.format("%.4f", indicators.volumeProfilePOC), GlowGold)
            }
        }

        // Detailed Arabic AI Reasoning Report
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = "Report", tint = GlowGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "التقرير والتحليل الفني الذكي:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = evaluation.reasoning,
                    color = TextLight,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Right
                )
            }
        }
    }
}

@Composable
fun IndicatorRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = valueColor, fontSize = 11.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Left)
    }
}

@Composable
fun AiWeightsTab() {
    val factors = listOf(
        Triple("الاتجاه (Trend)", 20, "تحليل موضع السعر نسبةً للمتوسطات المتحركة EMA20, EMA50, EMA200 ومؤشر SuperTrend لتأكيد المسار الصاعد أو الهابط."),
        Triple("حجم التداول (Volume)", 20, "مقارنة أحجام التداول الحالية بالمتوسط للتأكد من وجود زخم حقيقي يدعم اختراق السعر للقمم أو القيعان."),
        Triple("الزخم (Momentum)", 15, "مراقبة تباين وتطابق حركة السعر مع مؤشرات الزخم الفائقة RSI و MACD وتوقع الانعكاسات الصعودية والتشبعات."),
        Triple("السيولة (Liquidity)", 15, "تحديد أحجام السيولة الموزعة في دفتر الطلبات وتحليل Volume Profile POC لضمان سرعة تنفيذ الأوامر بأقل انزلاق."),
        Triple("التقلب السعري (Volatility)", 10, "احتساب متوسط المدى الحقيقي ATR وتحديد اتساع Bollinger Bands لحساب حجم المخاطرة وضبط وقف الخسارة الصارم."),
        Triple("دفتر الطلبات (Order Book)", 10, "مراقبة عمق السوق والطلبات المعلقة لحساب قوى العرض والطلب وحسم المعركة اللحظية بين الدببة والثيران."),
        Triple("نشاط المحافظ الضخمة (Whale Activity)", 10, "رصد ومحاكاة الطلبات الكبيرة المفاجئة وجدران الشراء/البيع لضمان الركوب الآمن مع اتجاه الحيتان المالي الكبار.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlateGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "نظام تقييم الصفقات الصارم (AI Score System)",
                        color = GlowGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لضمان أعلى معدل نجاح لصفقات السكالبينج (Scalping) السريعة، تم إعداد ميزان تقييم رقمي يزن كل عامل بدقة. الإشارة تُعرض وتُقترح للتنفيذ فقط إذا تجاوز المجموع عتبة الـ 90/100.",
                        color = TextLight,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        items(factors) { (title, weight, desc) ->
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = SlateGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "الوزن: $weight%", color = GlowGold, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Linear progress bar visual
                    LinearProgressIndicator(
                        progress = { weight.toFloat() / 20f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = GlowGold,
                        trackColor = CardBackground
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = desc, color = TextMuted, fontSize = 10.sp, lineHeight = 14.sp, textAlign = TextAlign.Right)
                }
            }
        }
    }
}

@Composable
fun SettingsTab(viewModel: TradingViewModel) {
    val context = LocalContext.current
    var editTimeframe by remember { mutableStateOf(viewModel.timeframe) }
    var editConf by remember { mutableStateOf(viewModel.confirmationTimeframe) }
    var editSl by remember { mutableStateOf(viewModel.stopLossPct.toString()) }
    var editTp by remember { mutableStateOf(viewModel.takeProfitPct.toString()) }
    var editMaxTrades by remember { mutableStateOf(viewModel.maxActiveTrades.toString()) }
    var editRisk by remember { mutableStateOf(viewModel.riskPct.toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlateGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "إعدادات السكالبينج الافتراضية:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Timeframe selection
                    Text("الفريم الأساسي للتحليل (الافتراضي 15 دقيقة)", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("5m", "15m", "30m", "1h").forEach { tf ->
                            val isSelected = editTimeframe == tf
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) NeonGreen.copy(alpha = 0.2f) else CardBackground,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(1.dp, if (isSelected) NeonGreen else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable { editTimeframe = tf }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (tf) {
                                        "5m" -> "5 د"
                                        "15m" -> "15 د"
                                        "30m" -> "30 د"
                                        "1h" -> "1 س"
                                        else -> tf
                                    },
                                    color = if (isSelected) NeonGreen else TextLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirmation Timeframe
                    Text("فريم التأكيد والفلترة (الافتراضي 5 دقائق)", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("1m", "5m", "15m").forEach { tf ->
                            val isSelected = editConf == tf
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) NeonGreen.copy(alpha = 0.2f) else CardBackground,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(1.dp, if (isSelected) NeonGreen else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable { editConf = tf }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (tf) {
                                        "1m" -> "1 د"
                                        "5m" -> "5 د"
                                        "15m" -> "15 د"
                                        else -> tf
                                    },
                                    color = if (isSelected) NeonGreen else TextLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Numeric values inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = editSl,
                            onValueChange = { editSl = it },
                            label = { Text("وقف الخسارة (%)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                focusedBorderColor = NeonGreen,
                                focusedLabelColor = NeonGreen,
                                unfocusedBorderColor = CardBackground
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = editTp,
                            onValueChange = { editTp = it },
                            label = { Text("جني الأرباح (%)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                focusedBorderColor = NeonGreen,
                                focusedLabelColor = NeonGreen,
                                unfocusedBorderColor = CardBackground
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = editMaxTrades,
                            onValueChange = { editMaxTrades = it },
                            label = { Text("أقصى عدد صفقات", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                focusedBorderColor = NeonGreen,
                                focusedLabelColor = NeonGreen,
                                unfocusedBorderColor = CardBackground
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = editRisk,
                            onValueChange = { editRisk = it },
                            label = { Text("المخاطرة (%)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                focusedBorderColor = NeonGreen,
                                focusedLabelColor = NeonGreen,
                                unfocusedBorderColor = CardBackground
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val sl = editSl.toDoubleOrNull() ?: 0.8
                                val tp = editTp.toDoubleOrNull() ?: 2.0
                                val maxTr = editMaxTrades.toIntOrNull() ?: 3
                                val rsk = editRisk.toDoubleOrNull() ?: 1.0

                                viewModel.updateSettings(editTimeframe, editConf, sl, tp, maxTr, rsk)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("حفظ الإعدادات الفنية", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                // Reset back to strict system defaults requested
                                editTimeframe = "15m"
                                editConf = "5m"
                                editSl = "0.8"
                                editTp = "2.0"
                                editMaxTrades = "3"
                                editRisk = "1.0"
                                viewModel.updateSettings("15m", "5m", 0.8, 2.0, 3, 1.0)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("الافتراضي 🔄", color = TextLight, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Info details card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlateGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("شرح ضوابط الإعدادات:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. الفريم 15 دقيقة: فريم العمل والبحث الأساسي عن الاتجاه الفرعي.\n" +
                               "2. تأكيد 5 دقائق: للتحقق من زخم الدخول وعدم الوقوع في تصحيحات عكسية.\n" +
                               "3. وقف الخسارة 0.8%: وقف ضيق وصارم لحماية وتأمين المحفظة.\n" +
                               "4. جني الأرباح 2.0%: الهدف الفني المستهدف في الحركة السريعة لصفقات السكالبينج.\n" +
                               "5. أقصى صفقات 3: للالتزام بإدارة المخاطر وتفادي التشتت في فتح صفقات زائدة.\n" +
                               "6. المخاطرة 1%: لضمان عدم خسارة أكثر من 1% من رأس المال الإجمالي في كل صفقة.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Right
                    )
                }
            }
        }
    }
}
