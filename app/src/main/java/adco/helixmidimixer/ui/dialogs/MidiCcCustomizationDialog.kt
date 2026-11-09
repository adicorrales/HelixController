package adco.helixmidimixer.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import adco.helixmidimixer.data.model.ChannelCc
import adco.helixmidimixer.data.model.ChannelConfig
import adco.helixmidimixer.data.model.MidiCcMapping
import adco.helixmidimixer.data.model.OutputGroup
import adco.helixmidimixer.ui.theme.HelixBackground
import adco.helixmidimixer.ui.theme.HelixBadgeBg
import adco.helixmidimixer.ui.theme.HelixBadgeText
import adco.helixmidimixer.ui.theme.HelixCardBorder
import adco.helixmidimixer.ui.theme.HelixGreenHeader
import adco.helixmidimixer.ui.theme.HelixGreenPrimary
import adco.helixmidimixer.ui.theme.HelixSurface
import adco.helixmidimixer.ui.theme.HelixTextPrimary
import adco.helixmidimixer.ui.theme.HelixTextSecondary

private enum class CcType { VOLUME, PAN, MUTE, SOLO }

@Composable
fun MidiCcCustomizationDialog(
    initialMapping: MidiCcMapping,
    matrixMidiChannels: Map<OutputGroup, Int>,
    channelVisibilities: Map<String, Boolean>,
    currentGroup: OutputGroup,
    onSaveMapping: (MidiCcMapping) -> Unit,
    onSaveMatrixMidiChannel: (OutputGroup, Int) -> Unit,
    onToggleChannelVisibility: (Int, Boolean) -> Unit,
    onDismiss: () -> Unit,
    transportMidiChannel: Int = 1,
    onSaveTransportMidiChannel: ((Int) -> Unit)? = null
) {
    var mapping by remember { mutableStateOf(initialMapping) }
    var selectedGroupTab by remember { mutableStateOf(currentGroup) }

    // State for editing a specific CC cell
    var editingTarget by remember { mutableStateOf<Triple<OutputGroup, Int, CcType>?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, HelixCardBorder, RoundedCornerShape(12.dp)),
            color = HelixSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "MIDI CC Config",
                            tint = HelixGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MIDI CC Customization Matrix",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = HelixTextPrimary
                            )
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = HelixTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Group Selector Tabs (1/4", XLR, Phones)
                TabRow(
                    selectedTabIndex = selectedGroupTab.ordinal,
                    containerColor = HelixBackground,
                    contentColor = HelixGreenPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, HelixCardBorder, RoundedCornerShape(8.dp))
                ) {
                    OutputGroup.entries.forEach { group ->
                        Tab(
                            selected = selectedGroupTab == group,
                            onClick = { selectedGroupTab = group },
                            text = {
                                Text(
                                    text = group.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selectedGroupTab == group) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedGroupTab == group) HelixGreenPrimary else HelixTextSecondary
                                    )
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Matrix MIDI Channel Selector for selected group
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(HelixBadgeBg)
                        .border(1.dp, HelixCardBorder, RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Matrix MIDI Channel (${selectedGroupTab.displayName}):",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = HelixTextPrimary
                        )
                    )
                    val defaultCh = when (selectedGroupTab) {
                        OutputGroup.OUTPUT_1_4 -> 3
                        OutputGroup.XLR -> 4
                        OutputGroup.PHONES -> 5
                    }
                    val currentCh = matrixMidiChannels[selectedGroupTab] ?: defaultCh
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = {
                                if (currentCh > 1) {
                                    onSaveMatrixMidiChannel(selectedGroupTab, currentCh - 1)
                                }
                            },
                            modifier = Modifier.size(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("-", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ch $currentCh",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = HelixGreenPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                if (currentCh < 16) {
                                    onSaveMatrixMidiChannel(selectedGroupTab, currentCh + 1)
                                }
                            },
                            modifier = Modifier.size(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Matrix Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(HelixGreenHeader)
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Show",
                        modifier = Modifier.width(44.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Channel",
                        modifier = Modifier.weight(1.3f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = "VOL CC",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "PAN CC",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "MUTE CC",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "SOLO CC",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Grid list of 19 Channels (0..18)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(19) { chIndex ->
                        val config = ChannelConfig.getConfigForIndex(chIndex)
                        val chCc = mapping.getChannelCc(selectedGroupTab, chIndex)
                        val key = "${selectedGroupTab.name}_$chIndex"
                        val isVisible = channelVisibilities[key] ?: true

                        MatrixRowItem(
                            chIndex = chIndex,
                            channelName = getChannelLabel(config),
                            isVisible = isVisible,
                            onVisibilityChanged = { visible -> onToggleChannelVisibility(chIndex, visible) },
                            chCc = chCc,
                            onEditCell = { ccType ->
                                editingTarget = Triple(selectedGroupTab, chIndex, ccType)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Actions: Reset to Defaults & Save/Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            mapping = MidiCcMapping(MidiCcMapping.createDefaultMappings())
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Restore,
                            contentDescription = "Reset Defaults",
                            tint = Color(0xFFECC94B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset Defaults",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFECC94B))
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HelixTextPrimary)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                onSaveMapping(mapping)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HelixGreenPrimary)
                        ) {
                            Text("Save Mapping", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Single CC Editor Sub-Dialog
    editingTarget?.let { (group, chIndex, ccType) ->
        val currentCc = when (ccType) {
            CcType.VOLUME -> mapping.getChannelCc(group, chIndex).volumeCc
            CcType.PAN -> mapping.getChannelCc(group, chIndex).panCc
            CcType.MUTE -> mapping.getChannelCc(group, chIndex).muteCc
            CcType.SOLO -> mapping.getChannelCc(group, chIndex).soloCc
        }

        EditSingleCcDialog(
            group = group,
            chIndex = chIndex,
            ccType = ccType,
            initialCc = currentCc,
            onSaveCc = { newCc ->
                val oldChCc = mapping.getChannelCc(group, chIndex)
                val newChCc = when (ccType) {
                    CcType.VOLUME -> oldChCc.copy(volumeCc = newCc)
                    CcType.PAN -> oldChCc.copy(panCc = newCc)
                    CcType.MUTE -> oldChCc.copy(muteCc = newCc)
                    CcType.SOLO -> oldChCc.copy(soloCc = newCc)
                }
                mapping = mapping.updateChannelCc(group, chIndex, newChCc)
                editingTarget = null
            },
            onDismiss = { editingTarget = null }
        )
    }
}

@Composable
private fun MatrixRowItem(
    chIndex: Int,
    channelName: String,
    isVisible: Boolean,
    onVisibilityChanged: (Boolean) -> Unit,
    chCc: ChannelCc,
    onEditCell: (CcType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = HelixBadgeBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visibility Checkbox
            Checkbox(
                checked = isVisible,
                onCheckedChange = onVisibilityChanged,
                colors = CheckboxDefaults.colors(
                    checkedColor = HelixGreenPrimary,
                    uncheckedColor = HelixTextSecondary,
                    checkmarkColor = Color.White
                ),
                modifier = Modifier.size(32.dp)
            )

            // Channel Name Label
            Text(
                text = channelName,
                modifier = Modifier.weight(1.3f),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = HelixTextPrimary,
                    fontSize = 11.sp
                )
            )

            // Vol CC Cell
            CcValueCell(
                ccVal = chCc.volumeCc,
                modifier = Modifier.weight(1f),
                onClick = { onEditCell(CcType.VOLUME) }
            )

            // Pan CC Cell
            CcValueCell(
                ccVal = chCc.panCc,
                modifier = Modifier.weight(1f),
                onClick = { onEditCell(CcType.PAN) }
            )

            // Mute CC Cell
            CcValueCell(
                ccVal = chCc.muteCc,
                modifier = Modifier.weight(1f),
                onClick = { onEditCell(CcType.MUTE) }
            )

            // Solo CC Cell
            CcValueCell(
                ccVal = chCc.soloCc,
                modifier = Modifier.weight(1f),
                onClick = { onEditCell(CcType.SOLO) }
            )
        }
    }
}

@Composable
private fun CcValueCell(
    ccVal: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(HelixBackground)
            .border(0.5.dp, HelixCardBorder, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CC $ccVal",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = HelixBadgeText
                )
            )
        }
    }
}

@Composable
private fun EditSingleCcDialog(
    group: OutputGroup,
    chIndex: Int,
    ccType: CcType,
    initialCc: Int,
    onSaveCc: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var textInput by remember { mutableStateOf(initialCc.toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val config = ChannelConfig.getConfigForIndex(chIndex)
    val title = "${getChannelLabel(config)} - ${ccType.name} CC"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit $title",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = HelixTextPrimary
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter MIDI CC Number (0..127) for ${group.displayName}:",
                    style = MaterialTheme.typography.bodySmall.copy(color = HelixTextSecondary)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { input ->
                        textInput = input
                        val parsed = input.toIntOrNull()
                        errorMessage = if (parsed == null || parsed !in 0..127) {
                            "CC must be between 0 and 127"
                        } else null
                    },
                    isError = errorMessage != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HelixGreenPrimary,
                        unfocusedBorderColor = HelixCardBorder,
                        focusedTextColor = HelixTextPrimary,
                        unfocusedTextColor = HelixTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = err,
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFE53E3E))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = textInput.toIntOrNull()
                    if (parsed != null && parsed in 0..127) {
                        onSaveCc(parsed)
                    }
                },
                enabled = errorMessage == null && textInput.isNotBlank(),
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

private fun getChannelLabel(config: ChannelConfig): String {
    return when (config.index) {
        0 -> "0: Home"
        1 -> "1: Path 1A"
        2 -> "2: Path 1B"
        3 -> "3: Path 2A"
        4 -> "4: Path 2B"
        5 -> "5: Song Master"
        in 6..13 -> "${config.index}: CH ${config.index - 5}"
        14 -> "14: Click"
        15 -> "15: Count In"
        16 -> "16: USB In 1/2"
        17 -> "17: Bluetooth In"
        18 -> "18: Aux In"
        else -> "${config.index}: ${config.defaultName}"
    }
}
