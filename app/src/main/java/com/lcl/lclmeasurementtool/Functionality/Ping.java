package com.lcl.lclmeasurementtool.Functionality;

import android.util.Log;

import androidx.navigation.NavBackStackEntry;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.time.Clock;

/**
 * class declaration that encapsulates the Ping functionality
 */
public class Ping {
    /**
     * instance variable declarations
     */

    private String host;
    private int numPings;
    private String pingOutput;
    private Object RuntimeException;

    /**
     * Ping constructor
     * @param host represents site or IP address being pinged
     * @param numPings represents number of pings to host
     */

    public Ping(String host, int numPings) {
        this.host = host;
        this.numPings = numPings;
        this.pingOutput = "";
    }

    /**
     * method launches the ping process
     * @return a string consisting of the output from the process containing network information
     *
     *     64 bytes from server-13-226-214-69.lax50.r.cloudfront.net (13.226.214.69): icmp_seq=5 ttl=226 time=41.7 ms
     *
     *     --- espn.com ping statistics ---
     *     5 packets transmitted, 5 received, 0% packet loss, time 4004ms
     *     rtt min/avg/max/mdev = 38.705/40.685/41.955/1.159 ms
     */

    public String launchPing() {
        Log.i("LCL_PING, ", "launchPing: entering ");
        try {
            Runtime runtime = Runtime.getRuntime();
            String command = "/system/bin/ping -c " + numPings + " " + host;
            Log.i("LCL_PING", command);

            // wait for the process to exit
            Process proc = Runtime.getRuntime().exec(command);
            final int exit;
            exit = proc.waitFor();
            Log.i("LCL_PING", "exit code = " +  exit);


            InputStreamReader reader = new InputStreamReader(proc.getInputStream());
            BufferedReader buffer = new BufferedReader(reader);
            String line;
            while ((line = buffer.readLine()) != null) {
                pingOutput += line;
                pingOutput += "\n";
               // Log.i("LCL_PING", "Ping output == " + line);
            }

            Log.i("LCL_PING", "Ping output == " + pingOutput);
        }
        catch (IOException | SecurityException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        Log.i("LCL_PING, ", "launchPing: exiting ");
        return pingOutput;
    }

    /**
     * method to obtain the percentage of packet loss
     * @return returns a string in the form "x % packet loss"
     * @throws Throwable RuntimeException
     */


    public String getPacketLoss() throws Throwable {
        if (pingOutput == null) {
            throw (Throwable) RuntimeException;
        }

        String[] split = pingOutput.split(",");
        return split[2];
    }

    /**
     * obtains the ping quantity
     * @return a double representing the latency in milliseconds
     * @throws Throwable RuntimeException
     */
    public PingStats getLatency() throws Throwable {
        Log.i("LCL_PING, ", "getLatency: entering ");
        if (pingOutput == null) {
            throw (Throwable) RuntimeException;
        }
        Log.i("LCL_PING", "getLatency: ");
        Log.i("LCL_PING", "pingOutput equals : " + pingOutput);
        PingStats pingStats = new PingStats();
        String[] split = pingOutput.split("/");
        String minStr = split[3];
        String actualMinStr = split[3].substring(split[3].indexOf("=")+2);
        String avgStr = split[4];
        String maxStr = split[5];
        Log.i("LCL_PING", "min = " + actualMinStr);
        Log.i("LCL_PING", "average = " + avgStr);
        Log.i("LCL_PING", "max = " + maxStr);

        // convert strings to double
        pingStats.setMinLatency(Double.parseDouble(actualMinStr));
        pingStats.setAverageLatency(Double.parseDouble(avgStr));
        pingStats.setMaxLatency(Double.parseDouble(maxStr));

        Log.i("LCL_PING, ", "getLatency: exiting ");
        return pingStats;
    }
}
