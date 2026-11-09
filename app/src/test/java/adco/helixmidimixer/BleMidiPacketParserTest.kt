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

        // Raw MIDI CC packet is 3 bytes (status, cc, value) without 0x80 header bytes
        assertEquals(3, packet.size)
        // Status byte for CC channel 0 is 0xB0
        assertEquals(0xB0.toByte(), packet[0])
        // CC Number
        assertEquals(11.toByte(), packet[1])
        // CC Value
        assertEquals(127.toByte(), packet[2])
    }

    @Test
    fun testParseRawCcPacket() {
        // Create a 3-byte raw MIDI CC packet for Channel 3 (0xB2), CC 34, Value 64
        val packet = byteArrayOf(
            0xB2.toByte(), // CC Channel 3 (0-indexed 2)
            34.toByte(),   // CC Number
            64.toByte()    // CC Value
        )

        val messages = BleMidiPacketParser.parse(packet)

        assertEquals(1, messages.size)
        val msg = messages[0]
        assertEquals(2, msg.channel)
        assertEquals(34, msg.ccNumber)
        assertEquals(64, msg.value)
    }

    @Test
    fun testParseBleMidiCcPacketWithHeader() {
        // Create a 5-byte BLE MIDI packet with header for Channel 2 (0xB1), CC 34, Value 64
        val packet = byteArrayOf(
            0x80.toByte(), // Header
            0x80.toByte(), // Timestamp
            0xB1.toByte(), // CC Channel 2 (0-indexed 1)
            34.toByte(),   // CC Number
            64.toByte()    // CC Value
        )

        val messages = BleMidiPacketParser.parse(packet)

        assertEquals(1, messages.size)
        val msg = messages[0]
        assertEquals(1, msg.channel)
        assertEquals(34, msg.ccNumber)
        assertEquals(64, msg.value)
    }

    @Test
    fun testParseShortOrEmptyBytes() {
        val emptyList = BleMidiPacketParser.parse(byteArrayOf(0x80.toByte()))
        assertTrue(emptyList.isEmpty())
    }
}
