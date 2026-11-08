package adco.helixmidimixer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import adco.helixmidimixer.ui.theme.HelixBadgeBg
import adco.helixmidimixer.ui.theme.HelixBadgeText
import adco.helixmidimixer.ui.theme.HelixCardBorder
import adco.helixmidimixer.ui.theme.HelixGreenPrimary
import adco.helixmidimixer.ui.theme.HelixMidiMixerTheme
import adco.helixmidimixer.ui.theme.HelixSurface
import adco.helixmidimixer.ui.theme.HelixTextPrimary
import adco.helixmidimixer.ui.theme.HelixTextSecondary

@Composable
fun VerticalFader(
    value: Int, // 0..127
    volumeCc: Int,
    onValueChange: (Int) -> Unit,
    accentColor: Color = HelixGreenPrimary,
    isMuted: Boolean = false,
    modifier: Modifier = Modifier
) {
    var componentHeightPx by remember { mutableFloatStateOf(1f) }

    // Convert 0..127 value to dB display string
    val dbText = remember(value) {
        when {
            value == 0 -> "-inf dB"
            value == 100 -> "0.0 dB"
            value > 100 -> {
                val db = (value - 100) * (6.0 / 27.0)
                "+${String.format("%.1f", db)} dB"
            }
            else -> {
                val db = (100 - value) * (-60.0 / 100.0)
                "${String.format("%.1f", db)} dB"
            }
        }
    }

    Column(
        modifier = modifier.width(54.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Volume dB Readout Text
        Text(
            text = dbText,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMuted) Color(0xFFE53E3E) else HelixTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Fader Canvas Track & Knob Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    if (coordinates.size.height > 0) {
                        componentHeightPx = coordinates.size.height.toFloat()
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (componentHeightPx > 0) {
                            val knobMargin = 20.dp.toPx()
                            val usableHeight = (componentHeightPx - 2 * knobMargin).coerceAtLeast(1f)
                            val clampedY = (offset.y - knobMargin).coerceIn(0f, usableHeight)
                            val fraction = 1f - (clampedY / usableHeight)
                            val newValue = (fraction * 127f).toInt().coerceIn(0, 127)
                            onValueChange(newValue)
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, _ ->
                        change.consume()
                        if (componentHeightPx > 0) {
                            val knobMargin = 20.dp.toPx()
                            val usableHeight = (componentHeightPx - 2 * knobMargin).coerceAtLeast(1f)
                            val clampedY = (change.position.y - knobMargin).coerceIn(0f, usableHeight)
                            val fraction = 1f - (clampedY / usableHeight)
                            val newValue = (fraction * 127f).toInt().coerceIn(0, 127)
                            onValueChange(newValue)
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val knobMargin = 20.dp.toPx()
                val usableHeight = (height - 2 * knobMargin).coerceAtLeast(1f)

                val centerX = width / 2f
                val clampedValue = value.coerceIn(0, 127)
                val fraction = clampedValue / 127f
                val knobCenterY = knobMargin + usableHeight * (1f - fraction)

                // 1. Draw Vertical Fader Track Slot
                val trackWidth = 8.dp.toPx()
                val trackLeft = centerX - trackWidth / 2f
                val trackTop = knobMargin
                val trackBottom = height - knobMargin

                drawRoundRect(
                    color = Color(0xFF0F1115),
                    topLeft = Offset(trackLeft, trackTop),
                    size = Size(trackWidth, usableHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                drawRoundRect(
                    color = HelixCardBorder,
                    topLeft = Offset(trackLeft, trackTop),
                    size = Size(trackWidth, usableHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )

                // 2. Draw Active Fill Line (from bottom of track up to knob)
                val fillTop = knobCenterY
                val fillHeight = trackBottom - fillTop
                if (fillHeight > 0) {
                    drawRoundRect(
                        color = if (isMuted) Color(0xFF9B2C2C) else accentColor.copy(alpha = 0.8f),
                        topLeft = Offset(trackLeft + 1.dp.toPx(), fillTop),
                        size = Size(trackWidth - 2.dp.toPx(), fillHeight),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }

                // 3. Draw Side Tick Marks (dB Scale markers)
                val tickPositions = listOf(
                    1.0f to "+6",
                    100f / 127f to "0",
                    80f / 127f to "-6",
                    60f / 127f to "-12",
                    40f / 127f to "-24",
                    20f / 127f to "-36",
                    0.0f to "-u"
                )

                tickPositions.forEach { (tickFrac, _) ->
                    val tickY = knobMargin + usableHeight * (1f - tickFrac)
                    drawLine(
                        color = HelixCardBorder,
                        start = Offset(4.dp.toPx(), tickY),
                        end = Offset(trackLeft - 3.dp.toPx(), tickY),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = HelixCardBorder,
                        start = Offset(trackLeft + trackWidth + 3.dp.toPx(), tickY),
                        end = Offset(width - 4.dp.toPx(), tickY),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 4. Draw Line 6 Helix Metallic Fader Knob
                val knobWidth = 42.dp.toPx()
                val knobHeight = 24.dp.toPx()
                val knobLeft = centerX - knobWidth / 2f
                val knobTop = knobCenterY - knobHeight / 2f

                drawRoundRect(
                    color = Color(0xFF2A2E38),
                    topLeft = Offset(knobLeft, knobTop),
                    size = Size(knobWidth, knobHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFF64748B),
                    topLeft = Offset(knobLeft, knobTop),
                    size = Size(knobWidth, knobHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                val lineSpacing = 4.dp.toPx()
                for (i in -1..1) {
                    val lineY = knobCenterY + i * lineSpacing
                    drawLine(
                        color = Color(0xFF475569),
                        start = Offset(knobLeft + 6.dp.toPx(), lineY),
                        end = Offset(knobLeft + 14.dp.toPx(), lineY),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFF475569),
                        start = Offset(knobLeft + knobWidth - 14.dp.toPx(), lineY),
                        end = Offset(knobLeft + knobWidth - 6.dp.toPx(), lineY),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val indicatorColor = if (isMuted) Color(0xFFE53E3E) else accentColor
                drawLine(
                    color = indicatorColor,
                    start = Offset(knobLeft + 16.dp.toPx(), knobCenterY),
                    end = Offset(knobLeft + knobWidth - 16.dp.toPx(), knobCenterY),
                    strokeWidth = 2.5.dp.toPx()
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // CC Value Readout at bottom
        Text(
            text = "Val: $value",
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                color = HelixTextSecondary
            )
        )
    }
}

@Preview(showBackground = true, heightDp = 300)
@Composable
fun VerticalFaderPreview() {
    HelixMidiMixerTheme {
        Box(
            modifier = Modifier
                .background(HelixSurface)
                .padding(16.dp)
        ) {
            VerticalFader(
                value = 100,
                volumeCc = 11,
                onValueChange = {},
                isMuted = false
            )
        }
    }
}
