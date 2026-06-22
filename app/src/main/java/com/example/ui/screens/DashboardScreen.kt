package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import com.example.LocalNotification
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.input.VisualTransformation
import com.example.data.*
import com.example.ui.BudgetViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    prefix: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            trailingIcon = trailingIcon,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            prefix = if (prefix.isNotEmpty()) {
                { Text(prefix, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp)) }
            } else null,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF1E222A),
                unfocusedContainerColor = Color(0xFF1E222A),
                cursorColor = WondrEmeraldGreen,
                focusedIndicatorColor = Color(0xFF333842),
                unfocusedIndicatorColor = Color(0xFF333842),
                disabledIndicatorColor = Color(0xFF333842)
            )
        )
    }
}

@Composable
fun CustomActionDialog(
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color = GoPayBrightTeal,
    onConfirm: () -> Unit,
    dismissText: String = "Batal",
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2029)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    color = Color.Gray,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = confirmText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                if (dismissText != "Batal") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WondrCoralRed),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = dismissText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Batal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DashboardScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val isBalanceVisible by viewModel.isBalanceVisible

    var showAddDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf(TransactionType.PENGELUARAN) }

    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    // Savings Goals
    val allGoals by viewModel.allSavingsGoals.collectAsState()
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var selectedGoalForTopup by remember { mutableStateOf<com.example.data.SavingsGoal?>(null) }
    
    val metaItems by viewModel.metaItems.collectAsState()

    // Computations
    val currentMonthTransactions = remember(transactions) {
        val cal = Calendar.getInstance()
        val targetMonth = cal.get(Calendar.MONTH)
        val targetYear = cal.get(Calendar.YEAR)
        transactions.filter { tx ->
            val txCal = Calendar.getInstance()
            txCal.timeInMillis = tx.timestamp
            txCal.get(Calendar.MONTH) == targetMonth && txCal.get(Calendar.YEAR) == targetYear
        }
    }

    val monthlyIncome = remember(currentMonthTransactions) {
        currentMonthTransactions.filter { it.type == TransactionType.PEMASUKAN && it.category != "Tabungan" }.sumOf { it.amount }
    }
    val monthlyExpense = remember(currentMonthTransactions) {
        currentMonthTransactions.filter { it.type == TransactionType.PENGELUARAN && it.category != "Tabungan" }.sumOf { it.amount }
    }
    val monthlySelisih = monthlyIncome - monthlyExpense

    val totalIncome = remember(transactions) {
        transactions.filter { it.type == TransactionType.PEMASUKAN && it.category != "Tabungan" }.sumOf { it.amount }
    }
    val totalExpense = remember(transactions) {
        transactions.filter { it.type == TransactionType.PENGELUARAN && it.category != "Tabungan" }.sumOf { it.amount }
    }
    val currentBalance = remember(transactions) {
        transactions.filter { it.type == TransactionType.PEMASUKAN }.sumOf { it.amount } - 
        transactions.filter { it.type == TransactionType.PENGELUARAN }.sumOf { it.amount }
    }

    val methodBalances = remember(transactions) {
        val balances = mutableMapOf<String, Double>()
        transactions.forEach { tx ->
            if (tx.category == "Tabungan") return@forEach
            
            if (tx.type == TransactionType.TRANSFER) {
                // Source
                val outCurrent = balances[tx.paymentMethod] ?: 0.0
                balances[tx.paymentMethod] = outCurrent - tx.amount
                // Destination
                val inCurrent = balances[tx.category] ?: 0.0
                balances[tx.category] = inCurrent + tx.amount
            } else {
                val current = balances[tx.paymentMethod] ?: 0.0
                if (tx.type == TransactionType.PEMASUKAN) {
                    balances[tx.paymentMethod] = current + tx.amount
                } else if (tx.type == TransactionType.PENGELUARAN) {
                    balances[tx.paymentMethod] = current - tx.amount
                }
            }
        }
        balances
    }

    val df = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val listState = rememberLazyListState()
    val showSmallHeader by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 50 }
    }
    
    var recentAnimAmount by remember { mutableStateOf<Double?>(null) }
    var recentAnimType by remember { mutableStateOf<TransactionType?>(null) }
    val scope = rememberCoroutineScope()
    var achievedGoalPopupName by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GoPayDarkBlue,
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    dialogType = TransactionType.PENGELUARAN
                    showAddDialog = true
                },
                containerColor = GoPayBrightTeal,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. App Gradient Header Block (Sticky/Collapsible)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(GoPayBlueGradientStart, GoPayBlueGradientEnd)
                        )
                    )
                    .padding(top = 0.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Column {
                    AnimatedVisibility(visible = !showSmallHeader) {
                        Column {
                            // Top GoPay Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Finance Tracker",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Balance display (Click to Hide / Mask) - Always Visible
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.clickable { viewModel.toggleBalanceVisibility() }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isBalanceVisible) {
                                        "Rp ${String.format("%,.0f", currentBalance).replace(",", ".")}"
                                    } else {
                                        "Rp ••••••••"
                                    },
                                    color = Color.White,
                                    fontSize = if (showSmallHeader) 20.sp else 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Balance",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            AnimatedVisibility(
                                visible = recentAnimAmount != null,
                                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                                exit = fadeOut()
                            ) {
                                val amtStr = recentAnimAmount?.let { String.format("%,.0f", it).replace(",", ".") } ?: ""
                                val isInc = recentAnimType == TransactionType.PEMASUKAN
                                Column {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isInc) "+ Rp $amtStr" else "- Rp $amtStr",
                                        color = if (isInc) WondrEmeraldGreen else WondrCoralRed,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = !showSmallHeader) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            // Monthly spend insight badge
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.15f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingDown,
                                        contentDescription = "Trend",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Rp ${String.format("%,.0f", monthlyExpense).replace(",", ".")} dipakai di bulan ini",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Main Scrollable Container (Wondr Slate Dashboard Visuals)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
            // Dashboard Data Analytics Card (Wondr style)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = GoPayNavy),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Arus Kas Bulanan",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Selisih: Rp ${String.format("%,.0f", monthlySelisih).replace(",", ".")}",
                            color = if (monthlySelisih >= 0) WondrLimeAccent else WondrCoralRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom Double Bar Chart Comparing Monthly Income vs Expenses (Indonesian Labels)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DoubleBarChart(
                                income = monthlyIncome,
                                expense = monthlyExpense
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Numeric Metrics Comparison
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(WondrEmeraldGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pemasukan", color = Color.Gray, fontSize = 12.sp)
                                }
                                Text(
                                    text = "Rp ${String.format("%,.0f", totalIncome).replace(",", ".")}",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Divider(
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(1.dp)
                                    .align(Alignment.CenterVertically),
                                color = OutlineVariant
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(WondrCoralRed)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pengeluaran", color = Color.Gray, fontSize = 12.sp)
                                }
                                Text(
                                    text = "Rp ${String.format("%,.0f", totalExpense).replace(",", ".")}",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }



            // Savings Goals Section
            item {
                SavingsGoalSection(
                    goals = allGoals,
                    onAddGoalClick = { showAddGoalDialog = true },
                    onManageGoalClick = { goal -> selectedGoalForTopup = goal }
                )
            }

            // Recent Transactions List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaksi Terkini",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (transactions.isNotEmpty()) {
                        Text(
                            text = "Tap item untuk edit/hapus",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada transaksi di bulan ini. Coba tambahkan manual sekarang!",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .testTag("empty_state_text")
                    )
                }
            } else {
                items(transactions.take(8)) { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, OutlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .background(GoPayNavy)
                            .clickable { editingTransaction = tx }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f).padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (tx.type) {
                                            TransactionType.PEMASUKAN -> WondrEmeraldGreen.copy(alpha = 0.15f)
                                            TransactionType.PENGELUARAN -> WondrCoralRed.copy(alpha = 0.15f)
                                            TransactionType.TRANSFER -> GoPayBrightTeal.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (tx.type) {
                                        TransactionType.PEMASUKAN -> Icons.Default.ArrowUpward
                                        TransactionType.PENGELUARAN -> Icons.Default.ArrowDownward
                                        TransactionType.TRANSFER -> Icons.Default.SyncAlt
                                    },
                                    contentDescription = "Arrow",
                                    tint = when (tx.type) {
                                        TransactionType.PEMASUKAN -> WondrEmeraldGreen
                                        TransactionType.PENGELUARAN -> WondrCoralRed
                                        TransactionType.TRANSFER -> GoPayBrightTeal
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (tx.notes.isNotBlank()) tx.notes else if (tx.type == TransactionType.TRANSFER) "Transfer dana ke ${tx.category}" else tx.category,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(GoPayBrightTeal.copy(alpha = 0.15f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = tx.paymentMethod,
                                            color = GoPayBrightTeal,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = df.format(Date(tx.timestamp)),
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Price tag
                        Text(
                            text = (when (tx.type) {
                                TransactionType.PEMASUKAN -> "+"
                                TransactionType.PENGELUARAN -> "-"
                                TransactionType.TRANSFER -> "⇄ "
                            }) + "Rp ${String.format("%,.0f", tx.amount).replace(",", ".")}",
                            color = when (tx.type) {
                                TransactionType.PEMASUKAN -> WondrEmeraldGreen
                                TransactionType.TRANSFER -> GoPayBrightTeal
                                else -> Color.White
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
    }

    val notify = LocalNotification.current
    
    // Interactive Add Dialog Form
    if (showAddDialog) {
        AddEditTransactionDialog(
            isEdit = false,
            initialType = dialogType,
            currentBalance = currentBalance,
            methodBalances = methodBalances,
            metaItems = metaItems,
            onDismiss = { showAddDialog = false },
            onSave = { type, amount, method, category, note ->
                val matchingGoal = allGoals.find { it.name.equals(note, ignoreCase = true) && it.targetAmount == amount }
                if (type == TransactionType.PENGELUARAN && matchingGoal != null) {
                    viewModel.deleteSavingsGoalDirectly(matchingGoal)
                    if (matchingGoal.currentAmount > 0) {
                        viewModel.addTransaction(
                            type = TransactionType.PEMASUKAN,
                            amount = matchingGoal.currentAmount,
                            paymentMethod = "Lainnya",
                            category = "Lainnya",
                            notes = "Menarik dana tabungan: ${matchingGoal.name}"
                        )
                    }
                    achievedGoalPopupName = matchingGoal.name
                } else {
                    notify?.invoke("Transaksi Berhasil Disimpan!")
                }
                viewModel.addTransaction(type, amount, method, category, note)
                showAddDialog = false
                recentAnimAmount = amount
                recentAnimType = type
                scope.launch {
                    kotlinx.coroutines.delay(2000)
                    recentAnimAmount = null
                }
            }
        )
    }

    // Interactive Edit Dialog Form
    editingTransaction?.let { tx ->
        AddEditTransactionDialog(
            isEdit = true,
            initialType = tx.type,
            initialAmount = tx.amount,
            initialMethod = tx.paymentMethod,
            initialCategory = tx.category,
            initialNotes = tx.notes,
            currentBalance = currentBalance + (if (tx.type == TransactionType.PENGELUARAN) tx.amount else -tx.amount),
            methodBalances = methodBalances,
            metaItems = metaItems,
            onDismiss = { editingTransaction = null },
            onSave = { type, amount, method, category, note ->
                viewModel.updateTransaction(tx.id, type, amount, method, category, note, tx.timestamp)
                editingTransaction = null
                notify?.invoke("Transaksi Berhasil Diubah!")
                val diff = amount - tx.amount
                val effType = if (diff > 0) type else (if (type == TransactionType.PENGELUARAN) TransactionType.PEMASUKAN else TransactionType.PENGELUARAN)
                recentAnimAmount = kotlin.math.abs(diff)
                recentAnimType = effType
                scope.launch {
                    kotlinx.coroutines.delay(2000)
                    recentAnimAmount = null
                }
            },
            onDelete = {
                viewModel.deleteTransaction(tx.id)
                editingTransaction = null
                notify?.invoke("Transaksi Berhasil Dihapus!")
            }
        )
    }

    if (showAddGoalDialog) {
        AddSavingsGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = { name, target, date, category ->
                viewModel.addSavingsGoal(name, target, date, category)
                showAddGoalDialog = false
                notify?.invoke("Tabungan Baru Berhasil Dibuat!")
            }
        )
    }

    selectedGoalForTopup?.let { goal ->
        ManageGoalDialog(
            goal = goal,
            onDismiss = { selectedGoalForTopup = null },
            onTopUp = { amount ->
                viewModel.addFundsToGoal(goal, amount)
                selectedGoalForTopup = null
                notify?.invoke("Top Up Tabungan Berhasil!")
                recentAnimAmount = amount
                recentAnimType = TransactionType.PENGELUARAN 
                scope.launch {
                    kotlinx.coroutines.delay(2000)
                    recentAnimAmount = null
                }
            },
            onWithdraw = { amount ->
                viewModel.subtractFundsFromGoal(goal, amount)
                selectedGoalForTopup = null
                notify?.invoke("Tarik Dana Tabungan Berhasil!")
                recentAnimAmount = amount
                recentAnimType = TransactionType.PEMASUKAN
                scope.launch {
                    kotlinx.coroutines.delay(2000)
                    recentAnimAmount = null
                }
            },
            onDelete = {
                viewModel.deleteSavingsGoal(goal)
                selectedGoalForTopup = null
                notify?.invoke("Tabungan Dihapus!")
                if (goal.currentAmount > 0) {
                    recentAnimAmount = goal.currentAmount
                    recentAnimType = TransactionType.PEMASUKAN
                    scope.launch {
                        kotlinx.coroutines.delay(2000)
                        recentAnimAmount = null
                    }
                }
            },
            onMarkDone = {
                viewModel.deleteSavingsGoalDirectly(goal)
                
                // Also refund to balance
                viewModel.addTransaction(
                    TransactionType.PEMASUKAN,
                    goal.currentAmount,
                    "Lainnya",
                    "Tabungan",
                    "Pencairan tabungan: ${goal.name}"
                )

                selectedGoalForTopup = null
                notify?.invoke("Tabungan Berhasil Dicapai, Dana dikembalikan!")
            },
            onMarkDoneAutomatically = { method, notes ->
                viewModel.deleteSavingsGoalDirectly(goal)
                
                // Log it as pengeluaran automatically rather than returning to balance
                viewModel.addTransaction(
                    TransactionType.PENGELUARAN,
                    goal.currentAmount,
                    paymentMethod = method,
                    category = goal.category,
                    notes = notes
                )

                selectedGoalForTopup = null
                notify?.invoke("Tabungan Selesai & Transaksi Otomatis Dibuat!")
            },
            onEdit = { newName, newTarget, newCategory ->
                viewModel.updateSavingsGoal(goal, newName, newTarget, goal.targetDateMillis, newCategory)
                selectedGoalForTopup = null
                notify?.invoke("Target Tabungan Diubah!")
            }
        )
    }

    achievedGoalPopupName?.let { popupName ->
        AlertDialog(
            onDismissRequest = { achievedGoalPopupName = null },
            title = { Text("Selamat! \uD83C\uDF89", color = WondrEmeraldGreen, fontWeight = FontWeight.Bold) },
            text = { Text("Transaksi pengeluaranmu cocok dengan target tabungan '$popupName'.", color = Color.White) },
            confirmButton = {
                TextButton(onClick = { achievedGoalPopupName = null }) {
                    Text("Luar biasa", color = WondrEmeraldGreen, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = GoPayNavy,
            titleContentColor = WondrEmeraldGreen,
            textContentColor = Color.White
        )
    }
}

@Composable
fun DoubleBarChart(income: Double, expense: Double) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        val total = income + expense
        val maxVal = if (total == 0.0) 100.0 else maxOf(income, expense)

        val spacing = 40.dp.toPx()
        val barWidth = 32.dp.toPx()
        val chartHeight = size.height - 40.dp.toPx()

        val center = size.width / 2

        // Axis base floor
        drawLine(
            color = OutlineVariant,
            start = Offset(20.dp.toPx(), chartHeight),
            end = Offset(size.width - 20.dp.toPx(), chartHeight),
            strokeWidth = 2f
        )

        // Draw Income Bar (Left side)
        val incomeHeight = if (income > 0) (income / maxVal * chartHeight).toFloat() else 5f
        val incomeX = center - spacing - barWidth / 2
        val incomeY = chartHeight - incomeHeight

        drawRoundRect(
            color = WondrEmeraldGreen,
            topLeft = Offset(incomeX, incomeY),
            size = Size(barWidth, incomeHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )

        // Draw Expense Bar (Right side)
        val expenseHeight = if (expense > 0) (expense / maxVal * chartHeight).toFloat() else 5f
        val expenseX = center + spacing - barWidth / 2
        val expenseY = chartHeight - expenseHeight

        drawRoundRect(
            color = WondrCoralRed,
            topLeft = Offset(expenseX, expenseY),
            size = Size(barWidth, expenseHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    isEdit: Boolean,
    initialType: TransactionType,
    initialAmount: Double = 0.0,
    initialMethod: String = "",
    initialCategory: String = "",
    initialNotes: String = "",
    currentBalance: Double = 0.0,
    methodBalances: Map<String, Double> = emptyMap(),
    metaItems: List<com.example.data.MetaItem> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (TransactionType, Double, String, String, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var type by remember { mutableStateOf(initialType) }
    var amountRaw by remember { mutableStateOf(if (initialAmount > 0) initialAmount.toLong() else 0L) }
    var amountStr by remember { mutableStateOf(if (amountRaw > 0) amountRaw.toString() else "") }
    var paymentMethod by remember { mutableStateOf(initialMethod) }
    var notes by remember { mutableStateOf(initialNotes) }

    val incomeCategories = metaItems.filter { it.type == com.example.data.ItemType.INCOME_CATEGORY }.map { it.name }.ifEmpty { listOf("Uang Saku", "Investasi", "Lainnya") }
    val expenseCategories = metaItems.filter { it.type == com.example.data.ItemType.EXPENSE_CATEGORY }.map { it.name }.ifEmpty { listOf("Makanan & Minuman", "Transportasi", "Belanja harian", "Keperluan Kuliah", "Tagihan & Pulsa", "Hiburan", "Lainnya") }
    val paymentMethods = metaItems.filter { it.type == com.example.data.ItemType.METHOD }.map { it.name }.ifEmpty { listOf("Cash", "Wondr", "Dana", "GoPay", "Lainnya") }

    val categories = if (type == TransactionType.PEMASUKAN) incomeCategories else if (type == TransactionType.TRANSFER) paymentMethods else expenseCategories
    var selectedCategory by remember { mutableStateOf(if (initialCategory.isNotBlank()) initialCategory else "") }

    var expandedMethod by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }
    var showInsufficientBalanceConfirm by remember { mutableStateOf(false) }
    var showSuggestTransferConfirm by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = GoPayNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (showInsufficientBalanceConfirm) {
                CustomActionDialog(
                    title = "Saldo Tidak Mencukupi",
                    message = "Saldo $paymentMethod anda saat ini tidak mencukupi untuk melakukan transfer sebesar Rp ${String.format("%,.0f", amountRaw.toDouble()).replace(",", ".")}.",
                    confirmText = "Edit Nominal Transaksi",
                    onConfirm = { showInsufficientBalanceConfirm = false },
                    onDismiss = onDismiss
                )
            } else if (showSuggestTransferConfirm) {
                CustomActionDialog(
                    title = "Saldo Akun Kurang",
                    message = "Saldo di akun $paymentMethod tidak mencukupi untuk pengeluaran ini. Ingin melakukan transfer ke akun $paymentMethod terlebih dahulu?",
                    confirmText = "Ya, Buat Transfer",
                    onConfirm = { 
                        showSuggestTransferConfirm = false
                        type = TransactionType.TRANSFER
                        selectedCategory = paymentMethod
                        paymentMethod = ""
                    },
                    dismissText = "Abaikan, Tetap Catat Pengeluaran",
                    onDismiss = { 
                        showSuggestTransferConfirm = false
                        onSave(type, amountRaw.toDouble(), paymentMethod, selectedCategory, notes) 
                    }
                )
            } else {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isEdit) "Ubah Transaksi" else "Tambah Transaksi Baru",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Type switcher: Income / Expense Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoPayDarkBlue)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (type == TransactionType.PENGELUARAN) WondrCoralRed else Color.Transparent)
                            .clickable { type = TransactionType.PENGELUARAN }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Pengeluaran",
                            color = if (type == TransactionType.PENGELUARAN) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (type == TransactionType.TRANSFER) GoPayBrightTeal else Color.Transparent)
                            .clickable { type = TransactionType.TRANSFER }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Transfer",
                            color = if (type == TransactionType.TRANSFER) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (type == TransactionType.PEMASUKAN) WondrEmeraldGreen else Color.Transparent)
                            .clickable { type = TransactionType.PEMASUKAN }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Pemasukan",
                            color = if (type == TransactionType.PEMASUKAN) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Nominal Amount Input Field
                CustomInputBox(
                    value = amountStr,
                    onValueChange = { input ->
                        val cleanString = input.filter { it.isDigit() }
                        // optionally strip leading zeroes
                        val stripped = if (cleanString.startsWith("0") && cleanString.length > 1) cleanString.trimStart('0') else cleanString
                        if (stripped.isNotEmpty()) {
                            val raw = stripped.toLongOrNull() ?: 0L
                            amountRaw = raw
                            amountStr = stripped
                        } else {
                            amountRaw = 0L
                            amountStr = ""
                        }
                    },
                    label = "Nominal",
                    prefix = "Rp",
                    visualTransformation = com.example.ui.ThousandsSeparatorVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("amount_field")
                )

                // Transaksi Jenis/Payment Method click selection row
                ExposedDropdownMenuBox(
                    expanded = expandedMethod,
                    onExpandedChange = { expandedMethod = !expandedMethod }
                ) {
                    CustomInputBox(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = if (type == TransactionType.TRANSFER) "Dari (Sumber Dana)" else "Metode Pembayaran",
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMethod) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMethod,
                        onDismissRequest = { expandedMethod = false },
                        modifier = Modifier.background(GoPayNavy)
                    ) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method, color = Color.White) },
                                onClick = {
                                    paymentMethod = method
                                    expandedMethod = false
                                }
                            )
                        }
                    }
                }

                // Category configurations flow wrap
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    CustomInputBox(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = if (type == TransactionType.TRANSFER) "Ke (Tujuan Dana)" else "Kategori",
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false },
                        modifier = Modifier.background(GoPayNavy)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = Color.White) },
                                onClick = {
                                    selectedCategory = cat
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                // Description/Keterangan notes (Optional)
                CustomInputBox(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Keterangan / Deskripsi (Opsional)",
                    modifier = Modifier.fillMaxWidth().testTag("notes_field")
                )

                // Dialog Buttons Bottom Drawer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isEdit && onDelete != null) {
                        Button(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = WondrCoralRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Hapus", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Batal", color = Color.White, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val amount = amountRaw.toDouble()
                            if (amount > 0.0 && paymentMethod.isNotBlank() && selectedCategory.isNotBlank()) {
                                if (type == TransactionType.TRANSFER && paymentMethod == selectedCategory) {
                                    android.widget.Toast.makeText(context, "Metode asal dan tujuan transfer tidak boleh sama", android.widget.Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val methodBalance = methodBalances[paymentMethod] ?: 0.0
                                if (type == TransactionType.TRANSFER && amount > methodBalance) {
                                    showInsufficientBalanceConfirm = true
                                } else if (type == TransactionType.PENGELUARAN && amount > methodBalance) {
                                    showSuggestTransferConfirm = true
                                } else {
                                    onSave(type, amount, paymentMethod, selectedCategory, notes)
                                }
                            }
                        },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = GoPayBrightTeal),
                        shape = RoundedCornerShape(8.dp),
                        enabled = amountRaw > 0,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Simpan", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                } // end Column
            } // end else
        } // end Card
    } // end Dialog
} // end fun
} // extra brace
