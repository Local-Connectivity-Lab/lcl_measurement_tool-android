package com.lcl.lclmeasurementtool.features.ping

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.math.sqrt

class PingUtil {
    companion object {
        const val TAG = "PING"
        private const val DEFAULT_UDP_PORT = 31337

        suspend fun doPing(address: String, times: Int, timeout: Long) : PingResult {
            return withContext(Dispatchers.IO) {
                try {
                    val target = parseUdpTarget(address)
                    val requestId = UUID.randomUUID().mostSignificantBits xor UUID.randomUUID().leastSignificantBits
                    val client = UdpPingClient()
                    val rtts = mutableListOf<Double>()
                    var lost = 0
                    var firstError: String? = null

                    Log.d(TAG, "============")
                    Log.d(TAG, "UDP ping starts: ${target.host}:${target.port}")
                    Log.d(TAG, "============")

                    if (times <= 0) {
                        return@withContext PingResult(
                            error = PingError(
                                code = PingErrorCase.IO,
                                message = "UDP ping requires times > 0",
                            ),
                        )
                    }

                    repeat(times) { sequence ->
                        when (val attemptResult = client.pingOnce(
                            host = target.host,
                            port = target.port,
                            timeoutMs = timeout,
                            requestId = requestId,
                            sequence = sequence,
                        )) {
                            is UdpPingAttemptResult.Success -> {
                                rtts += attemptResult.rttMs
                            }
                            UdpPingAttemptResult.Timeout -> {
                                lost += 1
                            }
                            is UdpPingAttemptResult.Error -> {
                                lost += 1
                                if (firstError == null) {
                                    firstError = attemptResult.message
                                }
                            }
                        }
                    }

                    if (rtts.isEmpty()) {
                        return@withContext PingResult(
                            error = PingError(
                                code = PingErrorCase.IO,
                                message = firstError ?: "UDP ping timed out for all attempts",
                            ),
                        )
                    }

                    val minRtt = rtts.minOrNull() ?: 0.0
                    val avgRtt = rtts.average()
                    val maxRtt = rtts.maxOrNull() ?: 0.0
                    val variance = rtts.map { (it - avgRtt) * (it - avgRtt) }.average()
                    val mdev = sqrt(variance)
                    val packetLossPercent = ((lost.toDouble() / times.toDouble()) * 100.0).roundToInt()

                    PingResult(
                        numLoss = packetLossPercent.toString(),
                        min = formatMs(minRtt),
                        avg = formatMs(avgRtt),
                        max = formatMs(maxRtt),
                        mdev = formatMs(mdev),
                        error = PingError(code = PingErrorCase.OK),
                    )
                } catch (e: IOException) {
                    PingResult(error = PingError(PingErrorCase.IO, e.message))
                } catch (e: IllegalArgumentException) {
                    PingResult(error = PingError(PingErrorCase.PARSING, e.message))
                } catch (e: Exception) {
                    PingResult(error = PingError(PingErrorCase.OTHER, e.message))
                }
            }
        }

        private fun formatMs(value: Double): String = String.format(Locale.US, "%.3f", value)

        private fun parseUdpTarget(address: String): UdpTarget {
            val trimmed = address.trim()
            require(trimmed.isNotEmpty()) { "Address cannot be empty" }

            if (trimmed.startsWith("[") && trimmed.contains("]:")) {
                val closingBracket = trimmed.indexOf(']')
                require(closingBracket > 1 && closingBracket + 2 < trimmed.length) {
                    "Invalid host:port format: $address"
                }
                val host = trimmed.substring(1, closingBracket)
                val port = trimmed.substring(closingBracket + 2).toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid port in address: $address")
                require(port in 1..65535) { "Port out of range: $port" }
                return UdpTarget(host = host, port = port)
            }

            val colonCount = trimmed.count { it == ':' }
            if (colonCount == 1) {
                val split = trimmed.split(':', limit = 2)
                val host = split[0]
                val port = split[1].toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid port in address: $address")
                require(host.isNotBlank()) { "Host cannot be blank" }
                require(port in 1..65535) { "Port out of range: $port" }
                return UdpTarget(host = host, port = port)
            }

            return UdpTarget(host = trimmed, port = DEFAULT_UDP_PORT)
        }

        private data class UdpTarget(val host: String, val port: Int)
    }
}