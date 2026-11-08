package adco.helixmidimixer.ui.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import adco.helixmidimixer.data.model.ChannelState
import adco.helixmidimixer.data.model.HomeRoute
import adco.helixmidimixer.data.model.MidiCcMapping
import adco.helixmidimixer.data.model.OutputGroup
import adco.helixmidimixer.data.repository.DataStoreRepository
import adco.helixmidimixer.midi.BleConnectionState
import adco.helixmidimixer.midi.BleDeviceInfo
import adco.helixmidimixer.midi.BleMidiManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: DataStoreRepository,
    val bleMidiManager: BleMidiManager
) : ViewModel() {

    private val _selectedGroup = MutableStateFlow(OutputGroup.OUTPUT_1_4)
    val selectedGroup: StateFlow<OutputGroup> = _selectedGroup.asStateFlow()

    // Local in-memory state for volume (0..127) per group and channel (key: "GROUP_chIndex")
    private val _volumes = MutableStateFlow<Map<String, Int>>(emptyMap())
    // Local in-memory state for pan (0..127) per group and channel (key: "GROUP_chIndex")
    private val _pans = MutableStateFlow<Map<String, Int>>(emptyMap())

    val midiCcMapping: StateFlow<MidiCcMapping> = repository.getMidiCcMapping()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MidiCcMapping())

    val bleConnectionState: StateFlow<BleConnectionState> = bleMidiManager.connectionState
    val scannedDevices: StateFlow<List<BleDeviceInfo>> = bleMidiManager.scannedDevices

    private val songChannelNames = repository.getSongChannelNames()
    private val muteStates = repository.getMuteStates()
    private val soloStates = repository.getSoloStates()
    private val homeRoutes = repository.getHomeRoutes()

    val channelStates: StateFlow<List<ChannelState>> = combine(
        _selectedGroup,
        _volumes,
        _pans,
        songChannelNames,
        muteStates,
        soloStates,
        homeRoutes
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val group = flows[0] as OutputGroup
        @Suppress("UNCHECKED_CAST")
        val volumes = flows[1] as Map<String, Int>
        @Suppress("UNCHECKED_CAST")
        val pans = flows[2] as Map<String, Int>
        @Suppress("UNCHECKED_CAST")
        val songNames = flows[3] as Map<Int, String>
        @Suppress("UNCHECKED_CAST")
        val mutes = flows[4] as Map<String, Boolean>
        @Suppress("UNCHECKED_CAST")
        val solos = flows[5] as Map<String, Boolean>
        @Suppress("UNCHECKED_CAST")
        val routes = flows[6] as Map<OutputGroup, HomeRoute>

        (0..10).map { chIndex ->
            val key = "${group.name}_$chIndex"
            val customName = if (chIndex in 2..9) {
                val songIdx = chIndex - 1 // 1..8
                songNames[songIdx]
            } else null

            val volumeVal = volumes[key] ?: 100 // Default 100 CC (~0dB)
            val panVal = pans[key] ?: 64        // Default Center (64)
            val isMutedVal = mutes[key] ?: false
            val isSoloedVal = solos[key] ?: false
            val routeVal = routes[group] ?: HomeRoute.ROUTE_1A

            ChannelState(
                group = group,
                channelIndex = chIndex,
                customName = customName,
                volume = volumeVal,
                pan = panVal,
                isMuted = isMutedVal,
                isSoloed = isSoloedVal,
                homeRoute = routeVal
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Listen to incoming MIDI CC packets over BLE
        viewModelScope.launch {
            bleMidiManager.incomingMidiCc.collect { ccMsg ->
                handleIncomingMidiCc(ccMsg.ccNumber, ccMsg.value)
            }
        }
    }

    fun selectOutputGroup(group: OutputGroup) {
        _selectedGroup.value = group
    }

    fun updateVolume(channelIndex: Int, volume: Int) {
        val group = _selectedGroup.value
        val key = "${group.name}_$channelIndex"
        val currentVols = _volumes.value.toMutableMap()
        currentVols[key] = volume.coerceIn(0, 127)
        _volumes.value = currentVols

        // Send BLE MIDI CC
        val ccNum = midiCcMapping.value.getChannelCc(group, channelIndex).volumeCc
        bleMidiManager.sendControlChange(channel = 0, ccNumber = ccNum, value = volume)
    }

    fun updatePan(channelIndex: Int, pan: Int) {
        val group = _selectedGroup.value
        val key = "${group.name}_$channelIndex"
        val currentPans = _pans.value.toMutableMap()
        currentPans[key] = pan.coerceIn(0, 127)
        _pans.value = currentPans

        // Send BLE MIDI CC
        val ccNum = midiCcMapping.value.getChannelCc(group, channelIndex).panCc
        bleMidiManager.sendControlChange(channel = 0, ccNumber = ccNum, value = pan)
    }

    fun toggleMute(channelIndex: Int, isMuted: Boolean) {
        val group = _selectedGroup.value
        viewModelScope.launch {
            repository.saveMuteState(group, channelIndex, isMuted)
        }
        // Send BLE MIDI CC (0 for off, 127 for on)
        val ccNum = midiCcMapping.value.getChannelCc(group, channelIndex).muteCc
        bleMidiManager.sendControlChange(channel = 0, ccNumber = ccNum, value = if (isMuted) 127 else 0)
    }

    fun toggleSolo(channelIndex: Int, isSoloed: Boolean) {
        val group = _selectedGroup.value
        viewModelScope.launch {
            repository.saveSoloState(group, channelIndex, isSoloed)
        }
        // Send BLE MIDI CC (0 for off, 127 for on)
        val ccNum = midiCcMapping.value.getChannelCc(group, channelIndex).soloCc
        bleMidiManager.sendControlChange(channel = 0, ccNumber = ccNum, value = if (isSoloed) 127 else 0)
    }

    fun updateHomeRoute(route: HomeRoute) {
        val group = _selectedGroup.value
        viewModelScope.launch {
            repository.saveHomeRoute(group, route)
        }
    }

    fun saveSongChannelName(songIndex: Int, newName: String) {
        viewModelScope.launch {
            repository.saveSongChannelName(songIndex, newName)
        }
    }

    fun updateMidiCcMapping(mapping: MidiCcMapping) {
        viewModelScope.launch {
            repository.saveMidiCcMapping(mapping)
        }
    }

    fun startBleScan() {
        bleMidiManager.startScan()
    }

    fun stopBleScan() {
        bleMidiManager.stopScan()
    }

    fun connectBleDevice(device: BluetoothDevice) {
        bleMidiManager.connect(device)
    }

    fun disconnectBleDevice() {
        bleMidiManager.disconnect()
    }

    private fun handleIncomingMidiCc(ccNumber: Int, value: Int) {
        val group = _selectedGroup.value
        val mapping = midiCcMapping.value

        for (chIndex in 0..10) {
            val chCc = mapping.getChannelCc(group, chIndex)
            val key = "${group.name}_$chIndex"
            when (ccNumber) {
                chCc.volumeCc -> {
                    val vols = _volumes.value.toMutableMap()
                    vols[key] = value
                    _volumes.value = vols
                }
                chCc.panCc -> {
                    val pns = _pans.value.toMutableMap()
                    pns[key] = value
                    _pans.value = pns
                }
                chCc.muteCc -> {
                    viewModelScope.launch {
                        repository.saveMuteState(group, chIndex, value >= 64)
                    }
                }
                chCc.soloCc -> {
                    viewModelScope.launch {
                        repository.saveSoloState(group, chIndex, value >= 64)
                    }
                }
            }
        }
    }
}
