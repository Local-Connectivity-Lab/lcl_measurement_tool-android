package com.lcl.lclmeasurementtool.features.iperf

data class IperfConfig(
    var mServerAddr: String,
    var mServerPort: Int,
    var isDownMode: Boolean,
    var interval: Double,
    var bandwidth: Long = BANDWIDTH_1M,
    var formatUnit: Char = 'm',
    var parallels: Int = 1,
    var userName: String,
    var password: String,
    var rsaKey: String
) {
    companion object {
        const val BANDWIDTH_1M: Long = 1000 * 1000
        const val BANDWIDTH_10M: Long = 10 * BANDWIDTH_1M
        const val BANDWIDTH_1000M: Long = 1000 * BANDWIDTH_1M
    }
}
