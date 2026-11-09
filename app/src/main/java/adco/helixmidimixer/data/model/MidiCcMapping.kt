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
            val groupOffset = 0 // Identical across Ch 3 (OUTPUT_1_4), Ch 4 (XLR), and Ch 5 (PHONES) layers
            val idx = channelIndex.coerceIn(0, 18)
            return ChannelCc(
                volumeCc = (11 + idx + groupOffset).coerceAtMost(127),
                panCc = (34 + idx + groupOffset).coerceAtMost(127),
                muteCc = (57 + idx + groupOffset).coerceAtMost(127),
                soloCc = (80 + idx + groupOffset).coerceAtMost(127)
            )
        }

        fun createDefaultMappings(): Map<String, ChannelCc> {
            val map = mutableMapOf<String, ChannelCc>()
            for (group in OutputGroup.entries) {
                for (ch in 0..18) {
                    map[makeKey(group, ch)] = defaultChannelCc(group, ch)
                }
            }
            return map
        }
    }
}
