package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = SakuSageAccent,
    onPrimary = Color.Black,
    primaryContainer = SakuMediumGreen,
    onPrimaryContainer = Color.White,
    secondary = SakuGoldAccent,
    onSecondary = Color.Black,
    background = SakuDarkBackground,
    onBackground = SakuDarkTextPrimary,
    surface = SakuDarkSurface,
    onSurface = SakuDarkTextPrimary,
    surfaceVariant = SakuDarkSurfaceVariant,
    onSurfaceVariant = SakuDarkTextSecondary,
    outline = SakuDarkBorder,
    error = SakuExpenseRed,
    onError = Color.White,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SakuDarkGreen,
    onPrimary = Color.White,
    primaryContainer = SakuMediumGreen,
    onPrimaryContainer = Color.White,
    secondary = SakuSageAccent,
    onSecondary = Color.White,
    secondaryContainer = SakuLightGreen,
    onSecondaryContainer = SakuDarkGreenDeep,
    tertiary = SakuGoldAccent,
    onTertiary = Color.White,
    tertiaryContainer = SakuGoldLight,
    onTertiaryContainer = SakuDarkGreenDeep,
    background = SakuCreamBackground,
    onBackground = SakuTextPrimary,
    surface = SakuCreamSurface,
    onSurface = SakuTextPrimary,
    surfaceVariant = SakuCreamSurfaceVariant,
    onSurfaceVariant = SakuTextSecondary,
    outline = SakuCreamBorder,
    outlineVariant = SakuCreamSurfaceVariant,
    error = SakuExpenseRed,
    onError = Color.White,
    errorContainer = SakuExpenseRedBg,
    onErrorContainer = SakuExpenseRed,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Keep Saku branded palette true to user prompt (cream & dark green)
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

