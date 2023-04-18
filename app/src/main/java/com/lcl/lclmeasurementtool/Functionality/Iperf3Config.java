package com.lcl.lclmeasurementtool.Functionality;

@Deprecated
/**
 * Configuration for iperf test
 */
public class Iperf3Config {
    public static final long BANDWIDTH_1M = 1000 * 1000;
    public static final long BANDWIDTH_10M = 10 * BANDWIDTH_1M;
    public static final long BANDWIDTH_1000M = 1000 * BANDWIDTH_1M;

    // server address
    public String mServerAddr;

    // server port
    public int mServerPort;

    // is downstream mode?
    public boolean isDownMode;

    // test interval
    public double interval = 1.0;

    // test bandwidth
    public long bandwidth = BANDWIDTH_1000M;

    // output unit
    public char formatUnit = 'm';

    // number of parallel test
    public int parallels = 1;

    // username
    public String userName;

    // password
    public String password;

    // rsa key
    public String rsaKey;

    public Iperf3Config() {
    }

    public Iperf3Config(String mServerAddr, int mServerPort) {
        this.mServerAddr = mServerAddr;
        this.mServerPort = mServerPort;
    }

    public Iperf3Config(String mServerAddr, int mServerPort, int parallels) {
        config(mServerAddr, mServerPort, parallels);
    }

    /**
     * Set up the iperf configuration
     * @param mServerAddr  the server address
     * @param mServerPort  the server port
     * @param parallels    number of parallel tests
     */
    public void config(String mServerAddr, int mServerPort, int parallels) {
        this.mServerAddr = mServerAddr;
        this.mServerPort = mServerPort;
        this.parallels = parallels;
    }

    /**
     * Set up the iperf configuration
     * @param mServerAddr the server address
     * @param mServerPort the server port
     * @param parallels   number of parallel tests
     * @param isDownMode  whether the test is downstream mode
     */
    public void config(String mServerAddr, int mServerPort, int parallels, boolean isDownMode) {
        config(mServerAddr, mServerPort, parallels);
        this.isDownMode = isDownMode;
    }
}
