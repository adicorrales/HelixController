package adco.helixmidimixer.ui.dialogs

import android.Manifest
import android.os.Build
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.BluetoothSearching
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import adco.helixmidimixer.midi.BleConnectionState
import adco.helixmidimixer.midi.BleDeviceInfo
import adco.helixmidimixer.ui.theme.HelixBackground
import adco.helixmidimixer.ui.theme.HelixBadgeBg
import adco.helixmidimixer.ui.theme.HelixCardBorder
import adco.helixmidimixer.ui.theme.HelixGreenHeader
import adco.helixmidimixer.ui.theme.HelixGreenPrimary
import adco.helixmidimixer.ui.theme.HelixSurface
import adco.helixmidimixer.ui.theme.HelixTextPrimary
import adco.helixmidimixer.ui.theme.HelixTextSecondary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BluetoothMidiDialog(
    connectionState: BleConnectionState,
    scannedDevices: List<BleDeviceInfo>,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDevice: (BleDeviceInfo) -> Unit,
    onDisconnect: () -> Unit,
    onDismiss: () -> Unit
) {
    val blePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = blePermissions)

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted && connectionState == BleConnectionState.Disconnected) {
            onStartScan()
        }
    }

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
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Bluetooth,
                            contentDescription = "Bluetooth MIDI",
                            tint = HelixGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BLE MIDI & WIDI Master",
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

                Spacer(modifier = Modifier.height(12.dp))

                // Current Connection Status Card
                StatusSummaryCard(
                    connectionState = connectionState,
                    onDisconnect = onDisconnect
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Permissions Warning or Devices List
                if (!permissionsState.allPermissionsGranted) {
                    PermissionRequiredCard(
                        onRequestPermissions = { permissionsState.launchMultiplePermissionRequest() }
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Available MIDI Devices (${scannedDevices.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = HelixTextSecondary
                            )
                        )

                        if (connectionState is BleConnectionState.Scanning) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable(onClick = onStopScan)
                                    .padding(4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = HelixGreenPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Stop Scan",
                                    style = MaterialTheme.typography.labelSmall.copy(color = HelixGreenPrimary)
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable(onClick = onStartScan)
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Scan",
                                    tint = HelixGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Scan",
                                    style = MaterialTheme.typography.labelSmall.copy(color = HelixGreenPrimary)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (scannedDevices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(HelixBackground)
                                .border(0.5.dp, HelixCardBorder, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (connectionState is BleConnectionState.Scanning) "Searching for WIDI Master & BLE MIDI..." else "No devices found. Tap Scan to search.",
                                style = MaterialTheme.typography.bodySmall.copy(color = HelixTextSecondary)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(scannedDevices) { deviceInfo ->
                                DeviceItemRow(
                                    deviceInfo = deviceInfo,
                                    isCurrentlyConnected = (connectionState as? BleConnectionState.Connected)?.address == deviceInfo.address,
                                    onConnect = { onConnectDevice(deviceInfo) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HelixTextPrimary)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusSummaryCard(
    connectionState: BleConnectionState,
    onDisconnect: () -> Unit
) {
    val (statusTitle, statusColor, icon) = when (connectionState) {
        is BleConnectionState.Connected -> Triple(
            "Connected to ${connectionState.deviceName ?: "WIDI Master"}",
            HelixGreenPrimary,
            Icons.Rounded.BluetoothConnected
        )
        is BleConnectionState.Connecting -> Triple(
            "Connecting to ${connectionState.deviceName ?: "Device"}...",
            Color(0xFFECC94B),
            Icons.AutoMirrored.Rounded.BluetoothSearching
        )
        is BleConnectionState.Scanning -> Triple(
            "Scanning for Bluetooth MIDI devices...",
            Color(0xFF38BDF8),
            Icons.AutoMirrored.Rounded.BluetoothSearching
        )
        is BleConnectionState.Error -> Triple(
            connectionState.message,
            Color(0xFFE53E3E),
            Icons.Rounded.Bluetooth
        )
        BleConnectionState.Disconnected -> Triple(
            "Disconnected",
            HelixTextSecondary,
            Icons.Rounded.Bluetooth
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = HelixBadgeBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = HelixTextPrimary
                    )
                )
            }

            if (connectionState is BleConnectionState.Connected) {
                Button(
                    onClick = onDisconnect,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B2C2C)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Disconnect", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun PermissionRequiredCard(
    onRequestPermissions: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = HelixBadgeBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bluetooth Permissions Required",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFECC94B)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Helix Stadium Mixer requires Bluetooth permissions to discover and connect to WIDI Master & BLE MIDI adapters.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = HelixTextSecondary,
                    fontSize = 12.sp
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRequestPermissions,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HelixGreenHeader)
            ) {
                Text("Grant Permissions")
            }
        }
    }
}

@Composable
private fun DeviceItemRow(
    deviceInfo: BleDeviceInfo,
    isCurrentlyConnected: Boolean,
    onConnect: () -> Unit
) {
    val isWidiMaster = deviceInfo.name.contains("WIDI", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isWidiMaster) 1.5.dp else 0.5.dp,
                color = if (isWidiMaster) HelixGreenPrimary else HelixCardBorder,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = HelixBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bluetooth,
                    contentDescription = null,
                    tint = if (isWidiMaster) HelixGreenPrimary else HelixTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = deviceInfo.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = HelixTextPrimary
                            )
                        )
                        if (isWidiMaster) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(HelixGreenHeader)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "WIDI MASTER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                    Text(
                        text = deviceInfo.address,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = HelixTextSecondary,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.SignalCellularAlt,
                    contentDescription = "RSSI",
                    tint = HelixTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${deviceInfo.rssi} dBm",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = HelixTextSecondary,
                        fontSize = 10.sp
                    )
                )
                Spacer(modifier = Modifier.width(10.dp))

                if (isCurrentlyConnected) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Connected",
                        tint = HelixGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Button(
                        onClick = onConnect,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isWidiMaster) HelixGreenPrimary else HelixGreenHeader
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Connect", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
