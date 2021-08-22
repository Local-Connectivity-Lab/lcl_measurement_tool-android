package com.lcl.lclmeasurementtool.Utils;

/**
 * Utilities that will be used to convert units.
 */
public class ConvertUtils {

    /**
     * Convert data from one unit to the other.
     *
     * @param from the base unit to be converted from.
     * @param to   the destination unit to be converted to.
     * @param data the data whose unit will be converted.
     * @throws IllegalArgumentException if input data is less than 0.
     * @see DataTransferRateUnit
     * @return a double in the the destination unit.
     */
    public static double convert(DataTransferRateUnit from,
                                 DataTransferRateUnit to,
                                 double data) {
        if (data < 0) {
            throw new IllegalArgumentException("the input parameter Mbps should be greater than 0");
        }

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
