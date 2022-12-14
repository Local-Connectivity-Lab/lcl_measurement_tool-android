package com.lcl.lclmeasurementtool.database.Entity;

/**
 * The enum representing the data stored in the database
 */
public enum EntityEnum {
    // connectivity
    CONNECTIVITY,

    // signal strength
    SIGNALSTRENGTH;

    /**
     * Retrieve the csv file name of the entity
     * @return file name of the entity
     */
    public String getFileName() {
        switch (this) {
            case CONNECTIVITY:
                return "connectivity";
            case SIGNALSTRENGTH:
                return "signal_strength";
            default: return "unknown_data";
        }
    }

    /**
     * Retrieve the header of the entity table
     * @return the headers of the entity
     */
    public String[] getHeader() {
        switch (this) {
            case CONNECTIVITY:
                return Connectivity.getHeader();
            case SIGNALSTRENGTH:
                return SignalStrength.getHeader();
            default: return new String[0];
        }
    }
}
