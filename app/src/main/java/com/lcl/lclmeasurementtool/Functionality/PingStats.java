package com.lcl.lclmeasurementtool.Functionality;

/**
 * Ping is a functionality module that is able to
 * run ping test to test the reachability between a host and the current device.
 */
public class PingStats {
    private double averageLatency;
    private double minLatency;
    private double maxLatency;

    public PingStats() {
        averageLatency = 0.0;
        minLatency = 0.0;
        maxLatency = 0.0;
    }

    public String toString() {
        return "average: " + averageLatency + "min: " + minLatency + "max: " + maxLatency;
    }

    public void setAverageLatency(double d) {
        averageLatency = d;
    }

    public void setMinLatency(double d) {
        minLatency = d;
    }

    public void setMaxLatency(double d) {
        maxLatency = d;
    }

    public double getAverageLatency() {
        return averageLatency;
    }

    public double getMinLatency() {
        return minLatency;
    }

    public double getMaxLatency() {
        return maxLatency;
    }
}
