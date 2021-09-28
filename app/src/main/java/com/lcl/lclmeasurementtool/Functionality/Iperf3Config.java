package com.lcl.lclmeasurementtool.Functionality;

public class Iperf3Config {
    public static final long BANDWIDTH_1M = 1000 * 1000;
    public static final long BANDWIDTH_10M = 10 * BANDWIDTH_1M;
    public static final long BANDWIDTH_1000M = 1000 * BANDWIDTH_1M;


    public String mServerAddr;
    public int mServerPort;
    public boolean isDownMode;
    public double interval = 1.0;
    public long bandwidth = BANDWIDTH_1000M;
    public char formatUnit = 'm';
    public int parallels = 1;

    public Iperf3Config() {
    }

    public Iperf3Config(String mServerAddr, int mServerPort) {
        this.mServerAddr = mServerAddr;
        this.mServerPort = mServerPort;
    }

    public Iperf3Config(String mServerAddr, int mServerPort, int parallels) {
        config(mServerAddr, mServerPort, parallels);
    }

    public void config(String mServerAddr, int mServerPort, int parallels) {
        this.mServerAddr = mServerAddr;
        this.mServerPort = mServerPort;
        this.parallels = parallels;
    }

    public void config(String mServerAddr, int mServerPort, int parallels, boolean isDownMode) {
        config(mServerAddr, mServerPort, parallels);
        this.isDownMode = isDownMode;
    }
}
