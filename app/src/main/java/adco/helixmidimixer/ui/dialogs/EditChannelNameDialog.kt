package adco.helixmidimixer.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import adco.helixmidimixer.ui.theme.HelixCardBorder
import adco.helixmidimixer.ui.theme.HelixGreenPrimary
import adco.helixmidimixer.ui.theme.HelixSurface
import adco.helixmidimixer.ui.theme.HelixTextPrimary
import adco.helixmidimixer.ui.theme.HelixTextSecondary

@Composable
fun EditChannelNameDialog(
    songIndex: Int, // 1..8
    currentName: String,
    onSaveName: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var textInput by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rename Song $songIndex Track",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = HelixTextPrimary
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter custom name for Track $songIndex:",
                    style = MaterialTheme.typography.bodySmall.copy(color = HelixTextSecondary)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    singleLine = true,
                    placeholder = { Text("Track $songIndex") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HelixGreenPrimary,
                        unfocusedBorderColor = HelixCardBorder,
                        focusedTextColor = HelixTextPrimary,
                        unfocusedTextColor = HelixTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveName(textInput.trim())
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
