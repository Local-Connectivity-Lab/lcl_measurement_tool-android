package com.lcl.lclmeasurementtool.features.ping

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class PingUtil {
    companion object {
        const val TAG = "PING"

        suspend fun doPing(address: String, times: Int, timeout: Long) : PingResult {
            val runtime: Runtime = Runtime.getRuntime()

            // execute ping command
            val command = "/system/bin/ping -c $times -W $timeout $address"

            try {
                val process = withContext(Dispatchers.IO) {
                    runtime.exec(command)
                }
                Log.d(TAG, "============")
                Log.d(TAG, "Ping starts: $address")
                Log.d(TAG, "============")
                val exitCode = withContext(Dispatchers.IO) {
                    process.waitFor()
                }
                Log.d(TAG, "exit code is: $exitCode")

                val pingResult: PingResult

                when(exitCode) {
                    0 -> {
                        val reader = BufferedReader(InputStreamReader(process.inputStream))
                        val line: String = reader.use {
                            it.readText().trimIndent()
                        }
                        Log.d(TAG, "result:\n$line")

                        val regex = "(\\d+)% packet loss.+rtt.+= (\\d*.?\\d+)/(\\d*.?\\d+)/(\\d*.?\\d+)/(\\d*.?\\d+)".toRegex(
                            setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
                        )
                        val match = regex.find(line)!!
                        val (numLoss, min, avg, max, mdev) = match.destructured
                        Log.d(TAG, "$numLoss, $min, $avg, $max, $mdev")
                        pingResult = PingResult(
                            numLoss = numLoss,
                            min = min,
                            avg = avg,
                            max = max,
                            mdev = mdev,
                            error = PingError(code = PingErrorCase.OK)
                        )
                    }
                    else -> {
                        val err = process.errorStream.bufferedReader().use { it.readText() }
                        pingResult = PingResult(error = PingError(code = PingErrorCase.IO, message = err))
                        Log.d(TAG, "error: $err")
                    }
                }

                process.destroy()
                return pingResult
            } catch (e: IOException) {
                return PingResult(error = PingError(PingErrorCase.IO, e.message))
            } catch (e: IndexOutOfBoundsException) {
                return PingResult(error = PingError(PingErrorCase.PARSING, e.message))
            } catch (e: Exception) {
                return PingResult(error = PingError(PingErrorCase.OTHER, e.message))
            }
        }
    }
}