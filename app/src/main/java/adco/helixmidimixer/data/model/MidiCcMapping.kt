package adco.helixmidimixer.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChannelCc(
    val volumeCc: Int,
    val panCc: Int,
    val muteCc: Int,
    val soloCc: Int
)

@Serializable
data class MidiCcMapping(
    val mappings: Map<String, ChannelCc> = createDefaultMappings()
) {
    fun getChannelCc(group: OutputGroup, channelIndex: Int): ChannelCc {
        val key = makeKey(group, channelIndex)
        return mappings[key] ?: defaultChannelCc(group, channelIndex)
    }

    fun updateChannelCc(group: OutputGroup, channelIndex: Int, newCc: ChannelCc): MidiCcMapping {
        val updatedMap = mappings.toMutableMap()
        updatedMap[makeKey(group, channelIndex)] = newCc
        return copy(mappings = updatedMap)
    }

    companion object {
        fun makeKey(group: OutputGroup, channelIndex: Int): String = "${group.name}_$channelIndex"

        fun defaultChannelCc(group: OutputGroup, channelIndex: Int): ChannelCc {
            val groupOffset = when (group) {
                OutputGroup.OUTPUT_1_4 -> 0
                OutputGroup.XLR -> 15
                OutputGroup.PHONES -> 30
            }
            return if (channelIndex == 14) {
                ChannelCc(
                    volumeCc = 28 + groupOffset,
                    panCc = 51 + groupOffset,
                    muteCc = 74 + groupOffset,
                    soloCc = 97 + groupOffset
                )
            } else {
                val idx = channelIndex.coerceIn(0, 13)
                ChannelCc(
                    volumeCc = 11 + idx + groupOffset,
                    panCc = 34 + idx + groupOffset,
                    muteCc = 57 + idx + groupOffset,
                    soloCc = 80 + idx + groupOffset
                )
            }
        }

        fun createDefaultMappings(): Map<String, ChannelCc> {
            val map = mutableMapOf<String, ChannelCc>()
            for (group in OutputGroup.entries) {
                for (ch in 0..14) {
                    map[makeKey(group, ch)] = defaultChannelCc(group, ch)
                }
            }
            return map
        }
    }
}
