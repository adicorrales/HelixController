package adco.helixmidimixer.data.model

data class ChannelConfig(
    val index: Int,
    val defaultName: String,
    val type: ChannelType,
    val colorCategory: ChannelColorCategory
) {
    companion object {
        val DEFAULT_CONFIGS: List<ChannelConfig> = buildList {
            // Channel 0: Home
            add(ChannelConfig(0, "Home", ChannelType.HOME, ChannelColorCategory.LIGHT_GREEN))
            // Channel 1: Song Master
            add(ChannelConfig(1, "Song Master", ChannelType.SONG_MASTER, ChannelColorCategory.LIGHT_SILVER))
            // Channels 2..9: Song 1 .. Song 8
            for (i in 1..8) {
                add(ChannelConfig(i + 1, "Song $i", ChannelType.SONG_CHANNEL, ChannelColorCategory.LIGHT_SILVER))
            }
            // Channel 10: BT Audio
            add(ChannelConfig(10, "BT Audio", ChannelType.BT_AUDIO, ChannelColorCategory.LIGHT_BLUE))
        }

        fun getConfigForIndex(index: Int): ChannelConfig {
            return DEFAULT_CONFIGS.getOrNull(index) ?: ChannelConfig(
                index = index,
                defaultName = "Channel ${index + 1}",
                type = ChannelType.SONG_CHANNEL,
                colorCategory = ChannelColorCategory.LIGHT_SILVER
            )
        }
    }
}
