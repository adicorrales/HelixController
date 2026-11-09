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

    val matrixMidiChannels: StateFlow<Map<OutputGroup, Int>> = repository.getMatrixMidiChannels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val transportMidiChannel: StateFlow<Int> = repository.getTransportMidiChannel()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    val routesExpanded: StateFlow<Map<OutputGroup, Boolean>> = repository.getRoutesExpanded()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val songChannelsExpanded: StateFlow<Map<OutputGroup, Boolean>> = repository.getSongChannelsExpanded()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val channelVisibilities: StateFlow<Map<String, Boolean>> = repository.getChannelVisibilities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

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
        homeRoutes,
        routesExpanded,
        channelVisibilities,
        songChannelsExpanded
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
        @Suppress("UNCHECKED_CAST")
        val expandedMap = flows[7] as Map<OutputGroup, Boolean>
        @Suppress("UNCHECKED_CAST")
        val visibilities = flows[8] as Map<String, Boolean>
        @Suppress("UNCHECKED_CAST")
        val songExpandedMap = flows[9] as Map<OutputGroup, Boolean>

        val isExpanded = expandedMap[group] ?: false
        val isSongExpanded = songExpandedMap[group] ?: true

        (0..18).mapNotNull { chIndex ->
            val key = "${group.name}_$chIndex"
            val isVisible = visibilities[key] ?: true
            if (!isVisible) {
                return@mapNotNull null
            }

            // If routes are collapsed, hide route channels (indices 1, 2, 3, 4: Path 1A, 1B, 2A, 2B)
            if (!isExpanded && chIndex in 1..4) {
                return@mapNotNull null
            }

            // If song channels are collapsed, hide song channels (indices 6..13: CH 1..8)
            if (!isSongExpanded && chIndex in 6..13) {
                return@mapNotNull null
            }

            val customName = if (chIndex in 6..13) {
                val songIdx = chIndex - 5 // 1..8
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

    private fun getMidiChannelForGroup(group: OutputGroup): Int {
        val defaultCh = when (group) {
            OutputGroup.OUTPUT_1_4 -> 3
            OutputGroup.XLR -> 4
            OutputGroup.PHONES -> 5
        }
        val channel1Based = matrixMidiChannels.value[group] ?: defaultCh
        return (channel1Based - 1).coerceIn(0, 15)
    }

    fun selectOutputGroup(group: OutputGroup) {
        _selectedGroup.value = group
    }

    fun updateVolume(channelIndex: Int, volume: Int) {
        val group = _selectedGroup.value
        val key = "${group.name}_$channelIndex"
        val clampedVol = volume.coerceIn(0, 127)
        val currentVols = _volumes.value
        if (currentVols[key] != clampedVol) {
            val mutableVols = currentVols.toMutableMap()
            mutableVols[key] = clampedVol
            _volumes.value = mutableVols

            // Send BLE MIDI CC
            val ccNum = midiCcMapping.value.getChannelCc(group, channelIndex).volumeCc
            val midiCh = getMidiChannelForGroup(group)
            bleMidiManager.sendControlChange(channel = midiCh, ccNumber = ccNum, value = clampedVol)
        }
    }

    fun updatePan(channelIndex: Int, pan: Int) {
        val group = _selectedGroup.value
        val key = "${group.name}_$channelIndex"
        val clampedPan = pan.coerceIn(0, 127)
        val currentPans = _pans.value
        if (currentPans[key] != clampedPan) {
            val mutablePans = currentPans.toMutableMap()
            mutablePans[key] = clampedPan
            _pans.value = mutablePans

            // Send BLE MIDI CC
            val ccNum = midiCcMapping.value.getChannelCc(group, channelIndex).panCc
            val midiCh = getMidiChannelForGroup(group)
            bleMidiManager.sendControlChange(channel = midiCh, ccNumber = ccNum, value = clampedPan)
        }
    }

    fun toggleMute(channelIndex: Int, isMuted: Boolean) {
        val group = _selectedGroup.value
        viewModelScope.launch {
            repository.saveMuteState(group, channelIndex, isMuted)
        }
        // Send BLE MIDI CC (0 for off, 127 for on >= 64)
        val ccNum = midiCcMapping.value.getChannelCc(group, channelIndex).muteCc
        val midiCh = getMidiChannelForGroup(group)
        bleMidiManager.sendControlChange(channel = midiCh, ccNumber = ccNum, value = if (isMuted) 127 else 0)
    }

    fun toggleSolo(channelIndex: Int, isSoloed: Boolean) {
        val group = _selectedGroup.value
        viewModelScope.launch {
            repository.saveSoloState(group, channelIndex, isSoloed)
        }
        // Send BLE MIDI CC (0 for off, 127 for on >= 64)
        val ccNum = midiCcMapping.value.getChannelCc(group, channelIndex).soloCc
        val midiCh = getMidiChannelForGroup(group)
        bleMidiManager.sendControlChange(channel = midiCh, ccNumber = ccNum, value = if (isSoloed) 127 else 0)
    }

    fun toggleChannelVisibility(channelIndex: Int, isVisible: Boolean) {
        val group = _selectedGroup.value
        viewModelScope.launch {
            repository.saveChannelVisibility(group, channelIndex, isVisible)
        }
    }

    fun clearAllSolos() {
        val group = _selectedGroup.value
        viewModelScope.launch {
            for (chIdx in 0..18) {
                repository.saveSoloState(group, chIdx, false)
            }
        }
        // Send Clear Solo CC (CC 94, value 127 >= 64)
        val midiCh = getMidiChannelForGroup(group)
        bleMidiManager.sendControlChange(channel = midiCh, ccNumber = 94, value = 127)
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

    fun saveMatrixMidiChannel(group: OutputGroup, channel: Int) {
        viewModelScope.launch {
            repository.saveMatrixMidiChannel(group, channel)
        }
    }

    fun saveTransportMidiChannel(channel: Int) {
        viewModelScope.launch {
            repository.saveTransportMidiChannel(channel)
        }
    }

    private fun getTransportMidiChannel0Based(): Int {
        val channel1Based = transportMidiChannel.value
        return (channel1Based - 1).coerceIn(0, 15)
    }

    fun sendTransportPrev() {
        val ch = getTransportMidiChannel0Based()
        bleMidiManager.sendControlChange(channel = ch, ccNumber = 49, value = 0)
    }

    fun sendTransportReturnToZero() {
        val ch = getTransportMidiChannel0Based()
        bleMidiManager.sendControlChange(channel = ch, ccNumber = 47, value = 127)
    }

    fun toggleTransportPlay() {
        val newPlaying = !_isPlaying.value
        _isPlaying.value = newPlaying
        val ch = getTransportMidiChannel0Based()
        bleMidiManager.sendControlChange(channel = ch, ccNumber = 51, value = 127)
    }

    fun sendTransportNext() {
        val ch = getTransportMidiChannel0Based()
        bleMidiManager.sendControlChange(channel = ch, ccNumber = 49, value = 127)
    }

    fun toggleRoutesExpanded(group: OutputGroup, expanded: Boolean) {
        viewModelScope.launch {
            repository.saveRoutesExpanded(group, expanded)
        }
    }

    fun toggleSongChannelsExpanded(group: OutputGroup, expanded: Boolean) {
        viewModelScope.launch {
            repository.saveSongChannelsExpanded(group, expanded)
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

        // Handle Clear Solo CC (CC 94)
        if (ccNumber == 94 && value >= 64) {
            viewModelScope.launch {
                for (chIdx in 0..18) {
                    repository.saveSoloState(group, chIdx, false)
                }
            }
            return
        }

        for (chIndex in 0..18) {
            val chCc = mapping.getChannelCc(group, chIndex)
            val key = "${group.name}_$chIndex"
            when (ccNumber) {
                chCc.volumeCc -> {
                    val clampedValue = value.coerceIn(0, 127)
                    val vols = _volumes.value
                    if (vols[key] != clampedValue) {
                        val mutableVols = vols.toMutableMap()
                        mutableVols[key] = clampedValue
                        _volumes.value = mutableVols
                    }
                }
                chCc.panCc -> {
                    val clampedValue = value.coerceIn(0, 127)
                    val pns = _pans.value
                    if (pns[key] != clampedValue) {
                        val mutablePns = pns.toMutableMap()
                        mutablePns[key] = clampedValue
                        _pans.value = mutablePns
                    }
                }
                chCc.muteCc -> {
                    val shouldMute = value >= 64
                    viewModelScope.launch {
                        repository.saveMuteState(group, chIndex, shouldMute)
                    }
                }
                chCc.soloCc -> {
                    val shouldSolo = value >= 64
                    viewModelScope.launch {
                        repository.saveSoloState(group, chIndex, shouldSolo)
                    }
                }
            }
        }
    }
}
