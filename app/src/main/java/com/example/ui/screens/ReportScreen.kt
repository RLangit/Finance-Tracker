package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.data.TransactionType
import com.example.data.BudgetLimit
import com.example.ui.BudgetViewModel
import com.example.ui.theme.*
import java.util.*

enum class ReportTab { PEMASUKAN, PENGELUARAN, BUDGETING }

@Composable
fun ReportScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val allGoals by viewModel.allSavingsGoals.collectAsState()
    val metaItems by viewModel.metaItems.collectAsState()
    val budgetLimits by viewModel.budgetLimits.collectAsState()
    
    val currentBalance = remember(transactions) {
        val income = transactions.filter { it.type == TransactionType.PEMASUKAN }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.PENGELUARAN }.sumOf { it.amount }
        val taxes = transactions.sumOf { it.taxAmount }
        income - expense - taxes
    }

    var selectedTab by remember { mutableStateOf(ReportTab.PENGELUARAN) }
    var currentMonthOffset by remember { mutableStateOf(0) } // 0 = Current, -1 = Prev Month, etc.
    
    fun getDynamicColor(category: String): Color {
        val mapped = metaItems.find { it.name == category }?.colorHex
        if (mapped != null) {
            try {
                return Color(android.graphics.Color.parseColor(mapped))
            } catch (e: Exception) { }
        }
        return getCategoryColor(category)
    }

    // Calculate dates based on month offset
    val periodText = remember(currentMonthOffset) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, currentMonthOffset)
        
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startSdf = java.text.SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        val startStr = startSdf.format(calendar.time)

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endSdf = java.text.SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        val endStr = endSdf.format(calendar.time)

        "$startStr - $endStr"
    }

    // Filter transactions by the chosen month range
    val filteredTransactions = remember(transactions, currentMonthOffset) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, currentMonthOffset)
        
        val targetMonth = calendar.get(Calendar.MONTH)
        val targetYear = calendar.get(Calendar.YEAR)

        transactions.filter { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            txCal.get(Calendar.MONTH) == targetMonth && txCal.get(Calendar.YEAR) == targetYear
        }
    }

    // Calculate Monthly Metrics
    val monthlyPemasukan = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.PEMASUKAN && it.category != "Tabungan" }.sumOf { it.amount }
    }
    val monthlyPengeluaran = remember(filteredTransactions) {
        val expense = filteredTransactions.filter { it.type == TransactionType.PENGELUARAN && it.category != "Tabungan" }.sumOf { it.amount }
        val taxes = filteredTransactions.sumOf { it.taxAmount }
        expense + taxes
    }
    val totalCurrentTabungan = remember(allGoals) {
        allGoals.sumOf { it.currentAmount }
    }
    val selisihValue = monthlyPemasukan - monthlyPengeluaran

    // Calculate breakdown categories logic for the circular donut layout
    val categoryStats = remember(filteredTransactions, selectedTab) {
        if (selectedTab == ReportTab.BUDGETING) {
            emptyList<CategoryReportItem>() // Not used for donut in budgeting
        } else {
            val selectedType = if (selectedTab == ReportTab.PEMASUKAN) TransactionType.PEMASUKAN else TransactionType.PENGELUARAN
            val subset = filteredTransactions.filter { it.type == selectedType && it.category != "Tabungan" }
            val totalAmount = subset.sumOf { it.amount + it.taxAmount }

            if (totalAmount == 0.0) emptyList()
            else {
                subset.groupBy { it.category }
                    .map { (cat, list) ->
                        val sum = list.sumOf { it.amount + it.taxAmount }
                        CategoryReportItem(
                            category = cat,
                            amount = sum,
                            percentage = (sum / totalAmount * 100),
                            count = list.size,
                            transactions = list
                        )
                    }
                    .sortedByDescending { it.amount }
            }
        }
    }

    var selectedDetailCategory by remember { mutableStateOf<CategoryReportItem?>(null) }

    // Modern list view layout
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GoPayDarkBlue)
    ) {
        // Safe content padding block for top
        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars))

        Text(
            text = "Rekap Keuangan",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            // Period switcher row (1 Mei 2026 - 31 Mei 2026)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoPayNavy)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { currentMonthOffset-- },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, "Prev Month", tint = Color.White)
                    }

                    Text(
                        text = periodText,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { currentMonthOffset++ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, "Next Month", tint = Color.White)
                    }
                }
            }

            // Summary Totals and Selisih Box Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = GoPayNavy),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(WondrEmeraldGreen))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pemasukan", color = Color.Gray, fontSize = 12.sp)
                                }
                                Text(
                                    text = "Rp ${String.format("%,.0f", monthlyPemasukan).replace(",", ".")}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(1.dp)
                                    .align(Alignment.CenterVertically),
                                color = OutlineVariant
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(GoPayBrightTeal))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tabungan", color = Color.Gray, fontSize = 12.sp)
                                }
                                Text(
                                    text = "Rp ${String.format("%,.0f", totalCurrentTabungan).replace(",", ".")}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(1.dp)
                                    .align(Alignment.CenterVertically),
                                color = OutlineVariant
                            )

                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(WondrCoralRed))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pengeluaran", color = Color.Gray, fontSize = 12.sp)
                                }
                                Text(
                                    text = "Rp ${String.format("%,.0f", monthlyPengeluaran).replace(",", ".")}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = OutlineVariant)

                        // Selisih calculation
                        Text("Selisih", color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = (if (selisihValue >= 0) "" else "-") + "Rp ${String.format("%,.0f", Math.abs(selisihValue)).replace(",", ".")}",
                            color = if (selisihValue >= 0) WondrEmeraldGreen else WondrCoralRed,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Arus Kas Chart (Moved from Homepage)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            com.example.ui.screens.DoubleBarChart(
                                income = monthlyPemasukan,
                                expense = monthlyPengeluaran
                            )
                        }
                    }
                }
            }

            // Segment tabs selector switches between Pemasukan/Pengeluaran/Budgeting
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(GoPayNavy)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedTab == ReportTab.PEMASUKAN) GoPayBrightTeal else Color.Transparent)
                            .clickable { selectedTab = ReportTab.PEMASUKAN }
                            .padding(vertical = 10.dp)
                            .testTag("report_tab_income"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pemasukan",
                            color = if (selectedTab == ReportTab.PEMASUKAN) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedTab == ReportTab.PENGELUARAN) GoPayBrightTeal else Color.Transparent)
                            .clickable { selectedTab = ReportTab.PENGELUARAN }
                            .padding(vertical = 10.dp)
                            .testTag("report_tab_expense"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pengeluaran",
                            color = if (selectedTab == ReportTab.PENGELUARAN) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selectedTab == ReportTab.BUDGETING) GoPayBrightTeal else Color.Transparent)
                            .clickable { selectedTab = ReportTab.BUDGETING }
                            .padding(vertical = 10.dp)
                            .testTag("report_tab_budgeting"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Budgeting",
                            color = if (selectedTab == ReportTab.BUDGETING) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (selectedTab == ReportTab.BUDGETING) {
                // Budgeting View Content
                item {
                    Text(
                        text = "Atur Limit Bulanan",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val categories = metaItems.filter { it.type == com.example.data.ItemType.EXPENSE_CATEGORY }
                items(categories) { category ->
                    val limit = budgetLimits.find { it.methodName == category.name }
                    val spent = filteredTransactions.filter { it.category == category.name && it.type == TransactionType.PENGELUARAN }.sumOf { it.amount + it.taxAmount }
                    val calculatedLimit = if (limit?.limitPercentage != null) {
                        (limit.limitPercentage / 100.0) * currentBalance
                    } else {
                        limit?.limitAmount ?: 0.0
                    }

                    BudgetMethodItem(
                        methodName = category.name,
                        spent = spent,
                        limit = calculatedLimit,
                        limitPercentage = limit?.limitPercentage,
                        limitAmount = limit?.limitAmount,
                        currentBalance = currentBalance,
                        onSetLimit = { amt: Double?, pct: Double? ->
                            viewModel.setBudgetLimit(category.name, amt, pct)
                        }
                    )
                }
            } else {
                // Ring/Donut Graph Representation
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = GoPayNavy),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Rincian Kategori",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (categoryStats.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Tidak ada data transaksi kategori di periode ini",
                                        color = Color.Gray,
                                        fontSize = 13.sp
                                    )
                                }
                            } else {
                                // Render high-fi custom Donut Gauge Canvas
                                Box(
                                    modifier = Modifier
                                        .size(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    DonutChart(items = categoryStats, colorProvider = ::getDynamicColor)

                                    // Center Label
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = if (selectedTab == ReportTab.PEMASUKAN) Icons.Default.TrendingUp else Icons.Default.RestaurantMenu,
                                            contentDescription = "Center Icon",
                                            tint = GoPayBrightTeal,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Category Details List representation
                if (categoryStats.isNotEmpty()) {
                    item {
                        Text(
                            text = "Rincian Transaksi",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(categoryStats) { stat ->
                        val color = remember(stat.category, metaItems) { getDynamicColor(stat.category) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(GoPayNavy)
                                .border(1.dp, OutlineVariant, RoundedCornerShape(12.dp))
                                .clickable { selectedDetailCategory = stat }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(stat.category),
                                        contentDescription = "Cat Icon",
                                        tint = color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = stat.category,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Dari ${stat.count} transaksi",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = (if (stat.amount < 0) "-" else "") + "Rp ${String.format("%,.0f", Math.abs(stat.amount)).replace(",", ".")}",
                                    color = if (stat.amount < 0) WondrCoralRed else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format("%.1f%%", stat.percentage),
                                    color = color,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedDetailCategory?.let { detail ->
        androidx.compose.ui.window.Dialog(onDismissRequest = { selectedDetailCategory = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GoPayNavy)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rincian ${detail.category}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    val sdf = remember { java.text.SimpleDateFormat("dd MMM", Locale("id", "ID")) }

                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(detail.transactions) { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GoPayDarkBlue)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = if (tx.notes.isNotBlank()) tx.notes else tx.category,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = sdf.format(Date(tx.timestamp)),
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "Rp ${String.format("%,.0f", tx.amount).replace(",", ".")}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { selectedDetailCategory = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoPayBrightTeal)
                    ) {
                        Text("Tutup", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetMethodItem(
    methodName: String,
    spent: Double,
    limit: Double,
    limitPercentage: Double?,
    limitAmount: Double?,
    currentBalance: Double,
    onSetLimit: (Double?, Double?) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    val progress = if (limit > 0) (spent / limit).toFloat() else 0f
    val color = getCategoryColor(methodName)
    val displayProgress = progress.coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GoPayNavy)
            .border(1.dp, OutlineVariant, RoundedCornerShape(12.dp))
            .clickable { showEditDialog = true }
            .padding(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(methodName),
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = methodName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Limit: ${if (limit > 0) "Rp ${String.format("%,.0f", limit).replace(",", ".")}" else "Tidak ada"}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    if (limitPercentage != null) {
                        Text(text = "(${String.format("%.1f", limitPercentage)}%)", color = GoPayBrightTeal, fontSize = 10.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = { displayProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (progress >= 0.9f) WondrCoralRed else GoPayBrightTeal,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Terpakai: Rp ${String.format("%,.0f", spent).replace(",", ".")}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format("%.1f%%", progress * 100),
                    color = if (progress >= 0.9f) WondrCoralRed else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showEditDialog) {
        var inputType by remember { mutableStateOf(if (limitPercentage != null) 1 else 0) } // 0: Amount, 1: %
        var amountStr by remember { mutableStateOf(limitAmount?.toLong()?.toString() ?: "") }
        var percentStr by remember { mutableStateOf(limitPercentage?.toString() ?: "") }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showEditDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GoPayNavy)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Atur Limit: $methodName", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(GoPayDarkBlue).padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(if (inputType == 0) GoPayBrightTeal else Color.Transparent).clickable { inputType = 0 }.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Rupiah", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(if (inputType == 1) GoPayBrightTeal else Color.Transparent).clickable { inputType = 1 }.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Persentase (%)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (inputType == 0) {
                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }
                                amountStr = clean
                            },
                            label = { Text("Limit Nominal (Rp)") },
                            prefix = { Text("Rp ", color = Color.White) },
                            visualTransformation = com.example.ui.ThousandsSeparatorVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E222A), unfocusedContainerColor = Color(0xFF1E222A)),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                    } else {
                        Column {
                            OutlinedTextField(
                                value = percentStr,
                                onValueChange = { percentStr = it },
                                label = { Text("Limit Persentase (%)") },
                                suffix = { Text("%", color = Color.White) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF1E222A), unfocusedContainerColor = Color(0xFF1E222A)),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                            
                            val pct = percentStr.toDoubleOrNull() ?: 0.0
                            val previewAmt = (pct / 100.0) * currentBalance
                            Text(
                                text = "Preview: Rp ${String.format("%,.0f", previewAmt).replace(",", ".")}",
                                color = GoPayBrightTeal,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onSetLimit(null, null); showEditDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = WondrCoralRed)
                        ) {
                            Text("Hapus Limit", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                if (inputType == 0) {
                                    onSetLimit(amountStr.toDoubleOrNull(), null)
                                } else {
                                    onSetLimit(null, percentStr.toDoubleOrNull())
                                }
                                showEditDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GoPayBrightTeal)
                        ) {
                            Text("Simpan", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

data class CategoryReportItem(
    val category: String,
    val amount: Double,
    val percentage: Double,
    val count: Int,
    val transactions: List<com.example.data.Transaction>
)

@Composable
fun DonutChart(items: List<CategoryReportItem>, colorProvider: (String) -> Color = ::getCategoryColor) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        var startAngle = -90f
        val strokeWidth = 24.dp.toPx()

        items.forEach { item ->
            val sweepAngle = (item.percentage * 3.6).toFloat()
            val color = colorProvider(item.category)

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            )

            startAngle += sweepAngle
        }
    }
}

// Global visual helpers for beautiful icons and color matching
fun getCategoryColor(category: String): Color {
    return when (category) {
        "Gaji" -> WondrEmeraldGreen // 0xFF28B781
        "Transfer Masuk" -> GoPayBrightTeal // 0xFF00B4D8 ? Wait, GoPayBrightTeal is distinct
        "Investasi" -> WondrGraphBlue
        "Uang Saku" -> WondrLimeAccent
        "Makanan & Minuman" -> Color(0xFFF4A261) // muted orange
        "Tagihan & Pulsa" -> Color(0xFFE76F51) // burnt sienna
        "Transportasi" -> Color(0xFF2A9D8F) // dark cyan
        "Belanja harian" -> Color(0xFF9B5DE5) // purple
        "Hiburan" -> Color(0xFFF15BB5) // pink
        "Wondr" -> Color(0xFFFF6D00) // sharp Orange
        "GoPay" -> GoPayNavy // 0xFF001F3F
        "Dana" -> Color(0xFF48CAE4) // bright blue
        "Cash" -> Color(0xFF80ED99) // light green
        "Lainnya" -> Color(0xFF9E9E9E) // grey
        else -> Color.Gray
    }
}

fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "Gaji" -> Icons.Default.Work
        "Transfer Masuk" -> Icons.Default.VerticalAlignBottom
        "Investasi" -> Icons.Default.ShowChart
        "Uang Saku" -> Icons.Default.CardGiftcard
        "Makanan & Minuman" -> Icons.Default.Restaurant
        "Tagihan & Pulsa" -> Icons.Default.Receipt
        "Transportasi" -> Icons.Default.DirectionsCar
        "Belanja harian" -> Icons.Default.ShoppingBag
        "Hiburan" -> Icons.Default.SportsEsports
        "Wondr" -> Icons.Default.AccountBalanceWallet
        "GoPay" -> Icons.Default.AccountBalanceWallet
        "Dana" -> Icons.Default.AccountBalanceWallet
        "Cash" -> Icons.Default.Payments
        "Lainnya" -> Icons.Default.Money
        else -> Icons.Default.Category
    }
}
