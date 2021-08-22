package com.lcl.lclmeasurementtool.Utils;

/**
 * The abstract interface of a data transfer rate
 */
public interface AbstractDataTransferRate {
    /**
     * Retrieve the level code of current enum value.
     * @return an numeric representation of the enumeration value.
     */
    int getLevel();

    /**
     * Retrieve the String representation of the unit.
     * @return a string representation of the unit.
     *         Null value will be returned if the enum mapping fails.
     */
    String getUnitString();
}
