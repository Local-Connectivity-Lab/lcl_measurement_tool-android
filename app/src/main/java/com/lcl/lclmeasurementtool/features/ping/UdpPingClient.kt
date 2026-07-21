package com.lcl.lclmeasurementtool.features.ping

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import kotlin.math.max

sealed interface UdpPingAttemptResult {
    data class Success(val rttMs: Double, val response: UdpPingPacket) : UdpPingAttemptResult
    object Timeout : UdpPingAttemptResult
    data class Error(val message: String, val cause: Throwable? = null) : UdpPingAttemptResult
}

class UdpPingClient(
    private val codec: UdpPingCodec = UdpPingCodec(),
    private val receiveBufferSize: Int = 1024,
) : PingClient {
    override fun pingOnce(
        host: String,
        port: Int,
        timeoutMs: Long,
        requestId: Long,
        sequence: Int,
    ): UdpPingAttemptResult {
        val timeoutIntMs = max(1L, timeoutMs).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val packet = UdpPingPacket(
            requestId = requestId,
            sequence = sequence,
            clientSendTimestamp = System.currentTimeMillis(),
        )
        val payload = codec.encodeRequest(packet)
        val remoteAddress = InetSocketAddress(host, port)

        return try {
            DatagramSocket().use { socket ->
                socket.soTimeout = timeoutIntMs
                val sendPacket = DatagramPacket(payload, payload.size, remoteAddress)
                val receiveBuffer = ByteArray(receiveBufferSize)
                val receivePacket = DatagramPacket(receiveBuffer, receiveBuffer.size)

                val sendStartedAtNs = System.nanoTime()
                socket.send(sendPacket)
                socket.receive(receivePacket)
                val recvAtNs = System.nanoTime()

                val responseBytes = receivePacket.data.copyOf(receivePacket.length)
                val responsePacket = codec.decodeResponse(responseBytes)
                if (responsePacket.requestId != requestId || responsePacket.sequence != sequence) {
                    return UdpPingAttemptResult.Error(
                        message = "Mismatched response id/sequence: ${responsePacket.requestId}/${responsePacket.sequence}",
                    )
                }

                UdpPingAttemptResult.Success(
                    rttMs = (recvAtNs - sendStartedAtNs) / 1_000_000.0,
                    response = responsePacket,
                )
            }
        } catch (_: SocketTimeoutException) {
            UdpPingAttemptResult.Timeout
        } catch (e: IllegalArgumentException) {
            UdpPingAttemptResult.Error(message = e.message ?: "Invalid UDP ping packet", cause = e)
        } catch (e: IOException) {
            UdpPingAttemptResult.Error(message = e.message ?: "UDP socket I/O error", cause = e)
        }
    }
}
