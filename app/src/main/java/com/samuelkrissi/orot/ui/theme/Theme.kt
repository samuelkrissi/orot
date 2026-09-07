package com.samuelkrissi.orot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Ink = Color(0xFF2B2116)
val Parchment = Color(0xFFF7F1E4)
val ParchmentDeep = Color(0xFFE8DCC4)
val Gold = Color(0xFFB8863A)
val GoldSoft = Color(0xFFE8C27A)
val Wine = Color(0xFF6B3E1D)
val Night = Color(0xFF1C1914)
val NightCard = Color(0xFF2A241C)

private val LightColors = lightColorScheme(
    primary = Wine,
    onPrimary = Color.White,
    secondary = Gold,
    onSecondary = Ink,
    background = Parchment,
    onBackground = Ink,
    surface = Parchment,
    onSurface = Ink,
    surfaceVariant = ParchmentDeep,
    onSurfaceVariant = Color(0xFF6F5B45),
    outline = Color(0xFFC9B89A),
)

private val DarkColors = darkColorScheme(
    primary = GoldSoft,
    onPrimary = Night,
    secondary = Gold,
    onSecondary = Night,
    background = Night,
    onBackground = Color(0xFFF3EAD7),
    surface = NightCard,
    onSurface = Color(0xFFF3EAD7),
    surfaceVariant = Color(0xFF3A3228),
    onSurfaceVariant = Color(0xFFC9B89A),
    outline = Color(0xFF6F5B45),
)

@Composable
fun OrotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography.copy(
            headlineLarge = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                lineHeight = 38.sp,
            ),
            headlineMedium = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
            ),
            titleLarge = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 28.sp,
            ),
            bodyLarge = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 17.sp,
                lineHeight = 26.sp,
            ),
            bodyMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            ),
        ),
        content = content,
    )
}
