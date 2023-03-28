package com.lcl.lclmeasurementtool.constants

class NetworkConstants {

    companion object {
        const val URL = "https://coverage.seattlecommunitynetwork.org/api/"
        const val REGISTRATION_ENDPOINT = "register"
        const val SIGNAL_ENDPOINT = "report_signal"
        const val CONNECTIVITY_ENDPOINT = "report_measurement"
        const val MEDIA_TYPE = "application/json"

        // NetworkTestViewModel Constants
        const val PING_TAG = "PING"
        const val IPERF_UP_TAG = "IPERF_UP"
        const val IPERF_DOWN_TAG = "IPERF_DOWN"
        const val WORKER_TAG = "backgroundTest"
        const val IPERF_COUNTS_TAG = "TIMES"
        const val IPERF_COUNTS = 5
        const val PING_TEST_ADDRESS = "google.com"
        const val PING_TEST_ADDRESS_TAG = "ADDRESS"
    }

}