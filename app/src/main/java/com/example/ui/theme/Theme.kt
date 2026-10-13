package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = BrandPrimaryDark,
  onPrimary = Color.White,
  primaryContainer = BrandPrimaryVariant,
  onPrimaryContainer = Color(0xFFD6E4FF),
  secondary = BrandSecondaryDark,
  onSecondary = Color.Black,
  tertiary = BrandAccentDark,
  onTertiary = Color.Black,
  background = BrandBackgroundDark,
  onBackground = BrandTextPrimaryDark,
  surface = BrandSurfaceDark,
  onSurface = BrandTextPrimaryDark,
  surfaceVariant = BrandSurfaceVariantDark,
  onSurfaceVariant = BrandTextSecondaryDark,
  outline = BrandBorderDark
)

private val LightColorScheme = lightColorScheme(
  primary = BrandPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE0ECFF),
  onPrimaryContainer = BrandPrimaryVariant,
  secondary = BrandSecondary,
  onSecondary = Color.White,
  tertiary = BrandAccent,
  onTertiary = Color.Black,
  background = BrandBackground,
  onBackground = BrandTextPrimary,
  surface = BrandSurface,
  onSurface = BrandTextPrimary,
  surfaceVariant = BrandSurfaceVariant,
  onSurfaceVariant = BrandTextSecondary,
  outline = BrandBorder
)

@Composable
fun RechargePayTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our branded theme for consistent fintech identity
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
