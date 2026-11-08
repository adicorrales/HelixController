package adco.helixmidimixer.data.model

data class ChannelConfig(
    val index: Int,
    val defaultName: String,
    val type: ChannelType,
    val colorCategory: ChannelColorCategory
) {
    companion object {
        val DEFAULT_CONFIGS: List<ChannelConfig> = buildList {
            // Channel 0: Home (All paths)
            add(ChannelConfig(0, "Home", ChannelType.HOME, ChannelColorCategory.LIGHT_GREEN))
            // Channel 1: Route 1A
            add(ChannelConfig(1, "Route 1A", ChannelType.ROUTE, ChannelColorCategory.LIGHT_GREEN))
            // Channel 2: Route 1B
            add(ChannelConfig(2, "Route 1B", ChannelType.ROUTE, ChannelColorCategory.LIGHT_GREEN))
            // Channel 3: Route 2A
            add(ChannelConfig(3, "Route 2A", ChannelType.ROUTE, ChannelColorCategory.LIGHT_GREEN))
            // Channel 4: Route 2B
            add(ChannelConfig(4, "Route 2B", ChannelType.ROUTE, ChannelColorCategory.LIGHT_GREEN))
            // Channel 5: Song Master
            add(ChannelConfig(5, "Song Master", ChannelType.SONG_MASTER, ChannelColorCategory.LIGHT_SILVER))
            // Channels 6..13: Song 1 .. Song 8
            for (i in 1..8) {
                add(ChannelConfig(5 + i, "Song $i", ChannelType.SONG_CHANNEL, ChannelColorCategory.LIGHT_SILVER))
            }
            // Channel 14: BT Audio
            add(ChannelConfig(14, "BT Audio", ChannelType.BT_AUDIO, ChannelColorCategory.LIGHT_BLUE))
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
