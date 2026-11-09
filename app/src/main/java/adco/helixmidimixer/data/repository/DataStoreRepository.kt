package adco.helixmidimixer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import adco.helixmidimixer.data.model.HomeRoute
import adco.helixmidimixer.data.model.MidiCcMapping
import adco.helixmidimixer.data.model.OutputGroup
import adco.helixmidimixer.data.model.SongPlayMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.helixDataStore: DataStore<Preferences> by preferencesDataStore(name = "helix_midi_mixer_settings")

class DataStoreRepository(
    private val dataStore: DataStore<Preferences>
) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val MIDI_CC_MAPPING_KEY = stringPreferencesKey("midi_cc_mapping")

        fun channelNameKey(group: OutputGroup, channelIndex: Int): Preferences.Key<String> =
            stringPreferencesKey("ch_name_${group.name}_$channelIndex")

        fun songChannelNameKey(songIndex: Int): Preferences.Key<String> =
            stringPreferencesKey("song_ch_name_$songIndex")

        fun volumeKey(group: OutputGroup, channelIndex: Int): Preferences.Key<Int> =
            intPreferencesKey("vol_${group.name}_$channelIndex")

        fun panKey(group: OutputGroup, channelIndex: Int): Preferences.Key<Int> =
            intPreferencesKey("pan_${group.name}_$channelIndex")

        fun muteStateKey(group: OutputGroup, channelIndex: Int): Preferences.Key<Boolean> =
            booleanPreferencesKey("mute_${group.name}_$channelIndex")

        fun soloStateKey(group: OutputGroup, channelIndex: Int): Preferences.Key<Boolean> =
            booleanPreferencesKey("solo_${group.name}_$channelIndex")

        fun channelVisibilityKey(group: OutputGroup, channelIndex: Int): Preferences.Key<Boolean> =
            booleanPreferencesKey("visibility_${group.name}_$channelIndex")

        fun homeRouteKey(group: OutputGroup): Preferences.Key<String> =
            stringPreferencesKey("home_route_${group.name}")

        fun matrixMidiChannelKey(group: OutputGroup): Preferences.Key<Int> =
            intPreferencesKey("matrix_midi_ch_${group.name}")

        fun routesExpandedKey(group: OutputGroup): Preferences.Key<Boolean> =
            booleanPreferencesKey("routes_expanded_${group.name}")

        fun songChannelsExpandedKey(group: OutputGroup): Preferences.Key<Boolean> =
            booleanPreferencesKey("song_channels_expanded_${group.name}")

        val TRANSPORT_MIDI_CHANNEL_KEY: Preferences.Key<Int> =
            intPreferencesKey("transport_midi_channel")

        private val SONG_PLAY_MODE_KEY: Preferences.Key<String> =
            stringPreferencesKey("song_play_mode")
    }

    // --- Transport MIDI Channel ---
    fun getTransportMidiChannel(): Flow<Int> {
        return dataStore.data.map { prefs ->
            prefs[TRANSPORT_MIDI_CHANNEL_KEY] ?: 1
        }
    }

    suspend fun saveTransportMidiChannel(channel: Int) {
        dataStore.edit { prefs ->
            prefs[TRANSPORT_MIDI_CHANNEL_KEY] = channel.coerceIn(1, 16)
        }
    }

    // --- Song Play Mode ---
    fun getSongPlayMode(): Flow<SongPlayMode> {
        return dataStore.data.map { prefs ->
            val modeName = prefs[SONG_PLAY_MODE_KEY]
            modeName?.let { runCatching { SongPlayMode.valueOf(it) }.getOrNull() } ?: SongPlayMode.PLAY
        }
    }

    suspend fun saveSongPlayMode(mode: SongPlayMode) {
        dataStore.edit { prefs ->
            prefs[SONG_PLAY_MODE_KEY] = mode.name
        }
    }

    // --- Matrix MIDI Channels ---
    fun getMatrixMidiChannels(): Flow<Map<OutputGroup, Int>> {
        return dataStore.data.map { prefs ->
            val map = mutableMapOf<OutputGroup, Int>()
            for (group in OutputGroup.entries) {
                val defaultCh = when (group) {
                    OutputGroup.OUTPUT_1_4 -> 3
                    OutputGroup.XLR -> 4
                    OutputGroup.PHONES -> 5
                }
                map[group] = prefs[matrixMidiChannelKey(group)] ?: defaultCh
            }
            map
        }
    }

    suspend fun saveMatrixMidiChannel(group: OutputGroup, channel: Int) {
        dataStore.edit { prefs ->
            prefs[matrixMidiChannelKey(group)] = channel.coerceIn(1, 16)
        }
    }

    // --- Routes Expanded State ---
    fun getRoutesExpanded(): Flow<Map<OutputGroup, Boolean>> {
        return dataStore.data.map { prefs ->
            val map = mutableMapOf<OutputGroup, Boolean>()
            for (group in OutputGroup.entries) {
                map[group] = prefs[routesExpandedKey(group)] ?: false // hidden by default on startup
            }
            map
        }
    }

    suspend fun saveRoutesExpanded(group: OutputGroup, expanded: Boolean) {
        dataStore.edit { prefs ->
            prefs[routesExpandedKey(group)] = expanded
        }
    }

    // --- Song Channels Expanded State ---
    fun getSongChannelsExpanded(): Flow<Map<OutputGroup, Boolean>> {
        return dataStore.data.map { prefs ->
            val map = mutableMapOf<OutputGroup, Boolean>()
            for (group in OutputGroup.entries) {
                map[group] = prefs[songChannelsExpandedKey(group)] ?: true // visible by default on startup
            }
            map
        }
    }

    suspend fun saveSongChannelsExpanded(group: OutputGroup, expanded: Boolean) {
        dataStore.edit { prefs ->
            prefs[songChannelsExpandedKey(group)] = expanded
        }
    }

    // --- Song Channel Names ---
    fun getSongChannelNames(): Flow<Map<Int, String>> {
        return dataStore.data.map { prefs ->
            val names = mutableMapOf<Int, String>()
            for (i in 1..8) {
                val name = prefs[songChannelNameKey(i)]
                if (!name.isNullOrBlank()) {
                    names[i] = name
                }
            }
            names
        }
    }

    suspend fun saveSongChannelName(songIndex: Int, name: String) {
        dataStore.edit { prefs ->
            prefs[songChannelNameKey(songIndex)] = name
        }
    }

    // --- Channel Name per Group/Channel ---
    fun getChannelName(group: OutputGroup, channelIndex: Int): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[channelNameKey(group, channelIndex)]
        }
    }

    suspend fun saveChannelName(group: OutputGroup, channelIndex: Int, name: String) {
        dataStore.edit { prefs ->
            prefs[channelNameKey(group, channelIndex)] = name
        }
    }

    // --- Volume States ---
    fun getVolumeStates(): Flow<Map<String, Int>> {
        return dataStore.data.map { prefs ->
            val map = mutableMapOf<String, Int>()
            for (group in OutputGroup.entries) {
                for (ch in 0..18) {
                    val vol = prefs[volumeKey(group, ch)] ?: 108
                    map["${group.name}_$ch"] = vol
                }
            }
            map
        }
    }

    suspend fun saveVolumeState(group: OutputGroup, channelIndex: Int, volume: Int) {
        dataStore.edit { prefs ->
            prefs[volumeKey(group, channelIndex)] = volume.coerceIn(0, 127)
        }
    }

    // --- Pan States ---
    fun getPanStates(): Flow<Map<String, Int>> {
        return dataStore.data.map { prefs ->
            val map = mutableMapOf<String, Int>()
            for (group in OutputGroup.entries) {
                for (ch in 0..18) {
                    val pan = prefs[panKey(group, ch)] ?: 64
                    map["${group.name}_$ch"] = pan
                }
            }
            map
        }
    }

    suspend fun savePanState(group: OutputGroup, channelIndex: Int, pan: Int) {
        dataStore.edit { prefs ->
            prefs[panKey(group, channelIndex)] = pan.coerceIn(0, 127)
        }
    }

    // --- Mute States ---
    fun getMuteStates(): Flow<Map<String, Boolean>> {
        return dataStore.data.map { prefs ->
            val map = mutableMapOf<String, Boolean>()
            for (group in OutputGroup.entries) {
                for (ch in 0..18) {
                    val isMuted = prefs[muteStateKey(group, ch)] ?: false
                    map["${group.name}_$ch"] = isMuted
                }
            }
            map
        }
    }

    suspend fun saveMuteState(group: OutputGroup, channelIndex: Int, isMuted: Boolean) {
        dataStore.edit { prefs ->
            prefs[muteStateKey(group, channelIndex)] = isMuted
        }
    }

    // --- Solo States ---
    fun getSoloStates(): Flow<Map<String, Boolean>> {
        return dataStore.data.map { prefs ->
            val map = mutableMapOf<String, Boolean>()
            for (group in OutputGroup.entries) {
                for (ch in 0..18) {
                    val isSoloed = prefs[soloStateKey(group, ch)] ?: false
                    map["${group.name}_$ch"] = isSoloed
                }
            }
            map
        }
    }

    suspend fun saveSoloState(group: OutputGroup, channelIndex: Int, isSoloed: Boolean) {
        dataStore.edit { prefs ->
            prefs[soloStateKey(group, channelIndex)] = isSoloed
        }
    }

    // --- Channel Visibilities ---
    fun getChannelVisibilities(): Flow<Map<String, Boolean>> {
        return dataStore.data.map { prefs ->
            val map = mutableMapOf<String, Boolean>()
            for (group in OutputGroup.entries) {
                for (ch in 0..18) {
                    val isVisible = prefs[channelVisibilityKey(group, ch)] ?: true
                    map["${group.name}_$ch"] = isVisible
                }
            }
            map
        }
    }

    suspend fun saveChannelVisibility(group: OutputGroup, channelIndex: Int, isVisible: Boolean) {
        dataStore.edit { prefs ->
            prefs[channelVisibilityKey(group, channelIndex)] = isVisible
        }
    }

    // --- Home Routes ---
    fun getHomeRoutes(): Flow<Map<OutputGroup, HomeRoute>> {
        return dataStore.data.map { prefs ->
            val map = mutableMapOf<OutputGroup, HomeRoute>()
            for (group in OutputGroup.entries) {
                val routeName = prefs[homeRouteKey(group)]
                val route = routeName?.let {
                    runCatching { HomeRoute.valueOf(it) }.getOrNull()
                } ?: HomeRoute.ROUTE_1A
                map[group] = route
            }
            map
        }
    }

    suspend fun saveHomeRoute(group: OutputGroup, route: HomeRoute) {
        dataStore.edit { prefs ->
            prefs[homeRouteKey(group)] = route.name
        }
    }

    // --- MIDI CC Mapping ---
    fun getMidiCcMapping(): Flow<MidiCcMapping> {
        return dataStore.data.map { prefs ->
            val jsonString = prefs[MIDI_CC_MAPPING_KEY]
            if (jsonString.isNullOrEmpty()) {
                MidiCcMapping()
            } else {
                runCatching {
                    json.decodeFromString<MidiCcMapping>(jsonString)
                }.getOrDefault(MidiCcMapping())
            }
        }
    }

    suspend fun saveMidiCcMapping(mapping: MidiCcMapping) {
        dataStore.edit { prefs ->
            val jsonString = json.encodeToString(mapping)
            prefs[MIDI_CC_MAPPING_KEY] = jsonString
        }
    }
}
