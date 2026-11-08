package adco.helixmidimixer.data.model

data class ChannelState(
    val group: OutputGroup,
    val channelIndex: Int,
    val customName: String? = null,
    val volume: Int = 0,
    val pan: Int = 64,
    val isMuted: Boolean = false,
    val isSoloed: Boolean = false,
    val homeRoute: HomeRoute = HomeRoute.ROUTE_1A
) {
    val config: ChannelConfig
        get() = ChannelConfig.getConfigForIndex(channelIndex)

    val effectiveName: String
        get() = customName?.ifBlank { null } ?: config.defaultName
}
