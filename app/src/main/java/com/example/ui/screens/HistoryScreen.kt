package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.BudgetViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val metaItems by viewModel.metaItems.collectAsState()

    // Filter and Search States
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedMethodFilter by remember { mutableStateOf<String?>(null) }
    var selectedDateFilterDays by remember { mutableStateOf<Int?>(null) } // null = All, 1 = Today, 7 = Last 7 days, 30 = Last 30 days

    // Filter Menu Expand toggles
    var activeFilterPanel by remember { mutableStateOf<String?>(null) } // "tanggal", "tipe", "kategori", "metode" or null

    // Date formatting helpers
    val daySdf = remember { SimpleDateFormat("EEEE, d MMM yyyy", Locale("id", "ID")) }
    val timeSdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val incomeCategories = metaItems.filter { it.type == com.example.data.ItemType.INCOME_CATEGORY }.map { it.name }.ifEmpty { listOf("Uang Saku", "Investasi", "Lainnya") }
    val expenseCategories = metaItems.filter { it.type == com.example.data.ItemType.EXPENSE_CATEGORY }.map { it.name }.ifEmpty { listOf("Makanan & Minuman", "Transportasi", "Belanja harian", "Keperluan Kuliah", "Tagihan & Pulsa", "Hiburan", "Lainnya") }
    val allMethods = metaItems.filter { it.type == com.example.data.ItemType.METHOD }.map { it.name }.ifEmpty { listOf("Cash", "Wondr", "Dana", "GoPay", "Lainnya") }
    val allCategories = if (selectedTypeFilter == TransactionType.PEMASUKAN) incomeCategories else if (selectedTypeFilter == TransactionType.PENGELUARAN) expenseCategories else if (selectedTypeFilter == TransactionType.TRANSFER) allMethods else emptyList()

    // For date picking
    var showDatePicker by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var exactDateFilter by remember { mutableStateOf<Long?>(null) }
    var dateRangeFilter by remember { mutableStateOf<LongRange?>(null) }

    // Filter logic
    val filteredTransactions = remember(transactions, searchQuery, selectedTypeFilter, selectedCategoryFilter, selectedMethodFilter, selectedDateFilterDays, exactDateFilter, dateRangeFilter) {
    val now = System.currentTimeMillis()
    transactions.filter { tx ->
        // Text Search filter
        val matchesSearch = tx.notes.lowercase().contains(searchQuery.lowercase()) ||
                tx.category.lowercase().contains(searchQuery.lowercase()) ||
                tx.amount.toString().contains(searchQuery)

        // Type filter
        val matchesType = selectedTypeFilter == null || tx.type == selectedTypeFilter

        // Category filter
        val matchesCategory = selectedCategoryFilter == null || tx.category == selectedCategoryFilter

        // Method filter
        val matchesMethod = selectedMethodFilter == null || tx.paymentMethod == selectedMethodFilter

        // Date filter
        val matchesDate = if (exactDateFilter != null) {
            val txDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(tx.timestamp))
            val filterDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(exactDateFilter!!))
            txDate == filterDate
        } else if (dateRangeFilter != null) {
            tx.timestamp in dateRangeFilter!!
        } else if (selectedDateFilterDays != null) {
            val durationMs = selectedDateFilterDays!!.toLong() * 24 * 60 * 60 * 1000
            (now - tx.timestamp) <= durationMs
        } else true

        matchesSearch && matchesType && matchesCategory && matchesMethod && matchesDate
    }
}

    // Grouping by Date string
    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy { tx ->
            daySdf.format(Date(tx.timestamp))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GoPayDarkBlue)
    ) {
        // Safe content padding block for top
        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars))

        // Live Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari rincian transaksi, atau kategori...", color = Color.Gray, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, "Search Icon", tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, "Clear Search", tint = Color.Gray)
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = GoPayNavy,
                unfocusedContainerColor = GoPayNavy,
                cursorColor = GoPayBrightTeal,
                focusedIndicatorColor = GoPayBrightTeal,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .testTag("search_bar"),
            singleLine = true
        )

        // Filter Pills Section (Tanggal, Kategori, Metode)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pill Tanggal
            FilterPill(
                label = when {
                    exactDateFilter != null -> "Tanggal Spesifik"
                    dateRangeFilter != null -> "Rentang Tanggal"
                    else -> "Semua Tanggal"
                },
                isActive = exactDateFilter != null || dateRangeFilter != null,
                isExpanded = activeFilterPanel == "tanggal",
                onClick = { activeFilterPanel = if (activeFilterPanel == "tanggal") null else "tanggal" }
            )

            // Pill Tipe
            FilterPill(
                label = selectedTypeFilter?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Tipe",
                isActive = selectedTypeFilter != null,
                isExpanded = activeFilterPanel == "tipe",
                onClick = { activeFilterPanel = if (activeFilterPanel == "tipe") null else "tipe" }
            )

            // Pill Kategori
            if (selectedTypeFilter != null) {
                FilterPill(
                    label = selectedCategoryFilter ?: "Kategori",
                    isActive = selectedCategoryFilter != null,
                    isExpanded = activeFilterPanel == "kategori",
                    onClick = { activeFilterPanel = if (activeFilterPanel == "kategori") null else "kategori" }
                )
            }

            // Pill Metode
            FilterPill(
                label = selectedMethodFilter ?: "Metode",
                isActive = selectedMethodFilter != null,
                isExpanded = activeFilterPanel == "metode",
                onClick = { activeFilterPanel = if (activeFilterPanel == "metode") null else "metode" }
            )

            // Reset filters link if any is active
            if (selectedDateFilterDays != null || selectedTypeFilter != null || selectedCategoryFilter != null || selectedMethodFilter != null || exactDateFilter != null || dateRangeFilter != null) {
                IconButton(
                    onClick = {
                        selectedDateFilterDays = null
                        selectedTypeFilter = null
                        selectedCategoryFilter = null
                        selectedMethodFilter = null
                        exactDateFilter = null
                        dateRangeFilter = null
                        activeFilterPanel = null
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WondrCoralRed.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Default.FilterListOff, "Clears", tint = WondrCoralRed, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Expanded sub-filter panel rows
        AnimatedVisibility(
            visible = activeFilterPanel != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoPayNavy)
                    .border(1.dp, OutlineVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                when (activeFilterPanel) {
                    "tanggal" -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (exactDateFilter == null && dateRangeFilter == null) GoPayBrightTeal else GoPayDarkBlue)
                                        .clickable {
                                            exactDateFilter = null
                                            dateRangeFilter = null
                                            selectedDateFilterDays = null
                                            activeFilterPanel = null
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Semua Waktu", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (exactDateFilter != null) GoPayBrightTeal else GoPayDarkBlue)
                                        .clickable { showDatePicker = true }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Pilih Tanggal Tunggal", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (dateRangeFilter != null) GoPayBrightTeal else GoPayDarkBlue)
                                        .clickable { showDateRangePicker = true }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Pilih Rentang Waktu", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    "tipe" -> {
                        Column {
                            Text("Pilih Tipe Transaksi", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedTypeFilter == null) GoPayBrightTeal else GoPayDarkBlue)
                                        .clickable {
                                            selectedTypeFilter = null
                                            selectedCategoryFilter = null
                                            activeFilterPanel = null
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Semua Tipe", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedTypeFilter == TransactionType.PEMASUKAN) GoPayBrightTeal else GoPayDarkBlue)
                                        .clickable {
                                            selectedTypeFilter = TransactionType.PEMASUKAN
                                            selectedCategoryFilter = null
                                            activeFilterPanel = null
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Pemasukan", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedTypeFilter == TransactionType.PENGELUARAN) GoPayBrightTeal else GoPayDarkBlue)
                                        .clickable {
                                            selectedTypeFilter = TransactionType.PENGELUARAN
                                            selectedCategoryFilter = null
                                            activeFilterPanel = null
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Pengeluaran", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedTypeFilter == TransactionType.TRANSFER) GoPayBrightTeal else GoPayDarkBlue)
                                        .clickable {
                                            selectedTypeFilter = TransactionType.TRANSFER
                                            selectedCategoryFilter = null
                                            activeFilterPanel = null
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Transfer", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    "kategori" -> {
                        Column {
                            Text("Pilih Kategori", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            // Scrollable categories row inside panel
                            Box(modifier = Modifier.height(36.dp)) {
                                androidx.compose.foundation.lazy.LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (selectedCategoryFilter == null) GoPayBrightTeal else GoPayDarkBlue)
                                                .clickable {
                                                    selectedCategoryFilter = null
                                                    activeFilterPanel = null
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Semua Kategori", color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                    items(allCategories) { cat ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (selectedCategoryFilter == cat) GoPayBrightTeal else GoPayDarkBlue)
                                                .clickable {
                                                    selectedCategoryFilter = cat
                                                    activeFilterPanel = null
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(cat, color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "metode" -> {
                        Column {
                            Text("Pilih Metode Pembayaran", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.height(36.dp)) {
                                androidx.compose.foundation.lazy.LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (selectedMethodFilter == null) GoPayBrightTeal else GoPayDarkBlue)
                                                .clickable {
                                                    selectedMethodFilter = null
                                                    activeFilterPanel = null
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Semua", color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                    items(allMethods) { method ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (selectedMethodFilter == method) GoPayBrightTeal else GoPayDarkBlue)
                                                .clickable {
                                                    selectedMethodFilter = method
                                                    activeFilterPanel = null
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(method, color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            if (groupedTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada riwayat transaksi yang cocok dengan filter pencarian.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                // Group transactions by date day headers
                groupedTransactions.forEach { (dateHeader, list) ->
                    item {
                        Text(
                            text = dateHeader,
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(list) { tx ->
                        val catColor = remember(tx.category) { getCategoryColor(tx.category) }
                        val catIcon = remember(tx.category) { getCategoryIcon(tx.category) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(GoPayNavy)
                                .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(catColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = catIcon,
                                        contentDescription = "Category Logo",
                                        tint = catColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = if (tx.notes.isNotBlank()) tx.notes else if (tx.type == TransactionType.TRANSFER) "Transfer dari ${tx.paymentMethod} ke ${tx.category}" else tx.category,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tx.category,
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val prefix = when (tx.type) {
                                    TransactionType.PEMASUKAN -> "+"
                                    TransactionType.PENGELUARAN -> "-"
                                    TransactionType.TRANSFER -> "⇄"
                                }
                                val textCol = when (tx.type) {
                                    TransactionType.PEMASUKAN -> WondrEmeraldGreen
                                    TransactionType.TRANSFER -> GoPayBrightTeal
                                    else -> Color.White
                                }
                                Text(
                                    text = "$prefix Rp ${String.format("%,.0f", tx.amount).replace(",", ".")}",
                                    color = textCol,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${tx.paymentMethod} Saldo",
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(GoPayBrightTeal)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    exactDateFilter = datePickerState.selectedDateMillis
                    selectedDateFilterDays = null
                    dateRangeFilter = null
                    showDatePicker = false
                    activeFilterPanel = null
                }) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    if (showDateRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        dateRangeFilter = start..end
                        exactDateFilter = null
                        selectedDateFilterDays = null
                        activeFilterPanel = null
                    }
                    showDateRangePicker = false
                }) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) { Text("Batal") }
            }
        ) {
            DateRangePicker(state = dateRangePickerState, title = { Text(text = "Pilih Rentang Waktu", modifier = Modifier.padding(16.dp)) })
        }
    }
}

@Composable
fun FilterPill(
    label: String,
    isActive: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isActive || isExpanded) GoPayBrightTeal.copy(alpha = 0.1f) else Color(0xFF1E293B)
    val borderColor = if (isActive || isExpanded) GoPayBrightTeal.copy(alpha = 0.3f) else Color(0xFF334155) // border-slate-700
    val contentColor = if (isActive || isExpanded) GoPayBrightTeal else Color(0xFF94A3B8) // text-slate-400
    
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp)) // rounded-full
            .background(bgColor)
            .border(
                1.dp,
                borderColor,
                RoundedCornerShape(100.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = "Caret",
            tint = contentColor,
            modifier = Modifier.size(14.dp)
        )
    }
}
