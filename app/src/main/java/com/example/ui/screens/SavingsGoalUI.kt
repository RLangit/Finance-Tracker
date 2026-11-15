package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.SavingsGoal
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SavingsGoalSection(
    goals: List<SavingsGoal>,
    onAddGoalClick: () -> Unit,
    onManageGoalClick: (SavingsGoal) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Target Tabungan",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "+ Tambah",
                color = GoPayBrightTeal,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onAddGoalClick() }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (goals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoPayNavy)
                    .border(1.dp, OutlineVariant, RoundedCornerShape(12.dp))
                    .clickable { onAddGoalClick() }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada tabungan, ayo buat target impianmu!",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(goals) { goal ->
                    SavingsGoalCard(goal = goal, onManageGoalClick = { onManageGoalClick(goal) })
                }
            }
        }
    }
}

@Composable
fun SavingsGoalCard(goal: SavingsGoal, onManageGoalClick: () -> Unit) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0) else 0.0

    Card(
        modifier = Modifier
            .width(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, OutlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onManageGoalClick() },
        colors = CardDefaults.cardColors(containerColor = GoPayNavy)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = goal.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = GoPayBrightTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = GoPayBrightTeal,
                trackColor = GoPayDarkBlue
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Terkumpul",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Rp ${String.format("%,.0f", goal.currentAmount).replace(",", ".")}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Rp ${String.format("%,.0f", goal.targetAmount).replace(",", ".")}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsGoalDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, Long, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetRaw by remember { mutableStateOf(0L) }
    var targetStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var expandedCategory by remember { mutableStateOf(false) }
    
    val categories = listOf("Makanan & Minuman", "Transportasi", "Belanja harian", "Keperluan Kuliah", "Tagihan & Pulsa", "Hiburan", "Lainnya")

    // Simple date picking simulation, we'll just set it to 3 months from now for simplicity.
    val defaultDate = System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = GoPayNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Buat Impian Baru",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                CustomInputBox(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nama Impian (Misal: Beli Motor)",
                    modifier = Modifier.fillMaxWidth()
                )

                CustomInputBox(
                    value = targetStr,
                    onValueChange = { input ->
                        val cleanString = input.filter { it.isDigit() }
                        val stripped = if (cleanString.startsWith("0") && cleanString.length > 1) cleanString.trimStart('0') else cleanString
                        if (stripped.isNotEmpty()) {
                            val raw = stripped.toLongOrNull() ?: 0L
                            targetRaw = raw
                            targetStr = stripped
                        } else {
                            targetRaw = 0L
                            targetStr = ""
                        }
                    },
                    label = "Target",
                    prefix = "Rp",
                    visualTransformation = com.example.ui.ThousandsSeparatorVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    CustomInputBox(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = "Kategori (Pengeluaran)",
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false },
                        modifier = Modifier.background(GoPayDarkBlue)
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c, color = Color.White) },
                                onClick = {
                                    category = c
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("Batal", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val target = targetRaw.toDouble()
                            if (name.isNotBlank() && target > 0 && category.isNotBlank()) {
                                onSave(name, target, defaultDate, category)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoPayBrightTeal),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageGoalDialog(
    goal: SavingsGoal,
    onDismiss: () -> Unit,
    onTopUp: (Double, String) -> Unit,
    onWithdraw: (Double, String) -> Unit,
    onDelete: () -> Unit,
    onMarkDone: () -> Unit,
    onMarkDoneAutomatically: (String, String) -> Unit,
    onEdit: (String, Double, String) -> Unit,
    methodBalances: Map<String, Double> = emptyMap(),
    metaItems: List<com.example.data.MetaItem> = emptyList(),
    getDynamicColor: (String) -> androidx.compose.ui.graphics.Color = { androidx.compose.ui.graphics.Color.Gray }
) {
    var amountRaw by remember { mutableStateOf(0L) }
    var amountStr by remember { mutableStateOf("") }
    var depositMode by remember { mutableStateOf(0) } // 0: Nominal (Rp), 1: Persentase (%)
    var percentageStr by remember { mutableStateOf("") }
    val remaining = goal.targetAmount - goal.currentAmount

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMarkDoneConfirm by remember { mutableStateOf(false) }
    var showMarkDoneAutoConfig by remember { mutableStateOf(false) }
    var showExceedConfirm by remember { mutableStateOf(false) }
    var showExceedOptions by remember { mutableStateOf(false) }

    var isEditingMode by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(goal.name) }
    var editTargetRaw by remember { mutableStateOf(goal.targetAmount.toLong()) }
    var editTargetStr by remember { mutableStateOf(goal.targetAmount.toLong().toString()) }
    var editCategory by remember { mutableStateOf(goal.category) }
    var expandedEditCategory by remember { mutableStateOf(false) }
    
    val categories = listOf("Makanan & Minuman", "Transportasi", "Belanja harian", "Keperluan Kuliah", "Tagihan & Pulsa", "Hiburan", "Lainnya")
    val allMethods = metaItems.filter { it.type == com.example.data.ItemType.METHOD }.map { it.name }.ifEmpty { listOf("Cash", "Wondr", "Dana", "GoPay", "Lainnya") }

    var selectedSourceMethod by remember { mutableStateOf(allMethods.first()) }
    var expandedSourceMethod by remember { mutableStateOf(false) }

    var autoMethod by remember { mutableStateOf("") }
    var expandedAutoMethod by remember { mutableStateOf(false) }
    var autoNotes by remember { mutableStateOf(goal.name) }

    val goalMethodStats = remember(goal.sourceBalances) {
        val total = goal.currentAmount
        goal.sourceBalances.map { (method, amount) ->
            CategoryReportItem(
                category = method,
                amount = amount,
                percentage = if (total > 0) (amount / total * 100) else 0.0,
                count = 0,
                transactions = emptyList()
            )
        }.filter { it.amount > 0 }.sortedByDescending { it.amount }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = GoPayNavy),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (showDeleteConfirm) {
                CustomActionDialog(
                    title = "Hapus Tabungan?",
                    message = "Progres tabungan akan dicairkan ke saldo utama sesuai sumber asalnya.",
                    confirmText = "Hapus",
                    confirmColor = WondrCoralRed,
                    onConfirm = onDelete,
                    onDismiss = { showDeleteConfirm = false }
                )
            } else if (showExceedConfirm) {
                val currentMethodBalance = (methodBalances[selectedSourceMethod] ?: 0.0).coerceAtLeast(0.0)
                val percentageVal = percentageStr.toDoubleOrNull() ?: 0.0
                val calculatedFromPct = (percentageVal / 100.0) * currentMethodBalance
                val effectiveAmount = if (depositMode == 0) amountRaw.toDouble() else calculatedFromPct

                CustomActionDialog(
                    title = "Tabungan Melebihi Target",
                    message = "Jumlah ini akan membuat tabungan melebihi target. Anda ingin lanjut memasukkan semuanya?",
                    confirmText = "Ya, Lanjut",
                    onConfirm = { 
                        onTopUp(effectiveAmount, selectedSourceMethod)
                        showExceedConfirm = false
                    },
                    dismissText = "Pilihan Lain",
                    onDismiss = { 
                        showExceedConfirm = false
                        showExceedOptions = true 
                    }
                )
            } else if (showExceedOptions) {
                CustomActionDialog(
                    title = "Pilihan Pengisian",
                    message = "Bagaimana Anda ingin melanjutkan?",
                    confirmText = "Isi hingga target (Rp ${String.format("%,.0f", remaining).replace(",", ".")})",
                    onConfirm = { 
                        onTopUp(remaining, selectedSourceMethod)
                        showExceedOptions = false
                    },
                    onDismiss = { showExceedOptions = false }
                )
            } else if (showMarkDoneConfirm) {
                CustomActionDialog(
                    title = "Selesaikan Tabungan",
                    message = "Target tabungan tercapai! Apakah Anda ingin membuat transaksi pengeluaran secara otomatis untuk pembelian ini?",
                    confirmText = "Ya, Otomatis Buat Transaksi",
                    confirmColor = WondrEmeraldGreen,
                    onConfirm = { 
                        showMarkDoneConfirm = false
                        showMarkDoneAutoConfig = true
                    },
                    dismissText = "Tidak, Kembalikan ke Saldo Utama",
                    onDismiss = { 
                        showMarkDoneConfirm = false
                        onMarkDone()
                    }
                )
            } else if (showMarkDoneAutoConfig) {
                 Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Catat Transaksi Pengeluaran", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedAutoMethod,
                        onExpandedChange = { expandedAutoMethod = !expandedAutoMethod }
                    ) {
                        CustomInputBox(
                            value = autoMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = "Metode Pembayaran",
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAutoMethod) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedAutoMethod,
                            onDismissRequest = { expandedAutoMethod = false },
                            modifier = Modifier.background(GoPayDarkBlue)
                        ) {
                            allMethods.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m, color = Color.White) },
                                    onClick = {
                                        autoMethod = m
                                        expandedAutoMethod = false
                                    }
                                )
                            }
                        }
                    }

                    CustomInputBox(
                        value = autoNotes,
                        onValueChange = { autoNotes = it },
                        label = "Keterangan",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { showMarkDoneAutoConfig = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                            Text("Batal", color = Color.White)
                        }
                        Button(
                            onClick = { 
                                if (autoMethod.isNotBlank()) {
                                    showMarkDoneAutoConfig = false
                                    onMarkDoneAutomatically(autoMethod, autoNotes)
                                }
                            }, 
                            modifier = Modifier.weight(1f), 
                            colors = ButtonDefaults.buttonColors(containerColor = WondrEmeraldGreen),
                            enabled = autoMethod.isNotBlank()
                        ) {
                            Text("Catat Trx", color = Color.White)
                        }
                    }
                }
            } else if (isEditingMode) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Ubah Target Tabungan", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    CustomInputBox(
                        value = editName,
                        onValueChange = { editName = it },
                        label = "Nama Tabungan",
                        modifier = Modifier.fillMaxWidth()
                    )

                    CustomInputBox(
                        value = editTargetStr,
                        onValueChange = { input ->
                            val cleanString = input.filter { it.isDigit() }
                            val stripped = if (cleanString.startsWith("0") && cleanString.length > 1) cleanString.trimStart('0') else cleanString
                            if (stripped.isNotEmpty()) {
                                val raw = stripped.toLongOrNull() ?: 0L
                                editTargetRaw = raw
                                editTargetStr = stripped
                            } else {
                                editTargetRaw = 0L
                                editTargetStr = ""
                            }
                        },
                        label = "Target Maksimal",
                        prefix = "Rp",
                        visualTransformation = com.example.ui.ThousandsSeparatorVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedEditCategory,
                        onExpandedChange = { expandedEditCategory = !expandedEditCategory }
                    ) {
                        CustomInputBox(
                            value = editCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = "Kategori (Pengeluaran)",
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEditCategory) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedEditCategory,
                            onDismissRequest = { expandedEditCategory = false },
                            modifier = Modifier.background(GoPayDarkBlue)
                        ) {
                            categories.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c, color = Color.White) },
                                    onClick = {
                                        editCategory = c
                                        expandedEditCategory = false
                                    }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Button(onClick = { isEditingMode = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                            Text("Batal", color = Color.White)
                        }
                        Button(
                            onClick = {
                                if (editName.isNotBlank() && editTargetRaw > 0 && editCategory.isNotBlank()) {
                                    onEdit(editName, editTargetRaw.toDouble(), editCategory)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GoPayBrightTeal),
                            enabled = editName.isNotBlank() && editTargetRaw > 0 && editCategory.isNotBlank()
                        ) {
                            Text("Simpan", color = Color.White)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kelola ${goal.name}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { isEditingMode = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Target", tint = GoPayBrightTeal)
                        }
                    }

                    if (goalMethodStats.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.1f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(60.dp)) {
                                DonutChart(items = goalMethodStats, colorProvider = getDynamicColor)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                goalMethodStats.forEach { stat ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(getDynamicColor(stat.category)))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${stat.category}: Rp ${String.format("%,.0f", stat.amount).replace(",", ".")}",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "Terkumpul: Rp ${String.format("%,.0f", goal.currentAmount).replace(",", ".")}\n" +
                                if (remaining > 0) "Kurang Rp ${String.format("%,.0f", remaining).replace(",", ".")} lagi!" else "Target Terpenuhi!",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = expandedSourceMethod,
                            onExpandedChange = { expandedSourceMethod = !expandedSourceMethod }
                        ) {
                            CustomInputBox(
                                value = selectedSourceMethod,
                                onValueChange = {},
                                readOnly = true,
                                label = "Sumber / Tujuan Dana",
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSourceMethod) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedSourceMethod,
                                onDismissRequest = { expandedSourceMethod = false },
                                modifier = Modifier.background(GoPayDarkBlue)
                            ) {
                                allMethods.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(m, color = Color.White) },
                                        onClick = {
                                            selectedSourceMethod = m
                                            expandedSourceMethod = false
                                        }
                                    )
                                }
                            }
                        }

                        val currentMethodBalance = (methodBalances[selectedSourceMethod] ?: 0.0).coerceAtLeast(0.0)
                        val percentageVal = percentageStr.toDoubleOrNull() ?: 0.0
                        val calculatedFromPct = (percentageVal / 100.0) * currentMethodBalance

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = depositMode == 0,
                                onClick = { depositMode = 0 },
                                label = { Text("Nominal (Rp)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoPayBrightTeal,
                                    selectedLabelColor = Color.White,
                                    containerColor = GoPayDarkBlue,
                                    labelColor = Color.Gray
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = depositMode == 1,
                                onClick = { depositMode = 1 },
                                label = { Text("Persentase (%)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoPayBrightTeal,
                                    selectedLabelColor = Color.White,
                                    containerColor = GoPayDarkBlue,
                                    labelColor = Color.Gray
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (depositMode == 0) {
                            val hasGoalOp = remember(amountStr) { com.example.ui.ArithmeticEvaluator.containsOperator(amountStr) }
                            val evalGoalVal = remember(amountStr) { com.example.ui.ArithmeticEvaluator.evaluate(amountStr) }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                CustomInputBox(
                                    value = amountStr,
                                    onValueChange = { input ->
                                        val filtered = input.filter { it.isDigit() || it in listOf('.', ',', '+', '-', '*', 'x', 'X', '×', ':', '/', '÷', '(', ')', ' ') }
                                        amountStr = filtered
                                        if (com.example.ui.ArithmeticEvaluator.containsOperator(filtered)) {
                                            val eval = com.example.ui.ArithmeticEvaluator.evaluate(filtered)
                                            if (eval != null && eval >= 0) {
                                                amountRaw = eval.toLong()
                                            }
                                        } else {
                                            val cleanString = filtered.filter { it.isDigit() }
                                            val stripped = if (cleanString.startsWith("0") && cleanString.length > 1) cleanString.trimStart('0') else cleanString
                                            if (stripped.isNotEmpty()) {
                                                amountRaw = stripped.toLongOrNull() ?: 0L
                                            } else {
                                                amountRaw = 0L
                                            }
                                        }
                                    },
                                    label = "Nominal",
                                    prefix = "Rp",
                                    visualTransformation = com.example.ui.ExpressionThousandsSeparatorVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (hasGoalOp) {
                                    if (evalGoalVal != null && evalGoalVal >= 0) {
                                        Text(
                                            text = "Hasil: Rp ${com.example.ui.ArithmeticEvaluator.formatResult(evalGoalVal)}",
                                            color = GoPayBrightTeal,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "Format kalkulasi belum lengkap",
                                            color = Color.Gray,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                CustomInputBox(
                                    value = percentageStr,
                                    onValueChange = { input ->
                                        val clean = input.filter { it.isDigit() || it == '.' }
                                        val num = clean.toDoubleOrNull()
                                        if (clean.isEmpty() || (num != null && num <= 100.0)) {
                                            percentageStr = clean
                                        }
                                    },
                                    label = "Persentase (%) dari $selectedSourceMethod",
                                    prefix = "%",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(10, 25, 50, 75, 100).forEach { pct ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (percentageStr == pct.toString()) GoPayBrightTeal else Color.White.copy(alpha = 0.1f))
                                                .clickable { percentageStr = pct.toString() }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$pct%",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "Saldo $selectedSourceMethod: Rp ${String.format("%,.0f", currentMethodBalance).replace(",", ".")}" +
                                            if (percentageVal > 0) " → Nominal: Rp ${String.format("%,.0f", calculatedFromPct).replace(",", ".")}" else "",
                                    color = GoPayBrightTeal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (remaining <= 0) {
                        Button(
                            onClick = { showMarkDoneConfirm = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WondrEmeraldGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Selesai & Hapus Tabungan", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = WondrCoralRed.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Hapus", color = WondrCoralRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        val currentEffectiveAmount = if (depositMode == 0) {
                            if (com.example.ui.ArithmeticEvaluator.containsOperator(amountStr)) {
                                com.example.ui.ArithmeticEvaluator.evaluate(amountStr) ?: amountRaw.toDouble()
                            } else {
                                amountRaw.toDouble()
                            }
                        } else {
                            ((percentageStr.toDoubleOrNull() ?: 0.0) / 100.0) * ((methodBalances[selectedSourceMethod] ?: 0.0).coerceAtLeast(0.0))
                        }
                        val canWithdraw = currentEffectiveAmount > 0 && (goal.sourceBalances[selectedSourceMethod] ?: 0.0) >= currentEffectiveAmount

                        Button(
                            onClick = {
                                if (canWithdraw) onWithdraw(currentEffectiveAmount, selectedSourceMethod)
                            },
                            modifier = Modifier.weight(1.1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GoPayDarkBlue),
                            shape = RoundedCornerShape(8.dp),
                            enabled = canWithdraw,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Tarik", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (remaining > 0) {
                            Button(
                                onClick = {
                                    val topupAmt = currentEffectiveAmount
                                    if (topupAmt > remaining && remaining > 0) {
                                        showExceedConfirm = true
                                    } else {
                                        if (topupAmt > 0) onTopUp(topupAmt, selectedSourceMethod)
                                    }
                                },
                                modifier = Modifier.weight(1.1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GoPayBrightTeal),
                                shape = RoundedCornerShape(8.dp),
                                enabled = currentEffectiveAmount > 0,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Top Up", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
