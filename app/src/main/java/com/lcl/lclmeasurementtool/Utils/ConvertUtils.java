package com.lcl.lclmeasurementtool.Utils;

public class ConvertUtils {

    // the conversion rate between megabit and megabyte
    public static final int CONVERSION_RATE = 8;

    /**
     * Convert data in MBps to Mbps
     *
     * @param MBps the data to be converted in MBps
     * @return corresponding data in Mbps
     */
    public static double toMbps(double MBps) {
        return MBps * CONVERSION_RATE;
    }

    /**
     * Convert data in Mbps to MBps
     *
     * @param Mbps the data to be converted in Mbps
     * @return corresponding data in MBps
     */
    public static double toMBps(double Mbps) {
        return Mbps / CONVERSION_RATE;
    }
}
