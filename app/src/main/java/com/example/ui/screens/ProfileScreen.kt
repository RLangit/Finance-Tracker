package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.window.Dialog
import com.example.ui.BudgetViewModel
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val gmailUser by viewModel.gmailAccount
    val lastBackupText by viewModel.lastBackupTime
    val isBackupRunning by viewModel.isBackupInProgress
    val metaItems by viewModel.metaItems.collectAsState()
    
    var showMetaEditor by remember { mutableStateOf<com.example.data.ItemType?>(null) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    var lastAttemptedEmail by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GoPayDarkBlue)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Upper Avatar block
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = GoPayNavy),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(GoPayBrightTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (gmailUser != null) gmailUser!!.take(2).uppercase() else "GP",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (gmailUser != null) "User Active" else "Guest Profile",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = gmailUser ?: "Belum terhubung, silahkan login",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                    
                    if (gmailUser == null) {
                        Button(
                            onClick = { showAuthDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GoPayBrightTeal),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Login / Daftar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Sign out option for debugging/testing
                        Text("Terhubung", fontSize = 12.sp, color = WondrEmeraldGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Feature 1: Saving with Gmail (Cloud backup dashboard)
        item {
            Text(
                text = "Backup & Amankan Data (Gmail)",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = "Cloud Icon",
                                tint = GoPayBrightTeal,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Simpan Data via Google Account",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = gmailUser?.let { "Terhubung dengan: $it" } ?: "Belum terhubung dengan akun Gmail",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(WondrEmeraldGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "AKTIF",
                                color = WondrEmeraldGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 14.dp), color = OutlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Status Pencadangan", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                text = lastBackupText,
                                color = if (lastBackupText.contains("Berhasil")) WondrEmeraldGreen else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (isBackupRunning) {
                            CircularProgressIndicator(
                                color = GoPayBrightTeal,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Button(
                                onClick = { 
                                    if (gmailUser != null) {
                                        viewModel.performGmailBackup() 
                                    } else {
                                        Toast.makeText(context, "Silahkan login terlebih dahulu", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (gmailUser != null) GoPayBrightTeal else Color.Gray
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("backup_button")
                            ) {
                                Text("Cadangkan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Feature: Kategori dan Metode
        item {
            Text(
                text = "Pengaturan Kategori & Metode",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, OutlineVariant, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = GoPayNavy),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showMetaEditor = com.example.data.ItemType.METHOD }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Wallet, contentDescription = null, tint = GoPayBrightTeal)
                        Spacer(Modifier.width(12.dp))
                        Text("Metode Pembayaran", color = Color.White, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                    Divider(color = OutlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showMetaEditor = com.example.data.ItemType.INCOME_CATEGORY }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = WondrEmeraldGreen)
                        Spacer(Modifier.width(12.dp))
                        Text("Kategori Pemasukan", color = Color.White, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                    Divider(color = OutlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showMetaEditor = com.example.data.ItemType.EXPENSE_CATEGORY }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.TrendingDown, contentDescription = null, tint = WondrCoralRed)
                        Spacer(Modifier.width(12.dp))
                        Text("Kategori Pengeluaran", color = Color.White, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }
        }

        // Feature 2: Auto-import from other wallets configuration
        item {
            Text(
                text = "Auto-Sync Integrasi Dompet (Dana / Wondr)",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cara Kerja Auto-Sync:",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(WondrEmeraldGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "AKTIF",
                                color = WondrEmeraldGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aplikasi akan mendengarkan (listen) push notification transaksi dari Dana & Wondr. Notifikasi akan diproses otomatis dan disimpan ke database lokal tanpa perlu input manual!",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "Area Berbahaya",
                color = WondrCoralRed,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            var showLogoutFirst by remember { mutableStateOf(false) }
            var showLogoutSecond by remember { mutableStateOf(false) }
            var showDeleteFirst by remember { mutableStateOf(false) }
            var showDeleteSecond by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WondrCoralRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = GoPayNavy),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Logout Section
                    if (gmailUser != null) {
                        Column {
                            Text(
                                text = "Keluar dari Akun",
                                color = WondrCoralRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Anda akan keluar dari akun $gmailUser. Pastikan data sudah dicadangkan.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showLogoutFirst = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = WondrCoralRed.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Logout", color = WondrCoralRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Divider(color = OutlineVariant.copy(alpha = 0.5f))
                    }

                    // Delete Data Section
                    Column {
                        Text(
                            text = "Hapus Semua Data",
                            color = WondrCoralRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tindakan ini akan menghapus seluruh data transaksi dalam aplikasi. Jika Anda belum melakukan pencadangan data melalui akun email, maka data tidak dapat dipulihkan.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showDeleteFirst = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = WondrCoralRed.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Hapus Data", color = WondrCoralRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Logout Confirmation Dialogs
            if (showLogoutFirst) {
                CustomActionDialog(
                    title = "Konfirmasi Logout",
                    message = "Apakah Anda yakin ingin keluar dari akun?",
                    confirmText = "Lanjutkan",
                    confirmColor = WondrCoralRed,
                    onConfirm = {
                        showLogoutFirst = false
                        showLogoutSecond = true
                    },
                    onDismiss = { showLogoutFirst = false }
                )
            }

            if (showLogoutSecond) {
                CustomActionDialog(
                    title = "Peringatan Terakhir",
                    message = "Pastikan semua data penting sudah dicadangkan sebelum keluar.",
                    confirmText = "Logout Sekarang",
                    confirmColor = WondrCoralRed,
                    onConfirm = {
                        auth.signOut()
                        showLogoutSecond = false
                    },
                    onDismiss = { showLogoutSecond = false }
                )
            }

            // Delete Confirmation Dialogs
            if (showDeleteFirst) {
                CustomActionDialog(
                    title = "Peringatan Hapus Data",
                    message = "Apakah Anda yakin ingin menghapus SEMUA data? Langkah ini tidak bisa dibatalkan.",
                    confirmText = "Ya, Lanjutkan",
                    confirmColor = WondrCoralRed,
                    onConfirm = {
                        showDeleteFirst = false
                        showDeleteSecond = true
                    },
                    onDismiss = { showDeleteFirst = false }
                )
            }

            if (showDeleteSecond) {
                CustomActionDialog(
                    title = "Konfirmasi Terakhir",
                    message = "Data Anda akan dihapus permanen sekarang. Lanjutkan?",
                    confirmText = "Hapus Permanen",
                    confirmColor = WondrCoralRed,
                    onConfirm = {
                        viewModel.deleteAllData()
                        showDeleteSecond = false
                    },
                    onDismiss = { showDeleteSecond = false }
                )
            }
        }
    }
    
    showMetaEditor?.let { itemType ->
        MetaEditorDialog(
            itemType = itemType,
            items = metaItems.filter { it.type == itemType },
            onDismiss = { showMetaEditor = null },
            viewModel = viewModel
        )
    }

    if (showAuthDialog) {
        AuthDialog(
            viewModel = viewModel,
            onDismiss = { showAuthDialog = false },
            onAuthSuccess = { showAuthDialog = false },
            onError = { msg, email -> 
                authError = msg
                lastAttemptedEmail = email
            }
        )
    }

    authError?.let { message ->
        val isNotRegistered = message.contains("Tidak terdaftar", ignoreCase = true)
        val isWrongPassword = message.contains("Password salah", ignoreCase = true)
        var showResetInfo by remember { mutableStateOf(false) }
        
        val displayMessage = when {
            isNotRegistered -> "Email tersebut belum terdaftar. Silahkan daftarkan email anda terlebih dahulu."
            isWrongPassword -> "Password atau Email anda salah. Silahkan coba lagi."
            else -> message
        }

        CustomActionDialog(
            title = "Autentikasi Gagal",
            message = displayMessage,
            confirmText = if (isNotRegistered) "Register" else "Coba Lagi",
            onConfirm = {
                authError = null
                showAuthDialog = true
            },
            dismissText = if (isWrongPassword) "Lupa Password?" else "Batal",
            onDismiss = {
                if (isWrongPassword) {
                    showResetInfo = true
                } else {
                    authError = null
                }
            }
        )

        if (showResetInfo) {
            CustomActionDialog(
                title = "Reset Password",
                message = "Kami akan mengirimkan link reset password ke $lastAttemptedEmail. Your reset password letter might be sitting in your 'SPAM' email, go ahead and check it.",
                confirmText = "Kirim Email",
                onConfirm = {
                    if (lastAttemptedEmail.isNotEmpty()) {
                        auth.sendPasswordResetEmail(lastAttemptedEmail)
                            .addOnCompleteListener { resetTask ->
                                if (resetTask.isSuccessful) {
                                    Toast.makeText(context, "Jika akun terdaftar, link reset telah dikirim ke email anda.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Gagal memproses permintaan: ${resetTask.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                    showResetInfo = false
                    authError = null
                },
                onDismiss = { 
                    showResetInfo = false
                    authError = null
                }
            )
        }
    }
}

fun isPasswordValid(password: String): Boolean {
    val hasUpperCase = password.any { it.isUpperCase() }
    val hasLowerCase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    return password.length >= 6 && hasUpperCase && hasLowerCase && hasDigit
}

@Composable
fun AuthDialog(
    viewModel: BudgetViewModel,
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit,
    onError: (String, String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val passwordValid = !isSignUp || isPasswordValid(password)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1F26)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isSignUp) "Daftar Akun Baru" else "Masuk ke Akun",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                CustomInputBox(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    modifier = Modifier.fillMaxWidth()
                )

                CustomInputBox(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                tint = Color.Gray
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (isSignUp && password.isNotEmpty() && !passwordValid) {
                    Text(
                        text = "Password minimal 6 karakter, mengandung huruf besar, kecil, dan angka.",
                        color = Color.Red,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                TextButton(
                    onClick = { isSignUp = !isSignUp },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = if (isSignUp) "Sudah punya akun? Login" else "Belum punya akun? Daftar",
                        color = GoPayBrightTeal
                    )
                }

                Button(
                    onClick = {
                        isLoading = true
                        scope.launch {
                            try {
                                val oldUid = auth.currentUser?.uid
                                if (isSignUp) {
                                    auth.createUserWithEmailAndPassword(email, password).await()
                                    Toast.makeText(context, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()
                                    // Move data from anonymous to new email account and clear old cloud space
                                    viewModel.handleRegisterMigration(oldUid)
                                } else {
                                    auth.signInWithEmailAndPassword(email, password).await()
                                    Toast.makeText(context, "Login berhasil! Mensinkronisasi data...", Toast.LENGTH_SHORT).show()
                                    // Pull cloud data, merge with local anonymous data, and clear old cloud space
                                    viewModel.handleLoginMerge()
                                    if (oldUid != null && oldUid != auth.currentUser?.uid) {
                                        // Optional: Additional check to clean up if IDs changed
                                        // The handleLoginMerge already does backupToCloudInternal which saves to new UID
                                    }
                                }
                                onAuthSuccess()
                            } catch (e: Exception) {
                                val errorMsg = when (e) {
                                    is FirebaseAuthInvalidUserException -> "Email tidak terdaftar."
                                    is FirebaseAuthInvalidCredentialsException -> {
                                        // "invalid-credential" is used for wrong password in newer Firebase versions
                                        if (e.errorCode == "ERROR_WRONG_PASSWORD" || e.message?.contains("credential") == true) {
                                            "Password salah."
                                        } else {
                                            "Email atau Password salah."
                                        }
                                    }
                                    is FirebaseAuthException -> {
                                        when (e.errorCode) {
                                            "ERROR_USER_NOT_FOUND" -> "Email tidak terdaftar."
                                            "ERROR_WRONG_PASSWORD" -> "Password salah."
                                            "ERROR_EMAIL_ALREADY_IN_USE" -> "Email sudah digunakan akun lain."
                                            "ERROR_WEAK_PASSWORD" -> "Password terlalu lemah."
                                            else -> "Gagal menghubungkan ke server. (${e.errorCode})"
                                        }
                                    }
                                    else -> "Gagal menghubungkan ke server. Silahkan cek koneksi internet."
                                }
                                onError(errorMsg, email)
                                onDismiss()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoPayBrightTeal),
                    shape = RoundedCornerShape(28.dp),
                    enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading && passwordValid
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text(if (isSignUp) "Daftar" else "Login", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

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

