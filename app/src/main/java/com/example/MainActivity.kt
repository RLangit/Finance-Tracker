package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.BudgetViewModel
import com.example.ui.screens.*
import com.example.ui.theme.GoPayBrightTeal
import com.example.ui.theme.GoPayDarkBlue
import com.example.ui.theme.GoPayNavy
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.foundation.border
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.foundation.shape.RoundedCornerShape

val LocalNotification = compositionLocalOf<((String) -> Unit)?> { null }

class MainActivity : ComponentActivity() {

    private val viewModel: BudgetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Prompt POST_NOTIFICATIONS runtime permission for Android 13+ devices
        checkAndRequestPushPermissions()

        setContent {
            MyApplicationTheme {
                MainScreenHolder(viewModel)
            }
        }
    }

    private fun checkAndRequestPushPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 302)
            }
        }
    }
}

@Composable
fun MainScreenHolder(viewModel: BudgetViewModel) {
    var selectedIndex by remember { mutableStateOf(0) }
    
    // Notification state
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val showNotification: (String) -> Unit = { message ->
        notificationMessage = message
        scope.launch {
            delay(2500)
            if (notificationMessage == message) {
                notificationMessage = null
            }
        }
    }

    // Empty database completely if requested, handled via generic fresh install logic
    val transactions by viewModel.allTransactions.collectAsState()

    CompositionLocalProvider(LocalNotification provides showNotification) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(GoPayDarkBlue),
            bottomBar = {
            // High-fidelity GoPay-styled Bottom Navigation Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GoPayNavy)
                    .border(1.dp, OutlineVariant)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Item 1: Beranda
                    BottomNavItem(
                        icon = if (selectedIndex == 0) Icons.Filled.Home else Icons.Outlined.Home,
                        label = "Beranda",
                        isSelected = selectedIndex == 0,
                        onClick = { selectedIndex = 0 }
                    )

                    // Item 2: Keuangan
                    BottomNavItem(
                        icon = if (selectedIndex == 1) Icons.Filled.Wallet else Icons.Outlined.Wallet,
                        label = "Keuangan",
                        isSelected = selectedIndex == 1,
                        onClick = { selectedIndex = 1 }
                    )

                    // Item 3: Riwayat
                    BottomNavItem(
                        icon = if (selectedIndex == 2) Icons.Filled.History else Icons.Outlined.History,
                        label = "Riwayat",
                        isSelected = selectedIndex == 2,
                        onClick = { selectedIndex = 2 }
                    )

                    // Item 4: Profil
                    BottomNavItem(
                        icon = if (selectedIndex == 3) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                        label = "Profil",
                        isSelected = selectedIndex == 3,
                        onClick = { selectedIndex = 3 }
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedIndex) {
                0 -> DashboardScreen(viewModel = viewModel)
                1 -> ReportScreen(viewModel = viewModel)
                2 -> HistoryScreen(viewModel = viewModel)
                3 -> ProfileScreen(viewModel = viewModel)
            }
            
            // Dynamic Island Notification overlay
            AnimatedVisibility(
                visible = notificationMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, "Success", tint = GoPayBrightTeal, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = notificationMessage ?: "",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun RowScope.BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) GoPayBrightTeal else Color.Gray,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = if (isSelected) GoPayBrightTeal else Color.Gray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
        )
    }
}
