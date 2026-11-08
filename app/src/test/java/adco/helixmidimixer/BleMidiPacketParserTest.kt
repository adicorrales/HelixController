package adco.helixmidimixer

import adco.helixmidimixer.midi.BleMidiPacketParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BleMidiPacketParserTest {

    @Test
    fun testCreateCcPacketFormat() {
        val channel = 0 // Channel 1 (0-indexed)
        val ccNumber = 11 // Volume CC
        val value = 127

        val packet = BleMidiPacketParser.createCcPacket(channel, ccNumber, value, timestampMs = 1000)

        assertEquals(5, packet.size)
        // Header byte has 0x80 bit set
        assertTrue((packet[0].toInt() and 0x80) != 0)
        // Timestamp byte has 0x80 bit set
        assertTrue((packet[1].toInt() and 0x80) != 0)
        // Status byte for CC channel 0 is 0xB0
        assertEquals(0xB0.toByte(), packet[2])
        // CC Number
        assertEquals(11.toByte(), packet[3])
        // CC Value
        assertEquals(127.toByte(), packet[4])
    }

    @Test
    fun testParseCcPacket() {
        // Create a 5-byte BLE MIDI packet for Channel 2 (0xB2), CC 34, Value 64
        val packet = byteArrayOf(
            0x80.toByte(), // Header
            0x80.toByte(), // Timestamp
            0xB2.toByte(), // CC Channel 2
            34.toByte(),   // Pan CC
            64.toByte()    // Center pan
        )

        val messages = BleMidiPacketParser.parse(packet)

        assertEquals(1, messages.size)
        val msg = messages[0]
        assertEquals(2, msg.channel)
        assertEquals(34, msg.ccNumber)
        assertEquals(64, msg.value)
    }

    @Test
    fun testParseMultipleCcMessagesInPacket() {
        // BLE MIDI packet with two CC messages
        val packet = byteArrayOf(
            0x80.toByte(),
            0x80.toByte(),
            0xB0.toByte(), 11.toByte(), 100.toByte(), // Msg 1: Ch 0, CC 11, Val 100
            0x80.toByte(),
            0xB0.toByte(), 57.toByte(), 127.toByte()  // Msg 2: Ch 0, CC 57, Val 127 (Mute)
        )

        val messages = BleMidiPacketParser.parse(packet)

        assertEquals(2, messages.size)
        assertEquals(0, messages[0].channel)
        assertEquals(11, messages[0].ccNumber)
        assertEquals(100, messages[0].value)

        assertEquals(0, messages[1].channel)
        assertEquals(57, messages[1].ccNumber)
        assertEquals(127, messages[1].value)
    }

    @Test
    fun testParseShortOrEmptyBytes() {
        val emptyList = BleMidiPacketParser.parse(byteArrayOf(0x80.toByte(), 0x80.toByte()))
        assertTrue(emptyList.isEmpty())
    }
}
