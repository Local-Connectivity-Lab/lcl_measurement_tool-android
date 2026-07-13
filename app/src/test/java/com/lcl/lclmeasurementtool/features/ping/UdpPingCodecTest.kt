package com.lcl.lclmeasurementtool.features.ping

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class UdpPingCodecTest {
    private val codec = UdpPingCodec()

    @Test
    fun decodeResponse_roundTripWithOptionalTimestamps_returnsExpectedPacket() {
        val expected = UdpPingPacket(
            requestId = 1234L,
            sequence = 7,
            clientSendTimestamp = 5678L,
            serverReceiveTimestamp = 6789L,
            serverSendTimestamp = 7890L,
        )

        val encoded = ByteBuffer.allocate(UdpPingCodec.HEADER_SIZE + 16).order(ByteOrder.BIG_ENDIAN).apply {
            putInt(expected.magic)
            put(expected.version.toByte())
            putLong(expected.requestId)
            putInt(expected.sequence)
            putLong(expected.clientSendTimestamp)
            putLong(expected.serverReceiveTimestamp!!)
            putLong(expected.serverSendTimestamp!!)
        }.array()

        val decoded = codec.decodeResponse(encoded)
        assertEquals(expected.magic, decoded.magic)
        assertEquals(expected.version, decoded.version)
        assertEquals(expected.requestId, decoded.requestId)
        assertEquals(expected.sequence, decoded.sequence)
        assertEquals(expected.clientSendTimestamp, decoded.clientSendTimestamp)
        assertEquals(expected.serverReceiveTimestamp, decoded.serverReceiveTimestamp)
        assertEquals(expected.serverSendTimestamp, decoded.serverSendTimestamp)
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeResponse_invalidMagic_throws() {
        val encoded = ByteBuffer.allocate(UdpPingCodec.HEADER_SIZE).order(ByteOrder.BIG_ENDIAN).apply {
            putInt(0xDEADBEEF.toInt())
            put(UdpPingCodec.VERSION.toByte())
            putLong(1L)
            putInt(1)
            putLong(1L)
        }.array()

        codec.decodeResponse(encoded)
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeResponse_invalidVersion_throws() {
        val encoded = ByteBuffer.allocate(UdpPingCodec.HEADER_SIZE).order(ByteOrder.BIG_ENDIAN).apply {
            putInt(UdpPingCodec.MAGIC)
            put(99.toByte())
            putLong(1L)
            putInt(1)
            putLong(1L)
        }.array()

        codec.decodeResponse(encoded)
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeResponse_tooSmall_throws() {
        codec.decodeResponse(ByteArray(UdpPingCodec.HEADER_SIZE - 1))
    }

    @Test
    fun decodeResponse_withoutServerTimestamps_setsNullServerTimestamps() {
        val encoded = codec.encodeRequest(
            UdpPingPacket(
                requestId = 99L,
                sequence = 10,
                clientSendTimestamp = 777L,
            ),
        )

        val decoded = codec.decodeResponse(encoded)
        assertNull(decoded.serverReceiveTimestamp)
        assertNull(decoded.serverSendTimestamp)
    }
}
