package com.lcl.lclmeasurementtool.features.ping

import java.nio.ByteBuffer
import java.nio.ByteOrder

class PingCodec {
    companion object {
        const val MAGIC: Int = 0x4C434C50 // "LCLP"
        const val HEADER_SIZE: Int = 4 + 1 + 8 + 4 + 8
        val VERSION: UByte = 1u
    }

    /**
     * Encode a ping request packet to bytes.
     *
     * @param packet the packet to encode
     * @return the encoded packet as a byte array
     * @throws IllegalArgumentException if packet magic or version is invalid
     */
    fun encodeRequest(packet: PingPacket): ByteArray {
        require(packet.magic == MAGIC) { "Unexpected magic: ${packet.magic}" }
        require(packet.version == VERSION) { "Unsupported version: ${packet.version}" }

        return ByteBuffer.allocate(HEADER_SIZE).order(ByteOrder.BIG_ENDIAN).apply {
            putInt(packet.magic)
            put(packet.version.toByte())
            putLong(packet.requestId)
            putInt(packet.sequence)
            putLong(packet.clientSendTimestamp)
        }.array()
    }

    /**
     * Decode a ping response packet from bytes.
     *
     * Reconstructs a [PingPacket] from a byte array, validating the magic number
     * and version. Server-provided timestamps (receive/send) are optional.
     *
     * @param data the encoded packet bytes
     * @return the decoded packet
     * @throws IllegalArgumentException if packet is too small, has invalid magic, or unsupported version
     */
    fun decodeResponse(data: ByteArray): PingPacket {
        require(data.size >= HEADER_SIZE) {
            "Ping packet too small: ${data.size} < $HEADER_SIZE"
        }

        val buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN)
        val magic = buffer.int
        require(magic == MAGIC) { "Unexpected magic: $magic" }

        val version = buffer.get().toUByte()
        require(version == VERSION) { "Unsupported version: $version" }

        val requestId = buffer.long
        val sequence = buffer.int
        val clientSendTimestamp = buffer.long
        val serverReceiveTimestamp = if (buffer.remaining() >= Long.SIZE_BYTES) buffer.long else null
        val serverSendTimestamp = if (buffer.remaining() >= Long.SIZE_BYTES) buffer.long else null

        return PingPacket(
            magic = magic,
            version = version,
            requestId = requestId,
            sequence = sequence,
            clientSendTimestamp = clientSendTimestamp,
            serverReceiveTimestamp = serverReceiveTimestamp,
            serverSendTimestamp = serverSendTimestamp,
        )
    }
}
