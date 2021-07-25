package com.lcl.lclmeasurementtool.Functionality;

import android.renderscript.ScriptGroup;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class IperfUtils {
    public static String TAG = "LCL_Iperf_IperfUtils";


    /**
     * @param host - the ip address
     * @param time - duration of the test
     * @param bandwidth - amount of data transferred
     * 1K means 1kilobit per second
     * 1M means 1 Megabit per second
     * 1G means 1 Gigabit per second
     */
    public static IperfStats launch(String host, double time, String bandwidth) throws IOException, InterruptedException {

        if (host == null) {
            throw new IllegalArgumentException("host should not be null");
        }
        if (time == 0) {
            throw new IllegalArgumentException("time should not be 0");

        }
        if (bandwidth == null) {
            throw new IllegalArgumentException("bandwidth should not be null");
        }

        IperfStats iperfStats = new IperfStats();
        StringBuilder result = new StringBuilder();
        Runtime runtime = Runtime.getRuntime();
        String command = "/system/bin/iperf3 -c "+ host + " -b " + bandwidth;
        //String command = "/system/bin/ping -c 1  192.168.1.30";
        Log.i(TAG,"Starting process " + command);
        Process process = runtime.exec(command);
        process.waitFor();
        int exitVal = process.exitValue();
        Log.i(TAG, "exitVal = : " + exitVal);

        switch (exitVal) {
            case 0:
                InputStreamReader reader = new InputStreamReader(process.getInputStream());
                BufferedReader buffer = new BufferedReader(reader);
                String output;

                while ((output = buffer.readLine()) != null) {
                    Log.i(TAG, output);
                    result.append(output).append("\n");
                }

                iperfStats.setFullOutput(result.toString());
                // Testing with custom iperf3 output.Uncomment if needed.
                // iperfStats.setFullOutput(iperfStats.setTestOutput());

                Log.i(TAG,iperfStats.getFullOutput());
                ParseIperfStats(iperfStats);
                return iperfStats;

            case 1:
                iperfStats.setError(new IperfError("test failed", exitVal));
                Log.e(TAG, "error failing to launch iperf3, exitVal = " + exitVal);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + exitVal);
        }

        process.destroy();
        return iperfStats;
    }


    public static void ParseIperfStats(IperfStats iperfStats) {
        String output = iperfStats.getFullOutput();

        //We split by the following to get to the final stats
        String[] split = output.split("- - - - - - - - - - - - - - - - - - - - - - - - -\n");

        //We are getting the different lines of the output in separate strings, one for sender stats, one for receiver stats
        String[] finalStats = split[1].split("\n");

        //parsing the output line for sender and receiver bandwidths
        int finalSpace = finalStats[1].indexOf("bits/sec") - 2;
        int initialSpace = finalStats[1].indexOf("Bytes") + 5;
        String senderBandwidth = finalStats[1].substring(initialSpace, finalSpace);
        Log.i(TAG,"senderBandwidth = " + senderBandwidth);
        iperfStats.setSenderBandwidth(Double.parseDouble(senderBandwidth));
        int finalSpace2 = finalStats[2].indexOf("bits/sec") - 2;
        int initialSpace2 = finalStats[2].indexOf("Bytes") + 5;
        String receiverBandwidth = finalStats[2].substring(initialSpace2, finalSpace2);
        Log.i(TAG, "receiverBandwidth = " + receiverBandwidth);
        iperfStats.setReceiverBandwidth(Double.parseDouble(receiverBandwidth));

        //parsing the output for sender and receiver bandwidth units
        int firstIndexOfSenderUnits = finalSpace + 1;
        int lastIndexOfSenderUnits = finalSpace + 10;
        iperfStats.setSenderBandwidthUnits(finalStats[1].substring(firstIndexOfSenderUnits,
                lastIndexOfSenderUnits));
        int firstIndexOfReceiverUnits = finalSpace2 + 1;
        int lastIndexOfReceiverUnits = finalSpace2 + 10;
        iperfStats.setReceiverBandwidthUnits(finalStats[2].substring(firstIndexOfReceiverUnits, lastIndexOfReceiverUnits));


    }
}
