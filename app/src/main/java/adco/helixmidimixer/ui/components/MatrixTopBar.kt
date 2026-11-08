package adco.helixmidimixer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.BluetoothSearching
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.Cable
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import adco.helixmidimixer.data.model.OutputGroup
import adco.helixmidimixer.midi.BleConnectionState
import adco.helixmidimixer.ui.theme.HelixBackground
import adco.helixmidimixer.ui.theme.HelixBadgeBg
import adco.helixmidimixer.ui.theme.HelixCardBorder
import adco.helixmidimixer.ui.theme.HelixGreenPrimary
import adco.helixmidimixer.ui.theme.HelixMidiMixerTheme
import adco.helixmidimixer.ui.theme.HelixSurface
import adco.helixmidimixer.ui.theme.HelixTextPrimary
import adco.helixmidimixer.ui.theme.HelixTextSecondary

@Composable
fun MatrixTopBar(
    selectedGroup: OutputGroup,
    onOutputGroupSelected: (OutputGroup) -> Unit,
    bleConnectionState: BleConnectionState,
    onOpenBleDialog: () -> Unit,
    onOpenCcDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(HelixSurface)
            .border(width = 1.dp, color = HelixCardBorder),
        color = HelixSurface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Helix Stadium Matrix Title / Logo
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(HelixGreenPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HELIX STADIUM MATRIX",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = HelixTextPrimary
                    )
                )
            }

            // Center: Output Group Selector Toggle Buttons (1/4", XLR, Phones)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(HelixBackground)
                    .border(1.dp, HelixCardBorder, RoundedCornerShape(8.dp))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutputGroupToggleItem(
                    group = OutputGroup.OUTPUT_1_4,
                    icon = Icons.Rounded.Cable,
                    isSelected = selectedGroup == OutputGroup.OUTPUT_1_4,
                    onClick = { onOutputGroupSelected(OutputGroup.OUTPUT_1_4) }
                )
                OutputGroupToggleItem(
                    group = OutputGroup.XLR,
                    icon = Icons.Rounded.Mic,
                    isSelected = selectedGroup == OutputGroup.XLR,
                    onClick = { onOutputGroupSelected(OutputGroup.XLR) }
                )
                OutputGroupToggleItem(
                    group = OutputGroup.PHONES,
                    icon = Icons.Rounded.Headphones,
                    isSelected = selectedGroup == OutputGroup.PHONES,
                    onClick = { onOutputGroupSelected(OutputGroup.PHONES) }
                )
            }

            // Right: BLE Connection Indicator & CC Customization Action
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bluetooth status button (WIDI Master connection status)
                BleStatusButton(
                    connectionState = bleConnectionState,
                    onClick = onOpenBleDialog
                )

                // Settings / CC Customization button
                IconButton(
                    onClick = onOpenCcDialog,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(HelixBadgeBg)
                        .border(1.dp, HelixCardBorder, RoundedCornerShape(8.dp))
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "CC Customization",
                        tint = HelixGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OutputGroupToggleItem(
    group: OutputGroup,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) HelixGreenPrimary else Color.Transparent,
        label = "groupToggleBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else HelixTextSecondary,
        label = "groupToggleContent"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = group.displayName,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = group.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        )
    }
}

@Composable
private fun BleStatusButton(
    connectionState: BleConnectionState,
    onClick: () -> Unit
) {
    val (statusText, statusColor, statusIcon) = when (connectionState) {
        is BleConnectionState.Connected -> Triple(
            connectionState.deviceName ?: "WIDI Connected",
            HelixGreenPrimary,
            Icons.Rounded.BluetoothConnected
        )
        is BleConnectionState.Connecting -> Triple(
            "Connecting...",
            Color(0xFFECC94B),
            Icons.AutoMirrored.Rounded.BluetoothSearching
        )
        is BleConnectionState.Scanning -> Triple(
            "Scanning...",
            Color(0xFF38BDF8),
            Icons.AutoMirrored.Rounded.BluetoothSearching
        )
        is BleConnectionState.Error -> Triple(
            "BLE Error",
            Color(0xFFE53E3E),
            Icons.Rounded.Bluetooth
        )
        BleConnectionState.Disconnected -> Triple(
            "WIDI Disconnected",
            HelixTextSecondary,
            Icons.Rounded.Bluetooth
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val isBlinking = connectionState is BleConnectionState.Scanning || connectionState is BleConnectionState.Connecting

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(HelixBadgeBg)
            .border(1.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .alpha(if (isBlinking) alphaAnim else 1f)
                .background(statusColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = statusIcon,
            contentDescription = "Bluetooth Status",
            tint = statusColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = HelixTextPrimary
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 900)
@Composable
fun MatrixTopBarPreview() {
    HelixMidiMixerTheme {
        MatrixTopBar(
            selectedGroup = OutputGroup.OUTPUT_1_4,
            onOutputGroupSelected = {},
            bleConnectionState = BleConnectionState.Connected("WIDI Master", "00:11:22:33:44:55"),
            onOpenBleDialog = {},
            onOpenCcDialog = {}
        )
    }
}
