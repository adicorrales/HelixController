package adco.helixmidimixer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val HelixDarkColorScheme = darkColorScheme(
    primary = HelixGreenPrimary,
    onPrimary = HelixBackground,
    primaryContainer = HelixGreenHeader,
    onPrimaryContainer = HelixTextPrimary,
    secondary = HelixBluePrimary,
    onSecondary = HelixTextPrimary,
    secondaryContainer = HelixBlueHeader,
    onSecondaryContainer = HelixTextPrimary,
    tertiary = HelixSilverPrimary,
    onTertiary = HelixBackground,
    background = HelixBackground,
    onBackground = HelixTextPrimary,
    surface = HelixSurface,
    onSurface = HelixTextPrimary,
    surfaceVariant = HelixSurfaceVariant,
    onSurfaceVariant = HelixTextSecondary,
    outline = HelixCardBorder
)

@Composable
fun HelixMidiMixerTheme(
    darkTheme: Boolean = true, // Default to true for dark matrix style
    content: @Composable () -> Unit
) {
    val colorScheme = HelixDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                window.statusBarColor = HelixBackground.toArgb()
                window.navigationBarColor = HelixBackground.toArgb()
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
