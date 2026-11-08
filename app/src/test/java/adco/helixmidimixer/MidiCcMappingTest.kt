package adco.helixmidimixer

import adco.helixmidimixer.data.model.ChannelCc
import adco.helixmidimixer.data.model.MidiCcMapping
import adco.helixmidimixer.data.model.OutputGroup
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MidiCcMappingTest {

    @Test
    fun testDefaultCcMappingsHelixStadiumSpec() {
        val mapping = MidiCcMapping()

        // Test Output Group 1 (1/4") Channel 0 (Home)
        val g1Ch0 = mapping.getChannelCc(OutputGroup.OUTPUT_1_4, 0)
        assertEquals(11, g1Ch0.volumeCc)
        assertEquals(34, g1Ch0.panCc)
        assertEquals(57, g1Ch0.muteCc)
        assertEquals(80, g1Ch0.soloCc)

        // Test Output Group 1 (1/4") Channel 1 (Route 1A)
        val g1Ch1 = mapping.getChannelCc(OutputGroup.OUTPUT_1_4, 1)
        assertEquals(12, g1Ch1.volumeCc)
        assertEquals(35, g1Ch1.panCc)
        assertEquals(58, g1Ch1.muteCc)
        assertEquals(81, g1Ch1.soloCc)

        // Test Output Group 1 (1/4") Channel 14 (BT Audio)
        val g1Ch14 = mapping.getChannelCc(OutputGroup.OUTPUT_1_4, 14)
        assertEquals(28, g1Ch14.volumeCc)
        assertEquals(51, g1Ch14.panCc)
        assertEquals(74, g1Ch14.muteCc)
        assertEquals(97, g1Ch14.soloCc)

        // Test Output Group 2 (XLR) Channel 0 (Home) - index offset 15
        val g2Ch0 = mapping.getChannelCc(OutputGroup.XLR, 0)
        assertEquals(26, g2Ch0.volumeCc)
        assertEquals(49, g2Ch0.panCc)
        assertEquals(72, g2Ch0.muteCc)
        assertEquals(95, g2Ch0.soloCc)

        // Test Output Group 2 (XLR) Channel 7 - index offset 15
        val g2Ch7 = mapping.getChannelCc(OutputGroup.XLR, 7)
        assertEquals(11 + 7 + 15, g2Ch7.volumeCc)
        assertEquals(34 + 7 + 15, g2Ch7.panCc)
        assertEquals(57 + 7 + 15, g2Ch7.muteCc)
        assertEquals(80 + 7 + 15, g2Ch7.soloCc)
    }

    @Test
    fun testCustomCcMappingUpdate() {
        var mapping = MidiCcMapping()
        val customCc = ChannelCc(volumeCc = 5, panCc = 6, muteCc = 7, soloCc = 8)

        mapping = mapping.updateChannelCc(OutputGroup.PHONES, 3, customCc)

        val retrieved = mapping.getChannelCc(OutputGroup.PHONES, 3)
        assertEquals(5, retrieved.volumeCc)
        assertEquals(6, retrieved.panCc)
        assertEquals(7, retrieved.muteCc)
        assertEquals(8, retrieved.soloCc)
    }

    @Test
    fun testJsonSerializationDeserialization() {
        val json = Json { ignoreUnknownKeys = true }
        var original = MidiCcMapping()
        original = original.updateChannelCc(OutputGroup.XLR, 0, ChannelCc(100, 101, 102, 103))

        val encoded = json.encodeToString(original)
        assertNotNull(encoded)

        val decoded = json.decodeFromString<MidiCcMapping>(encoded)
        val xlrCh0 = decoded.getChannelCc(OutputGroup.XLR, 0)
        assertEquals(100, xlrCh0.volumeCc)
        assertEquals(101, xlrCh0.panCc)
        assertEquals(102, xlrCh0.muteCc)
        assertEquals(103, xlrCh0.soloCc)
    }
}
