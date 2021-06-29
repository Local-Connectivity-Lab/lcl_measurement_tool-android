package com.lcl.lclmeasurementtool.Utils;

public class ConvertUtils {

    /**
     * the conversion rate between megabit and megabyte.
     */
    public static final int CONVERSION_RATE = 8;

    /**
     * Convert data in MBps to Mbps.
     *
     * @param MBps the data to be converted in MBps.
     * @throws IllegalArgumentException if MBps is less than 0.
     * @return corresponding data in Mbps.
     */
    public static double toMbps(double MBps) {
        if (MBps < 0) {
            throw new IllegalArgumentException("the input parameter MBps should be greater than 0");
        }
        return MBps * CONVERSION_RATE;
    }

    /**
     * Convert data in Mbps to MBps.
     *
     * @param Mbps the data to be converted in Mbps.
     * @throws IllegalArgumentException if Mbps is less than 0.
     * @return corresponding data in MBps.
     */
    public static double toMBps(double Mbps) {
        if (Mbps < 0) {
            throw new IllegalArgumentException("the input parameter Mbps should be greater than 0");
        }
        return Mbps / CONVERSION_RATE;
    }

    public static double convert(DataTransferRateUnit from,
                                 DataTransferRateUnit to,
                                 double data) {

        double unitConversionRate = 1.0;
        if (!from.getUnit().equals(to.getUnit())) {
            int diff = from.getUnit().getLevel() - to.getUnit().getLevel();
            unitConversionRate = Math.pow(DataTransferRateUnit.Unit.BASE_CONVERSION_RATE, diff);
        }

        double magnitudeConversionRate = 1.0;
        if (!from.getMagnitude().equals(to.getMagnitude())) {
            int diff = from.getMagnitude().getLevel() - to.getMagnitude().getLevel();
            magnitudeConversionRate = Math.pow(DataTransferRateUnit.Magnitude.BASE_CONVERSION_RATE,
                                                diff);
        }

        return data * unitConversionRate * magnitudeConversionRate;

    }
}
