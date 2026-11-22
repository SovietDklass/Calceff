package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.fx.EffectMode

@Immutable
data class FxThemeColors(
  val isDark: Boolean,
  val backgroundTop: Color,
  val backgroundBottom: Color,
  val displayContainer: Color,
  val keyDigitContainer: Color,
  val keyUtilityContainer: Color,
  val keyOperatorContainer: Color,
  val keyBorder: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val accentPrimary: Color,
  val accentSecondary: Color,
)

val LocalFxColors = staticCompositionLocalOf {
  FxThemeColors(
    isDark = true,
    backgroundTop = DarkBackgroundTop,
    backgroundBottom = DarkBackgroundBottom,
    displayContainer = DarkDisplayContainer,
    keyDigitContainer = DarkKeyDigit,
    keyUtilityContainer = DarkKeyUtility,
    keyOperatorContainer = DarkKeyOperator,
    keyBorder = DarkKeyBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    accentPrimary = NeonCyan,
    accentSecondary = NeonMagenta,
  )
}

@Composable
fun FxCalculatorTheme(
  darkTheme: Boolean,
  effectMode: EffectMode,
  content: @Composable () -> Unit,
) {
  val accentPrimary = effectMode.primaryColor(darkTheme)
  val accentSecondary = effectMode.secondaryColor(darkTheme)

  val fxColors = if (darkTheme) {
    FxThemeColors(
      isDark = true,
      backgroundTop = DarkBackgroundTop,
      backgroundBottom = DarkBackgroundBottom,
      displayContainer = DarkDisplayContainer,
      keyDigitContainer = DarkKeyDigit,
      keyUtilityContainer = DarkKeyUtility,
      keyOperatorContainer = accentPrimary.copy(alpha = 0.14f),
      keyBorder = DarkKeyBorder,
      textPrimary = DarkTextPrimary,
      textSecondary = DarkTextSecondary,
      accentPrimary = accentPrimary,
      accentSecondary = accentSecondary,
    )
  } else {
    FxThemeColors(
      isDark = false,
      backgroundTop = LightBackgroundTop,
      backgroundBottom = LightBackgroundBottom,
      displayContainer = LightDisplayContainer,
      keyDigitContainer = LightKeyDigit,
      keyUtilityContainer = LightKeyUtility,
      keyOperatorContainer = accentPrimary.copy(alpha = 0.12f),
      keyBorder = LightKeyBorder,
      textPrimary = LightTextPrimary,
      textSecondary = LightTextSecondary,
      accentPrimary = accentPrimary,
      accentSecondary = accentSecondary,
    )
  }

  val colorScheme = if (darkTheme) {
    darkColorScheme(
      primary = accentPrimary,
      onPrimary = Color.Black,
      secondary = accentSecondary,
      onSecondary = Color.White,
      background = DarkBackgroundTop,
      onBackground = DarkTextPrimary,
      surface = DarkSurface,
      onSurface = DarkTextPrimary,
      surfaceVariant = DarkSurfaceElevated,
      onSurfaceVariant = DarkTextSecondary,
      outline = DarkKeyBorder,
      error = ErrorCrimson,
      onError = Color.White,
    )
  } else {
    lightColorScheme(
      primary = accentPrimary,
      onPrimary = Color.White,
      secondary = accentSecondary,
      onSecondary = Color.White,
      background = LightBackgroundTop,
      onBackground = LightTextPrimary,
      surface = LightSurface,
      onSurface = LightTextPrimary,
      surfaceVariant = LightSurfaceElevated,
      onSurfaceVariant = LightTextSecondary,
      outline = LightKeyBorder,
      error = ErrorCrimson,
      onError = Color.White,
    )
  }

  CompositionLocalProvider(LocalFxColors provides fxColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = AppTypography,
      content = content,
    )
  }
}
