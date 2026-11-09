package adco.helixmidimixer.data.model

enum class SongPlayMode(val displayName: String, val description: String) {
    PLAY("Play", "Immediately starts playback of the cued song."),
    LOAD("Load", "Loads the song, but does not automatically start its playback."),
    LOAD_PRESET("Load+Preset", "Loads the song and its Start Flag-Preset/Snapshot, but does not auto-play.")
}
