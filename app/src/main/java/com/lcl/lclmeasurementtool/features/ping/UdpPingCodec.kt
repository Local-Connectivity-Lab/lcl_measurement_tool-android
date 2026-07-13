package com.lcl.lclmeasurementtool.features.ping

import java.nio.ByteBuffer
import java.nio.ByteOrder

class UdpPingCodec {
    companion object {
        const val MAGIC: Int = 0x4C434C50 // "LCLP"
        const val HEADER_SIZE: Int = 4 + 1 + 8 + 4 + 8
        val VERSION: UByte = 1u
    }

    fun encodeRequest(packet: UdpPingPacket): ByteArray {
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

    fun decodeResponse(data: ByteArray): UdpPingPacket {
        require(data.size >= HEADER_SIZE) {
            "UDP ping packet too small: ${data.size} < $HEADER_SIZE"
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

        return UdpPingPacket(
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
