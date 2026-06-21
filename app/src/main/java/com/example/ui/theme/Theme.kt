package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GoPayBudgetDarkColors = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    background = Background,
    surface = Surface,
    onBackground = OnBackground,
    onSurface = OnSurface,
    outlineVariant = OutlineVariant
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // We enforce our custom GoPay Dark colors as the theme for premium visuals matching the user designs.
    MaterialTheme(
        colorScheme = GoPayBudgetDarkColors,
        typography = Typography,
        content = content
    )
}
