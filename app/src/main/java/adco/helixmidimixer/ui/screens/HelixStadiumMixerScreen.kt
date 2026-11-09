package adco.helixmidimixer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import adco.helixmidimixer.ui.components.ChannelStripsRow
import adco.helixmidimixer.ui.components.MatrixTopBar
import adco.helixmidimixer.ui.components.TransportBar
import adco.helixmidimixer.ui.dialogs.BluetoothMidiDialog
import adco.helixmidimixer.ui.dialogs.EditChannelNameDialog
import adco.helixmidimixer.ui.dialogs.MidiCcCustomizationDialog
import adco.helixmidimixer.ui.dialogs.PanAdjustmentDialog
import adco.helixmidimixer.ui.theme.HelixBackground
import adco.helixmidimixer.ui.viewmodel.MainViewModel

@Composable
fun HelixStadiumMixerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedGroup by viewModel.selectedGroup.collectAsStateWithLifecycle()
    val channelStates by viewModel.channelStates.collectAsStateWithLifecycle()
    val midiCcMapping by viewModel.midiCcMapping.collectAsStateWithLifecycle()
    val matrixMidiChannels by viewModel.matrixMidiChannels.collectAsStateWithLifecycle()
    val transportMidiChannel by viewModel.transportMidiChannel.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val routesExpanded by viewModel.routesExpanded.collectAsStateWithLifecycle()
    val songChannelsExpanded by viewModel.songChannelsExpanded.collectAsStateWithLifecycle()
    val channelVisibilities by viewModel.channelVisibilities.collectAsStateWithLifecycle()
    val bleConnectionState by viewModel.bleConnectionState.collectAsStateWithLifecycle()
    val scannedDevices by viewModel.scannedDevices.collectAsStateWithLifecycle()

    // Dialog state management
    var showBleDialog by remember { mutableStateOf(false) }
    var showCcDialog by remember { mutableStateOf(false) }
    var panChannelTarget by remember { mutableStateOf<Int?>(null) } // channelIndex
    var renameSongTarget by remember { mutableStateOf<Int?>(null) } // songIndex (1..8)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = HelixBackground,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            MatrixTopBar(
                selectedGroup = selectedGroup,
                onOutputGroupSelected = { group -> viewModel.selectOutputGroup(group) },
                bleConnectionState = bleConnectionState,
                onOpenBleDialog = { showBleDialog = true },
                onOpenCcDialog = { showCcDialog = true }
            )
        },
        bottomBar = {
            TransportBar(
                isPlaying = isPlaying,
                onPrev = { viewModel.sendTransportPrev() },
                onReturnToZero = { viewModel.sendTransportReturnToZero() },
                onPlayToggle = { viewModel.toggleTransportPlay() },
                onNext = { viewModel.sendTransportNext() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HelixBackground)
        ) {
            ChannelStripsRow(
                channelStates = channelStates,
                midiCcMapping = midiCcMapping,
                selectedGroup = selectedGroup,
                routesExpanded = routesExpanded,
                songChannelsExpanded = songChannelsExpanded,
                onVolumeChanged = { chIdx, newVol -> viewModel.updateVolume(chIdx, newVol) },
                onPanChanged = { chIdx, newPan -> viewModel.updatePan(chIdx, newPan) },
                onMuteToggled = { chIdx, isMuted -> viewModel.toggleMute(chIdx, isMuted) },
                onSoloToggled = { chIdx, isSoloed -> viewModel.toggleSolo(chIdx, isSoloed) },
                onHomeRouteChanged = { newRoute -> viewModel.updateHomeRoute(newRoute) },
                onOpenPanDialog = { chIdx -> panChannelTarget = chIdx },
                onChannelRenameRequested = { songIdx -> renameSongTarget = songIdx },
                onToggleRoutesExpanded = { group, expanded -> viewModel.toggleRoutesExpanded(group, expanded) },
                onToggleSongChannelsExpanded = { group, expanded -> viewModel.toggleSongChannelsExpanded(group, expanded) }
            )
        }
    }

    // 1. Bluetooth BLE MIDI Dialog
    if (showBleDialog) {
        BluetoothMidiDialog(
            connectionState = bleConnectionState,
            scannedDevices = scannedDevices,
            onStartScan = { viewModel.startBleScan() },
            onStopScan = { viewModel.stopBleScan() },
            onConnectDevice = { info -> viewModel.connectBleDevice(info.device) },
            onDisconnect = { viewModel.disconnectBleDevice() },
            onDismiss = { showBleDialog = false }
        )
    }

    // 2. MIDI CC Customization Dialog
    if (showCcDialog) {
        MidiCcCustomizationDialog(
            initialMapping = midiCcMapping,
            matrixMidiChannels = matrixMidiChannels,
            transportMidiChannel = transportMidiChannel,
            channelVisibilities = channelVisibilities,
            currentGroup = selectedGroup,
            onSaveMapping = { newMapping -> viewModel.updateMidiCcMapping(newMapping) },
            onSaveMatrixMidiChannel = { group, ch -> viewModel.saveMatrixMidiChannel(group, ch) },
            onSaveTransportMidiChannel = { ch -> viewModel.saveTransportMidiChannel(ch) },
            onToggleChannelVisibility = { chIdx, visible -> viewModel.toggleChannelVisibility(chIdx, visible) },
            onDismiss = { showCcDialog = false }
        )
    }

    // 3. Pan Adjustment Dialog
    panChannelTarget?.let { chIdx ->
        val chState = channelStates.find { channelState -> channelState.channelIndex == chIdx }
        val panCc = midiCcMapping.getChannelCc(selectedGroup, chIdx).panCc
        if (chState != null) {
            PanAdjustmentDialog(
                channelName = chState.effectiveName,
                panCc = panCc,
                initialPan = chState.pan,
                onPanChanged = { newPan -> viewModel.updatePan(chIdx, newPan) },
                onDismiss = { panChannelTarget = null }
            )
        }
    }

    // 4. Edit Song Channel Name Dialog
    renameSongTarget?.let { songIdx ->
        val chIdx = songIdx + 5 // song 1..8 -> channel 6..13
        val chState = channelStates.find { channelState -> channelState.channelIndex == chIdx }
        val currentName = chState?.customName ?: "CH $songIdx"
        EditChannelNameDialog(
            songIndex = songIdx,
            currentName = currentName,
            onSaveName = { newName -> viewModel.saveSongChannelName(songIdx, newName) },
            onDismiss = { renameSongTarget = null }
        )
    }
}
