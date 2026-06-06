package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = RoyalGold,
    secondary = PremiumGoldDark,
    tertiary = RoyalMaroon,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = LuxuryBlack,
    onSecondary = PureWhite,
    onBackground = PureWhite,
    onSurface = PureWhite,
    surfaceVariant = DarkCard
)

private val LightColorScheme = lightColorScheme(
    primary = LuxuryBlack,
    secondary = RoyalGold,
    tertiary = RoyalMaroon,
    background = PremiumIvory,
    surface = PureWhite,
    onPrimary = PureWhite,
    onSecondary = LuxuryBlack,
    onBackground = RichCharcoal,
    onSurface = RichCharcoal,
    surfaceVariant = PremiumIvory
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled to strictly honor the luxury gold/black brand color palette
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
