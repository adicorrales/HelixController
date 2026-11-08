package adco.helixmidimixer.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import adco.helixmidimixer.ui.theme.HelixBadgeBg
import adco.helixmidimixer.ui.theme.HelixCardBorder
import adco.helixmidimixer.ui.theme.HelixGreenHeader
import adco.helixmidimixer.ui.theme.HelixGreenPrimary
import adco.helixmidimixer.ui.theme.HelixSurface
import adco.helixmidimixer.ui.theme.HelixTextPrimary
import adco.helixmidimixer.ui.theme.HelixTextSecondary

@Composable
fun PanAdjustmentDialog(
    channelName: String,
    panCc: Int,
    initialPan: Int,
    onPanChanged: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var currentPan by remember { mutableIntStateOf(initialPan) }

    val panText = remember(currentPan) {
        when {
            currentPan == 64 -> "Center (64)"
            currentPan < 64 -> "L ${64 - currentPan} (CC $currentPan)"
            else -> "R ${currentPan - 64} (CC $currentPan)"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Adjust Pan - $channelName",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = HelixTextPrimary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pan CC: $panCc",
                    style = MaterialTheme.typography.labelSmall.copy(color = HelixTextSecondary)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Pan Display Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HelixBadgeBg)
                        .border(1.dp, HelixCardBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = panText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = HelixGreenPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Slider (0..127)
                Slider(
                    value = currentPan.toFloat(),
                    onValueChange = { currentPan = it.toInt() },
                    valueRange = 0f..127f,
                    steps = 126,
                    colors = SliderDefaults.colors(
                        thumbColor = HelixGreenPrimary,
                        activeTrackColor = HelixGreenPrimary,
                        inactiveTrackColor = HelixCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Preset Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { currentPan = 0 },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Left (0)", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { currentPan = 64 },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HelixGreenHeader),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Center (64)", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { currentPan = 127 },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Right (127)", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPanChanged(currentPan)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = HelixGreenPrimary)
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = HelixSurface
    )
}
