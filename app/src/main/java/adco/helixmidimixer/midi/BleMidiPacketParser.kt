package adco.helixmidimixer.midi

object BleMidiPacketParser {

    /**
     * Builds a standard BLE MIDI packet containing a single Control Change (CC) message.
     * Header byte: 0x80 | (timestampHigh & 0x3F)
     * Timestamp byte: 0x80 | (timestampLow & 0x7F)
     * Status byte: 0xB0 | (channel & 0x0F)
     * CC Number: ccNumber & 0x7F
     * CC Value: value & 0x7F
     */
    fun createCcPacket(
        channel: Int,
        ccNumber: Int,
        value: Int,
        timestampMs: Long = System.currentTimeMillis()
    ): ByteArray {
        val ts = (timestampMs and 0x1FFF).toInt() // 13-bit timestamp
        val header = 0x80 or ((ts shr 7) and 0x3F)
        val timestamp = 0x80 or (ts and 0x7F)
        val status = 0xB0 or (channel and 0x0F)
        val data1 = ccNumber and 0x7F
        val data2 = value and 0x7F

        return byteArrayOf(
            header.toByte(),
            timestamp.toByte(),
            status.toByte(),
            data1.toByte(),
            data2.toByte()
        )
    }

    /**
     * Parses incoming BLE MIDI packet bytes into a list of [MidiCcMessage] objects.
     */
    fun parse(bytes: ByteArray): List<MidiCcMessage> {
        if (bytes.size < 3) return emptyList()

        val messages = mutableListOf<MidiCcMessage>()
        var i = 0

        // In BLE MIDI, byte 0 is the header byte (bit 7 set)
        if ((bytes[0].toInt() and 0x80) != 0) {
            i = 1
        }

        var runningStatus: Int? = null

        while (i < bytes.size) {
            var b = bytes[i].toInt() and 0xFF

            // In BLE MIDI, a timestamp byte (bit 7 set) precedes a status byte or running status data
            if ((b and 0x80) != 0 && i + 1 < bytes.size && (bytes[i + 1].toInt() and 0x80) != 0) {
                // bytes[i] is a timestamp byte preceding a status byte
                i++
                if (i >= bytes.size) break
                b = bytes[i].toInt() and 0xFF
            }

            if ((b and 0x80) != 0) {
                // High bit set: MIDI status byte
                if (b in 0xB0..0xBF) {
                    // Control Change status byte
                    runningStatus = b
                    val channel = b and 0x0F
                    if (i + 2 < bytes.size) {
                        val ccNum = bytes[i + 1].toInt() and 0x7F
                        val ccVal = bytes[i + 2].toInt() and 0x7F
                        messages.add(MidiCcMessage(channel, ccNum, ccVal))
                        i += 3
                    } else {
                        break
                    }
                } else if (b in 0x80..0xAF || b in 0xC0..0xEF) {
                    // Other channel voice status byte
                    runningStatus = b
                    val dataLen = if (b in 0xC0..0xDF) 1 else 2
                    i += 1 + dataLen
                } else {
                    // System Realtime / Common / Header byte
                    i++
                }
            } else {
                // High bit is 0: Data byte. Check running status for Control Change
                if (runningStatus != null && runningStatus in 0xB0..0xBF) {
                    if (i + 1 < bytes.size) {
                        val ccNum = b and 0x7F
                        val ccVal = bytes[i + 1].toInt() and 0x7F
                        val channel = runningStatus and 0x0F
                        messages.add(MidiCcMessage(channel, ccNum, ccVal))
                        i += 2
                    } else {
                        break
                    }
                } else {
                    i++
                }
            }
        }

        return messages
    }
}
