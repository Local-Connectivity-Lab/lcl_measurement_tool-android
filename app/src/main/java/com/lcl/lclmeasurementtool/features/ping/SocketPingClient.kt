package com.lcl.lclmeasurementtool.features.ping

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SocketPingClient(
    private val codec: PingCodec = PingCodec(),
    private val receiveBufferSize: Int = 1024,
) : PingClient {
    override suspend fun pingOnce(
        host: String,
        port: Int,
        timeoutMs: Long,
        requestId: Long,
        sequence: Int,
    ): PingClient.PingResult = withContext(Dispatchers.IO) {
        val timeoutIntMs = max(1L, timeoutMs).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val packet = PingPacket(
            requestId = requestId,
            sequence = sequence,
            clientSendTimestamp = System.currentTimeMillis(),
        )
        val payload = codec.encodeRequest(packet)
        val remoteAddress = InetSocketAddress(host, port)

        try {
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
                    return@withContext PingClient.PingResult.Error(
                        message = "Mismatched response id/sequence: ${responsePacket.requestId}/${responsePacket.sequence}",
                    )
                }

                PingClient.PingResult.Success(
                    rttMs = (recvAtNs - sendStartedAtNs) / 1_000_000.0,
                    response = responsePacket,
                )
            }
        } catch (_: SocketTimeoutException) {
            PingClient.PingResult.Timeout
        } catch (e: IllegalArgumentException) {
            PingClient.PingResult.Error(message = e.message ?: "Invalid ping packet", cause = e)
        } catch (e: IOException) {
            PingClient.PingResult.Error(message = e.message ?: "Socket I/O error", cause = e)
        }
    }
}
