package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
                text = "Target Tabungan / Budgeting",
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
    onTopUp: (Double) -> Unit,
    onWithdraw: (Double) -> Unit,
    onDelete: () -> Unit,
    onMarkDone: () -> Unit,
    onMarkDoneAutomatically: (String, String) -> Unit,
    onEdit: (String, Double, String) -> Unit
) {
    var amountRaw by remember { mutableStateOf(0L) }
    var amountStr by remember { mutableStateOf("") }
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
    val methods = listOf("Cash", "Wondr", "Dana", "GoPay", "Lainnya")

    var autoMethod by remember { mutableStateOf("") }
    var expandedAutoMethod by remember { mutableStateOf(false) }
    var autoNotes by remember { mutableStateOf(goal.name) }

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
                    message = "Progres tabungan akan dicairkan ke saldo utama.",
                    confirmText = "Hapus",
                    confirmColor = WondrCoralRed,
                    onConfirm = onDelete,
                    onDismiss = { showDeleteConfirm = false }
                )
            } else if (showExceedConfirm) {
                CustomActionDialog(
                    title = "Tabungan Melebihi Target",
                    message = "Jumlah ini akan membuat tabungan melebihi target. Anda ingin lanjut memasukkan semuanya?",
                    confirmText = "Ya, Lanjut",
                    onConfirm = { 
                        onTopUp(amountRaw.toDouble())
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
                        onTopUp(remaining)
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
                            methods.forEach { m ->
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

                    Text(
                        text = "Terkumpul: Rp ${String.format("%,.0f", goal.currentAmount).replace(",", ".")}\n" +
                                if (remaining > 0) "Kurang Rp ${String.format("%,.0f", remaining).replace(",", ".")} lagi!" else "Target Terpenuhi!",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    CustomInputBox(
                        value = amountStr,
                        onValueChange = { input ->
                            val cleanString = input.filter { it.isDigit() }
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
                        modifier = Modifier.fillMaxWidth()
                    )

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
                        Button(
                            onClick = {
                                if (amountRaw > 0 && amountRaw <= goal.currentAmount) onWithdraw(amountRaw.toDouble())
                            },
                            modifier = Modifier.weight(1.1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GoPayDarkBlue),
                            shape = RoundedCornerShape(8.dp),
                            enabled = amountRaw > 0 && amountRaw <= goal.currentAmount,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Tarik", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (remaining > 0) {
                            Button(
                                onClick = {
                                    val topupAmt = amountRaw.toDouble()
                                    if (topupAmt > remaining && remaining > 0) {
                                        showExceedConfirm = true
                                    } else {
                                        if (topupAmt > 0) onTopUp(topupAmt)
                                    }
                                },
                                modifier = Modifier.weight(1.1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GoPayBrightTeal),
                                shape = RoundedCornerShape(8.dp),
                                enabled = amountRaw > 0,
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
