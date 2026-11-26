package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val AntriYukLightColorScheme = lightColorScheme(
    primary = RoseGoldTerracotta,
    onPrimary = PurePorcelain,
    primaryContainer = BlushMist,
    onPrimaryContainer = DeepTerracotta,
    secondary = ChampagneGold,
    onSecondary = PurePorcelain,
    secondaryContainer = ChampagneSoft,
    onSecondaryContainer = EspressoBronze,
    tertiary = EmeraldSage,
    onTertiary = PurePorcelain,
    tertiaryContainer = EmeraldSoft,
    onTertiaryContainer = EmeraldSage,
    background = WarmIvory,
    onBackground = DeepCharcoal,
    surface = WarmIvory,
    onSurface = DeepCharcoal,
    surfaceVariant = WarmAlabaster,
    onSurfaceVariant = MutedTaupe,
    surfaceContainerLowest = PurePorcelain,
    surfaceContainerLow = WarmAlabaster,
    surfaceContainer = WarmSand,
    outline = SilkHairline,
    outlineVariant = SilkHairline,
    error = CrimsonRose,
    onError = PurePorcelain,
    errorContainer = CrimsonSoft,
    onErrorContainer = CrimsonRose
)

private val ColorTokensMutedDark = androidx.compose.ui.graphics.Color(0xFFB8B0AC)

private val AntriYukDarkColorScheme = darkColorScheme(
    primary = RoseGoldLight,
    onPrimary = DeepTerracotta,
    primaryContainer = RoseGoldDark,
    onPrimaryContainer = BlushMist,
    secondary = ChampagneGold,
    onSecondary = DarkSalonBg,
    secondaryContainer = EspressoBronze,
    onSecondaryContainer = ChampagneSoft,
    tertiary = EmeraldSage,
    onTertiary = PurePorcelain,
    background = DarkSalonBg,
    onBackground = IvoryText,
    surface = DarkSalonSurface,
    onSurface = IvoryText,
    surfaceVariant = DarkSalonSurfaceVariant,
    onSurfaceVariant = ColorTokensMutedDark,
    outline = DarkSalonOutline,
    outlineVariant = DarkSalonOutline,
    error = CrimsonRose,
    onError = PurePorcelain
)

val AntriYukShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun AntriYukTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AntriYukDarkColorScheme
        else -> AntriYukLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AntriYukTypography,
        shapes = AntriYukShapes,
        content = content
    )
}
