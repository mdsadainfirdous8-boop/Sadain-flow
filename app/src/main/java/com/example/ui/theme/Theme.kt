package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.data.ThemeMode

@Immutable
data class HisabColors(
    val credit: Color,
    val creditContainer: Color,
    val onCreditContainer: Color,
    val debit: Color,
    val debitContainer: Color,
    val onDebitContainer: Color,
    val heroStart: Color,
    val heroEnd: Color,
    val isDark: Boolean
)

val LocalHisabColors = staticCompositionLocalOf {
    HisabColors(
        credit = CreditGreenLight,
        creditContainer = CreditGreenContainerLight,
        onCreditContainer = OnCreditGreenContainerLight,
        debit = DebitRedLight,
        debitContainer = DebitRedContainerLight,
        onDebitContainer = OnDebitRedContainerLight,
        heroStart = HeroGradientStartLight,
        heroEnd = HeroGradientEndLight,
        isDark = false
    )
}

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimaryLight,
    onPrimary = EmeraldOnPrimaryLight,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = EmeraldOnContainerLight,
    secondary = SlateSecondaryLight,
    onSecondary = SlateOnSecondaryLight,
    secondaryContainer = SlateContainerLight,
    onSecondaryContainer = SlateOnContainerLight,
    tertiary = AmberTertiaryLight,
    onTertiary = AmberOnTertiaryLight,
    tertiaryContainer = AmberContainerLight,
    onTertiaryContainer = AmberOnContainerLight,
    error = DebitRedLight,
    onError = Color.White,
    errorContainer = DebitRedContainerLight,
    onErrorContainer = OnDebitRedContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineLight.copy(alpha = 0.5f)
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = EmeraldOnPrimaryDark,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = EmeraldOnContainerDark,
    secondary = SlateSecondaryDark,
    onSecondary = SlateOnSecondaryDark,
    secondaryContainer = SlateContainerDark,
    onSecondaryContainer = SlateOnContainerDark,
    tertiary = AmberTertiaryDark,
    onTertiary = AmberOnTertiaryDark,
    tertiaryContainer = AmberContainerDark,
    onTertiaryContainer = AmberOnContainerDark,
    error = DebitRedDark,
    onError = Color(0xFF450A0A),
    errorContainer = DebitRedContainerDark,
    onErrorContainer = OnDebitRedContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineDark.copy(alpha = 0.6f)
)

private val HisabShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyDailyHisabTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val hisabColors = if (darkTheme) {
        HisabColors(
            credit = CreditGreenDark,
            creditContainer = CreditGreenContainerDark,
            onCreditContainer = OnCreditGreenContainerDark,
            debit = DebitRedDark,
            debitContainer = DebitRedContainerDark,
            onDebitContainer = OnDebitRedContainerDark,
            heroStart = HeroGradientStartDark,
            heroEnd = HeroGradientEndDark,
            isDark = true
        )
    } else {
        HisabColors(
            credit = CreditGreenLight,
            creditContainer = CreditGreenContainerLight,
            onCreditContainer = OnCreditGreenContainerLight,
            debit = DebitRedLight,
            debitContainer = DebitRedContainerLight,
            onDebitContainer = OnDebitRedContainerLight,
            heroStart = HeroGradientStartLight,
            heroEnd = HeroGradientEndLight,
            isDark = false
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalHisabColors provides hisabColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HisabTypography,
            shapes = HisabShapes,
            content = content
        )
    }
}
