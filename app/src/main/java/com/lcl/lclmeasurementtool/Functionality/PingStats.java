package com.lcl.lclmeasurementtool.Functionality;

/**
 * Ping is a functionality module that is able to
 * run ping test to test the reachability between a host and the current device.
 */
public class PingStats {
    private String host;
    private String loss;
    private double averageLatency;
    private double minLatency;
    private double maxLatency;
    private PingError error;
    private String fullOutput;

    public PingStats() {
        averageLatency = 0.0;
        minLatency = 0.0;
        maxLatency = 0.0;
        fullOutput = null;
        error = null;
    }

    public void setFullOutput(String fullOutput) {
        this.fullOutput = fullOutput;
    }

    public void setLoss(String loss) {
        this.loss = loss;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setError(PingError error) {
        this.error = error;
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

    public String getLoss() {
        return loss;
    }

    public String getHost() {
        return host;
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

    public String getFullOutput() {
        return fullOutput;
    }

    public PingError getError() {
        return error;
    }

    public boolean hasError() {
        return error.getCode() != 0;
    }
}
