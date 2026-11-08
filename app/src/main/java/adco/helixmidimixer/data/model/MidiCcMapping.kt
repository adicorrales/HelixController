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
                OutputGroup.XLR -> 11
                OutputGroup.PHONES -> 22
            }
            val index = groupOffset + channelIndex
            return ChannelCc(
                volumeCc = 11 + index,
                panCc = 34 + index,
                muteCc = 57 + index,
                soloCc = 80 + index
            )
        }

        fun createDefaultMappings(): Map<String, ChannelCc> {
            val map = mutableMapOf<String, ChannelCc>()
            for (group in OutputGroup.entries) {
                for (ch in 0..10) {
                    map[makeKey(group, ch)] = defaultChannelCc(group, ch)
                }
            }
            return map
        }
    }
}
