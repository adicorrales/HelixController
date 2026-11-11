package adco.helixmidimixer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import adco.helixmidimixer.ui.theme.HelixBadgeBg
import adco.helixmidimixer.ui.theme.HelixCardBorder
import adco.helixmidimixer.ui.theme.HelixGreenPrimary
import adco.helixmidimixer.ui.theme.HelixMidiMixerTheme
import adco.helixmidimixer.ui.theme.HelixSurface
import adco.helixmidimixer.ui.theme.HelixTextPrimary

@Composable
fun TransportBar(
    isPlaying: Boolean,
    onPrev: () -> Unit,
    onReturnToZero: () -> Unit,
    onPlayToggle: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenSettings: (() -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val isCompact = configuration.smallestScreenWidthDp < 600

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .background(HelixSurface)
            .border(width = 1.dp, color = HelixCardBorder),
        color = HelixSurface,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isCompact) 8.dp else 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prev Button
                TransportButton(
                    label = "PREV",
                    icon = Icons.Rounded.SkipPrevious,
                    compact = isCompact,
                    onClick = onPrev
                )

                // Return to 0 Button
                TransportButton(
                    label = "RT0",
                    icon = Icons.Rounded.Replay,
                    compact = isCompact,
                    onClick = onReturnToZero
                )

                // Play Button (styled with green accent when active/pressed)
                TransportButton(
                    label = if (isPlaying) "PAUSE" else "PLAY",
                    icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    isActive = isPlaying,
                    activeColor = HelixGreenPrimary,
                    compact = isCompact,
                    onClick = onPlayToggle
                )

                // Next Button
                TransportButton(
                    label = "NEXT",
                    icon = Icons.Rounded.SkipNext,
                    compact = isCompact,
                    onClick = onNext
                )

                // Transport / Helix Song Settings Button
                if (onOpenSettings != null) {
                    TransportButton(
                        label = "SET",
                        icon = Icons.Rounded.Settings,
                        compact = isCompact,
                        onClick = onOpenSettings
                    )
                }
            }
        }
    }
}

@Composable
private fun TransportButton(
    label: String,
    icon: ImageVector,
    isActive: Boolean = false,
    activeColor: Color = HelixGreenPrimary,
    compact: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (isActive) activeColor else HelixBadgeBg
    val contentColor = if (isActive) Color.White else HelixTextPrimary
    val borderColor = if (isActive) activeColor else HelixCardBorder

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = if (compact) 8.dp else 16.dp, vertical = if (compact) 8.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(if (compact) 16.dp else 20.dp)
        )
        Spacer(modifier = Modifier.width(if (compact) 4.dp else 6.dp))
        Text(
            text = label,
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Clip,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = contentColor,
                fontSize = if (compact) 10.sp else 12.sp
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TransportBarPreview() {
    HelixMidiMixerTheme {
        TransportBar(
            isPlaying = false,
            onPrev = {},
            onReturnToZero = {},
            onPlayToggle = {},
            onNext = {}
        )
    }
}
