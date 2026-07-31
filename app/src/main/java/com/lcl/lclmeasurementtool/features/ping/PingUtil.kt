package com.lcl.lclmeasurementtool.features.ping

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

class PingUtil {
    companion object {
        const val TAG = "PING"

        suspend fun doPing(address: String, times: Int, timeout: Long) : PingResult {
            return withContext(Dispatchers.IO) {
                try {
                    val target = parsePingTarget(address)
                    val requestId = Random.nextLong()
                    val client: PingClient = SocketPingClient()
                    val rtts = mutableListOf<Double>()
                    var lost = 0
                    var firstError: String? = null

                    Log.d(TAG, "============")
                    Log.d(TAG, "Ping starts: ${target.host}:${target.port}")
                    Log.d(TAG, "============")

                    if (times <= 0) {
                        return@withContext PingResult(
                            error = PingError(
                                code = PingErrorCase.IO,
                                message = "Ping requires times > 0",
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
                            is PingAttemptResult.Success -> {
                                rtts += attemptResult.rttMs
                            }
                            PingAttemptResult.Timeout -> {
                                lost += 1
                            }
                            is PingAttemptResult.Error -> {
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
                                message = firstError ?: "Ping timed out for all attempts",
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

        private fun parsePingTarget(address: String): PingTarget {
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
                return PingTarget(host = host, port = port)
            }

            val colonCount = trimmed.count { it == ':' }
            if (colonCount == 1) {
                val split = trimmed.split(':', limit = 2)
                val host = split[0]
                val port = split[1].toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid port in address: $address")
                require(host.isNotBlank()) { "Host cannot be blank" }
                require(port in 1..65535) { "Port out of range: $port" }
                return PingTarget(host = host, port = port)
            }

            throw IllegalArgumentException(
                "Address must include an explicit port, e.g. host:port or [ipv6]:port"
            )
        }

        private data class PingTarget(val host: String, val port: Int)
    }
}
