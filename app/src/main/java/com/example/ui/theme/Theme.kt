package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = StoreFlowDarkPrimary,
    onPrimary = StoreFlowDarkOnPrimary,
    primaryContainer = StoreFlowDarkPrimaryContainer,
    onPrimaryContainer = StoreFlowDarkOnPrimaryContainer,
    secondary = StoreFlowDarkSecondary,
    onSecondary = StoreFlowDarkOnSecondaryContainer,
    secondaryContainer = StoreFlowDarkSecondaryContainer,
    onSecondaryContainer = StoreFlowDarkOnSecondaryContainer,
    background = StoreFlowDarkBackground,
    onBackground = StoreFlowDarkOnSurface,
    surface = StoreFlowDarkSurface,
    onSurface = StoreFlowDarkOnSurface
  )

private val LightColorScheme =
  lightColorScheme(
    primary = StoreFlowPrimary,
    onPrimary = StoreFlowOnPrimary,
    primaryContainer = StoreFlowPrimaryContainer,
    onPrimaryContainer = StoreFlowOnPrimaryContainer,
    secondary = StoreFlowSecondary,
    onSecondary = StoreFlowOnPrimary,
    secondaryContainer = StoreFlowSecondaryContainer,
    onSecondaryContainer = StoreFlowOnSecondaryContainer,
    background = StoreFlowBackground,
    onBackground = StoreFlowOnSurface,
    surface = StoreFlowSurface,
    onSurface = StoreFlowOnSurface,
    onSurfaceVariant = StoreFlowOnSurfaceVariant,
    outline = StoreFlowOutline,
    outlineVariant = StoreFlowOutlineVariant
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set to false to preserve exact custom Professional Polish theme colors
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
