package adco.helixmidimixer

import adco.helixmidimixer.midi.BleMidiPacketParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BleMidiPacketParserTest {

    @Test
    fun testCreateCcPacketFormat() {
        val channel = 1 // Channel 1 (1-based)
        val ccNumber = 11 // Volume CC
        val value = 127

        val packet = BleMidiPacketParser.createCcPacket(channel, ccNumber, value, timestampMs = 1000)

        // BLE-MIDI CC packet is 5 bytes (header, timestamp, status, cc, value)
        assertEquals(5, packet.size)
        // Header byte (0x80 | (timestamp >> 7) & 0x3F) for 1000 ms is 0x87
        assertEquals(0x87.toByte(), packet[0])
        // Timestamp byte (0x80 | timestamp & 0x7F) for 1000 ms is 0xE8
        assertEquals(0xE8.toByte(), packet[1])
        // Status byte for CC channel 1 (0-based 0) is 0xB0
        assertEquals(0xB0.toByte(), packet[2])
        // CC Number
        assertEquals(11.toByte(), packet[3])
        // CC Value
        assertEquals(127.toByte(), packet[4])
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

    @Test
    fun testOutputGroupDefaultMidiChannelsAndStatusBytes() {
        // OUTPUT_1_4 default = 3 (1-based), status 0xB2 (0-based 2)
        val packet1_4 = BleMidiPacketParser.createCcPacket(channel = 3, ccNumber = 11, value = 100)
        assertEquals(0xB2.toByte(), packet1_4[2])

        // XLR default = 4 (1-based), status 0xB3 (0-based 3)
        val packetXlr = BleMidiPacketParser.createCcPacket(channel = 4, ccNumber = 11, value = 100)
        assertEquals(0xB3.toByte(), packetXlr[2])

        // PHONES default = 5 (1-based), status 0xB4 (0-based 4)
        val packetPhones = BleMidiPacketParser.createCcPacket(channel = 5, ccNumber = 11, value = 100)
        assertEquals(0xB4.toByte(), packetPhones[2])
    }

    @Test
    fun testCreateAndParseRoundTrip() {
        val packet = BleMidiPacketParser.createCcPacket(channel = 3, ccNumber = 11, value = 95)
        val messages = BleMidiPacketParser.parse(packet)
        assertEquals(1, messages.size)
        assertEquals(2, messages[0].channel) // 0-based channel
        assertEquals(11, messages[0].ccNumber)
        assertEquals(95, messages[0].value)
    }
}
