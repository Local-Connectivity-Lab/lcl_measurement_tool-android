package com.lcl.lclmeasurementtool.Constants;

public class NetworkConstants {
    public static final String URL = "https://api-dev.seattlecommunitynetwork.org/api";
    public static final String REGISTRATION_ENDPOINT = "/register";
    public static final String SIGNAL_ENDPOINT = "/report_signal";
    public static final String CONNECTIVITY_ENDPOINT = "/report_measurement";

    // NetworkTestViewModel Constants
    public static final String PING_TAG = "PING";
    public static final String IPERF_UP_TAG = "IPERF_UP";
    public static final String IPERF_DOWN_TAG = "IPERF_DOWN";
    public static final String WORKER_TAG = "backgroundTest";
    public static final String IPERF_COUNTS_TAG = "TIMES";
    public static final int IPERF_COUNTS = 5;
    public static final String IPERF_TEST_ADDRESS = "google.com";
    public static final String IPERF_TEST_ADDRESS_TAG = "ADDRESS";
}
