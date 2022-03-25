package com.lcl.lclmeasurementtool.Functionality;

import androidx.annotation.NonNull;

/**
 * Ping is a functionality module that is able to
 * run ping test to test the reachability between a host and the current device.
 */
public class PingStats {
    private String host;
    private double loss;
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

    /**
     * Set full output from the ping test
     * @param fullOutput  the raw output string
     */
    public void setFullOutput(String fullOutput) {
        this.fullOutput = fullOutput;
    }

    /**
     * Set package loss data
     * @param loss  the package loss data
     */
    public void setLoss(double loss) {
        this.loss = loss;
    }

    /**
     * Set the host data
     * @param host the host of the ping test
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Set error data
     * @param error  the error during the ping test
     */
    public void setError(PingError error) {
        this.error = error;
    }

    @NonNull
    @Override
    public String toString() {
        return "average: " + averageLatency + "min: " + minLatency + "max: " + maxLatency;
    }

    /**
     * Set the average latency
     * @param d  the average latency
     */
    public void setAverageLatency(double d) {
        averageLatency = d;
    }

    /**
     * Set the minimum latency
     * @param d the minimum latency from the test
     */
    public void setMinLatency(double d) {
        minLatency = d;
    }

    /**
     * Set the maximum latency
     * @param d the maximum latency from the test
     */
    public void setMaxLatency(double d) {
        maxLatency = d;
    }

    /**
     * Retrieve the package loss data
     * @return the data loss data
     */
    public double getLoss() {
        return loss;
    }

    /**
     * Retrieve the testing host
     * @return the host for the test
     */
    public String getHost() {
        return host;
    }

    /**
     * Retrieve the average latency for the ping test
     * @return the average latency for the ping test
     */
    public double getAverageLatency() {
        return averageLatency;
    }

    /**
     * Retrieve the minimum latency
     * @return the minimum latency during the ping test
     */
    public double getMinLatency() {
        return minLatency;
    }

    /**
     * Retrieve the maximum latency
     * @return the maximum latency during the ping test
     */
    public double getMaxLatency() {
        return maxLatency;
    }

    /**
     * Retrieve the full test output
     * @return the full test output
     */
    public String getFullOutput() {
        return fullOutput;
    }

    /**
     * Retrieve the error occurs during the ping test
     * @return the ping error representing the error during the test
     * @see PingError
     */
    public PingError getError() {
        return error;
    }

    /**
     * Return whether an error occurs during the test
     * @return true if there is an error; false otherwise
     */
    public boolean hasError() {
        return error.getCode() != 0;
    }
}
