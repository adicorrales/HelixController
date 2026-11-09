package adco.helixmidimixer.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import adco.helixmidimixer.data.model.SongPlayMode
import adco.helixmidimixer.ui.theme.HelixCardBorder
import adco.helixmidimixer.ui.theme.HelixGreenPrimary
import adco.helixmidimixer.ui.theme.HelixSurface
import adco.helixmidimixer.ui.theme.HelixTextPrimary
import adco.helixmidimixer.ui.theme.HelixTextSecondary

@Composable
fun TransportSettingsDialog(
    initialTransportChannel: Int,
    initialSongPlayMode: SongPlayMode,
    onSaveTransportChannel: (Int) -> Unit,
    onSaveSongPlayMode: (SongPlayMode) -> Unit,
    onDismiss: () -> Unit
) {
    var channelInput by remember { mutableStateOf(initialTransportChannel.toString()) }
    var selectedMode by remember { mutableStateOf(initialSongPlayMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Transport & Helix Song Settings",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = HelixTextPrimary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Transport MIDI Channel (1-16):",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = HelixTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = channelInput,
                    onValueChange = { channelInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HelixGreenPrimary,
                        unfocusedBorderColor = HelixCardBorder,
                        focusedTextColor = HelixTextPrimary,
                        unfocusedTextColor = HelixTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Helix Song Play Mode (Global Settings > Songs):",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = HelixTextPrimary
                    )
                )
                Text(
                    text = "Matches your Helix device configuration so app button syncs correctly.",
                    style = MaterialTheme.typography.labelSmall.copy(color = HelixTextSecondary, fontSize = 11.sp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                SongPlayMode.entries.forEach { mode ->
                    val isSelected = (mode == selectedMode)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedMode = mode }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedMode = mode },
                            colors = RadioButtonDefaults.colors(selectedColor = HelixGreenPrimary)
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = mode.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = HelixTextPrimary
                                )
                            )
                            Text(
                                text = mode.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = HelixTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ch = channelInput.toIntOrNull()?.coerceIn(1, 16) ?: initialTransportChannel
                    onSaveTransportChannel(ch)
                    onSaveSongPlayMode(selectedMode)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = HelixGreenPrimary)
            ) {
                Text("Save")
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
