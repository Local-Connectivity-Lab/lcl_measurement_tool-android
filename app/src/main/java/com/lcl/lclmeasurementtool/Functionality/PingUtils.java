package com.lcl.lclmeasurementtool.Functionality;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Deprecated
/**
 *
 */
public class PingUtils {

    /**
     * Perform ping test given the ping settings
     *
     * @param address  the address to ping to
     * @param times    the number of times for the ping test
     * @param timeout  the timeout limit
     * @return         a <code>ping stats</code> corresponding to the test
     * @throws IOException           for networking error
     * @throws InterruptedException  for IO interruption
     * @see PingStats
     */
    public static PingStats ping(String address, int times, int timeout) throws IOException, InterruptedException {
        if (address == null) {
            throw new IllegalArgumentException("address should not be null");
        }

        // do ping
        PingStats pingStats = new PingStats();
        pingStats.setHost(address);

        StringBuilder result = new StringBuilder();
        Runtime runtime = Runtime.getRuntime();

        int timeoutSeconds = Math.max(timeout / 1000, 1); // timeout should be in second

        // execute ping command
        String command = "/system/bin/ping -c " + times + " " + address;
        Process process = runtime.exec(command);
        process.waitFor();
        int exitCode = process.exitValue();

        switch (exitCode) {
            case 0:
                // get result
                InputStreamReader reader = new InputStreamReader(process.getInputStream());
                BufferedReader buffer = new BufferedReader(reader);
                String line;
                while ((line = buffer.readLine()) != null) {
                    result.append(line).append("\n");
                }

                return getPingStats(pingStats, result.toString());
            case 1:
                pingStats.setError(new PingError("Ping failed", exitCode));
                break;
            default:
                pingStats.setError(new PingError("error occurred", exitCode));
                break;
        }

        process.destroy();
        return pingStats;
    }

    /**
     * Retrieve the ping statistics from raw string output
     * @param pingStats  the ping stat object holding the ping statistics
     * @param s          the raw output string from the ping test
     * @return           the ping stats with the given statistics from the test output
     * @see PingStats
     */
    public static PingStats getPingStats(PingStats pingStats, String s) {
        pingStats.setFullOutput(s);

        String[] sLoss = s.split(",");
        String[] sStats = s.split("/");
        String minStr = sStats[3];
        String actualMinStr = sStats[3].substring(sStats[3].indexOf("=")+2);
        String avgStr = sStats[4];
        String maxStr = sStats[5];
        String packetLoss = sLoss[2].trim().split("%")[0];

        // convert strings to double
        pingStats.setMinLatency(Double.parseDouble(actualMinStr));
        pingStats.setAverageLatency(Double.parseDouble(avgStr));
        pingStats.setMaxLatency(Double.parseDouble(maxStr));
        pingStats.setLoss(Double.parseDouble(packetLoss));

        PingError pingError = new PingError();
        pingError.setCode(0);
        if (s.contains("unknown host")) {
            pingError.setMessage("unknown host");
        } else {
            pingError.setMessage("unknown error in getPingStats");
        }
        pingStats.setError(pingError);

        return pingStats;
    }
}
