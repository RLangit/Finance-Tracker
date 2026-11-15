package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ItemType
import com.example.data.MetaItem
import com.example.ui.BudgetViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetaEditorDialog(
    itemType: ItemType,
    items: List<MetaItem>,
    onDismiss: () -> Unit,
    viewModel: BudgetViewModel
) {
    var showAddEditDialog by remember { mutableStateOf<MetaItem?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<MetaItem?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
            colors = CardDefaults.cardColors(containerColor = GoPayNavy),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when(itemType) {
                            ItemType.METHOD -> "Metode Pembayaran"
                            ItemType.INCOME_CATEGORY -> "Kategori Pemasukan"
                            ItemType.EXPENSE_CATEGORY -> "Kategori Pengeluaran"
                        },
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, tint = Color.White, contentDescription = "Close")
                    }
                }
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(GoPayDarkBlue, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(
                                            try { Color(android.graphics.Color.parseColor(item.colorHex)) } catch (e: Exception) { Color.Gray }
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(item.name, color = Color.White, fontSize = 14.sp)
                            }
                            Row {
                                IconButton(onClick = { viewModel.moveMetaItemUp(item) }, modifier = Modifier.size(24.dp)) {
                                    Icon(androidx.compose.material.icons.Icons.Default.ArrowUpward, tint = Color.Gray, contentDescription = "Up", modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { viewModel.moveMetaItemDown(item) }, modifier = Modifier.size(24.dp)) {
                                    Icon(androidx.compose.material.icons.Icons.Default.ArrowDownward, tint = Color.Gray, contentDescription = "Down", modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = { showAddEditDialog = item }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Edit, tint = GoPayBrightTeal, contentDescription = "Edit")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { itemToDelete = item }, 
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, tint = WondrCoralRed, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
                
                Button(
                    onClick = { showAdd = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoPayBrightTeal)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Tambah Baru", color = Color.White)
                }
            }
        }
    }
    
    if (showAdd || showAddEditDialog != null) {
        val isEdit = showAddEditDialog != null
        val editItem = showAddEditDialog
        AddEditMetaDialog(
            isEdit = isEdit,
            initialName = editItem?.name ?: "",
            initialColor = editItem?.colorHex ?: "#00A5CF",
            onDismiss = { showAdd = false; showAddEditDialog = null },
            onSave = { name, color ->
                if (name.isNotBlank()) {
                    if (isEdit) {
                        viewModel.updateMetaItem(editItem!!.copy(name = name, colorHex = color))
                    } else {
                        viewModel.addMetaItem(name, itemType, color)
                    }
                    showAdd = false
                    showAddEditDialog = null
                }
            }
        )
    }

    itemToDelete?.let { item ->
        DeleteSyncDialog(
            item = item,
            allAvailableItems = items.filter { it.id != item.id },
            onDismiss = { itemToDelete = null },
            onDeleteOnly = {
                viewModel.deleteMetaItem(item)
                itemToDelete = null
            },
            onDeleteAndSync = { replacement ->
                viewModel.deleteMetaItem(item, replacement.name)
                itemToDelete = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteSyncDialog(
    item: MetaItem,
    allAvailableItems: List<MetaItem>,
    onDismiss: () -> Unit,
    onDeleteOnly: () -> Unit,
    onDeleteAndSync: (MetaItem) -> Unit
) {
    var selectedReplacement by remember { mutableStateOf<MetaItem?>(allAvailableItems.find { it.name == "Lainnya" } ?: allAvailableItems.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GoPayNavy),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Hapus ${item.name}?",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Terdapat data transaksi lama yang menggunakan '${item.name}'. Apa yang ingin Anda lakukan?",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                if (allAvailableItems.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Pindahkan data ke:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedReplacement?.name ?: "Pilih...",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = GoPayDarkBlue,
                                    unfocusedContainerColor = GoPayDarkBlue,
                                    focusedBorderColor = GoPayBrightTeal,
                                    unfocusedBorderColor = OutlineVariant
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(GoPayNavy)
                            ) {
                                allAvailableItems.forEach { replacement ->
                                    DropdownMenuItem(
                                        text = { Text(replacement.name, color = Color.White) },
                                        onClick = {
                                            selectedReplacement = replacement
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { selectedReplacement?.let { onDeleteAndSync(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoPayBrightTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hapus & Pindahkan Data", fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(
                    onClick = onDeleteOnly,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = WondrCoralRed)
                ) {
                    Text("Hapus Saja (Data jadi 'Ghost')")
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Batal")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMetaDialog(
    isEdit: Boolean,
    initialName: String,
    initialColor: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var colorHex by remember { mutableStateOf(initialColor) }
    
    val defaultColors = listOf(
        "#00BFA5", "#2196F3", "#9E9E9E", "#FF5252", "#FFC107", 
        "#8BC34A", "#3F51B5", "#9C27B0", "#FF9800", "#4CAF50",
        "#00A5CF", "#FF6D00", "#F15BB5", "#9B5DE5", "#2A9D8F",
        "#F4A261", "#E76F51", "#80ED99", "#48CAE4", "#001F3F"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GoPayDarkBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    if (isEdit) "Edit Item" else "Tambah Item", 
                    color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoPayBrightTeal,
                        unfocusedBorderColor = OutlineVariant,
                        focusedLabelColor = GoPayBrightTeal,
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = GoPayBrightTeal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Pilih Warna", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Color grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in 0 until 4) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            for (col in 0 until 5) {
                                val idx = row * 5 + col
                                if (idx < defaultColors.size) {
                                    val c = defaultColors[idx]
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(c)))
                                            .border(
                                                width = if (c.equals(colorHex, true)) 3.dp else 0.dp,
                                                color = if (c.equals(colorHex, true)) Color.White else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { colorHex = c }
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = Color.Gray)
                    }
                    Button(
                        onClick = { onSave(name, colorHex) }, 
                        colors = ButtonDefaults.buttonColors(containerColor = GoPayBrightTeal)
                    ) {
                        Text("Simpan", color = Color.White)
                    }
                }
            }
        }
    }
}
