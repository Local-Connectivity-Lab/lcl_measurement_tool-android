package com.lcl.lclmeasurementtool.Utils;

/**
 * An enumeration that maps data transfer rate unit, i.e. kbps, Mb/s
 * based on its unit and magnitude.
 */
public enum DataTransferRateUnit {
    Kilobit(Unit.Bit, Magnitude.Kilo),
    Kilobyte(Unit.Byte, Magnitude.Kilo),
    Megabit(Unit.Bit, Magnitude.Mega),
    Megabyte(Unit.Byte, Magnitude.Mega),
    Gigabit(Unit.Bit, Magnitude.Giga),
    Gigabyte(Unit.Byte, Magnitude.Giga);

    // the unit of the rate.
    private Unit unit;

    // the magnitude of the rate.
    private Magnitude magnitude;

    /**
     * Initialize a DataTransferRateUnit following the unit and magnitude of the rate.
     * @param unit the unit of the data transfer rate.
     * @param magnitude the magnitude of the data transfer rate.
     */
    DataTransferRateUnit(Unit unit, Magnitude magnitude) {
        this.unit = unit;
        this.magnitude = magnitude;
    }

    /**
     * Return the <code>Unit</code> of the enum value.
     * @return the corresponding unit of the enum value.
     */
    public Unit getUnit() {
        return this.unit;
    }

    /**
     * Retrieve the String representation of the data transfer rate unit.
     * @return a string representation of the data transfer rate.
     */
    public String getUnitString() {
        return this.magnitude.getUnitString() + this.unit.getUnitString() + "/s";
    }

    /**
     * Return the <code>Magnitude</code> of the enum value.
     * @return the corresponding magnitude of the enum value.
     */
    public Magnitude getMagnitude() {
        return this.magnitude;
    }

    /**
     * An enumeration that represents the magnitude of a unit.
     */
    public enum Magnitude implements AbstractDataTransferRate {
        Kilo(1),      // Kilo
        Mega(2),      // Mega
        Giga(3);      // Giga

        /**
         * the numeric level code for each magnitude. 1 = kilo, 2 = mega, 3 = giga
         */
        private final int level;

        /**
         * The base conversion rate between any two adjacent magnitudes.
         */
        public static final int BASE_CONVERSION_RATE = 1000;

        /**
         * Construct a Magnitude enum object.
         * @param level the level corresponding to the magnitude object
         */
        Magnitude(int level) {
            this.level = level;
        }

        @Override
        public int getLevel() {
            return this.level;
        }

        @Override
        public String getUnitString() {
            switch (this) {
                case Kilo:
                    return "K";
                case Mega:
                    return "M";
                case Giga:
                    return "G";
            }
            return null;
        }
    }

    /**
     * An enumeration that represents the counting unit of a data transfer rate.
     */
    public enum Unit implements AbstractDataTransferRate {
        Bit(1),      // Bit
        Byte(2);     // Byte

        /**
         * the numeric level code for each unit. 1 = bit, 2 = byte
         */
        private final int level;

        /**
         * The base conversion rate between any two adjacent magnitudes.
         */
        public static final int BASE_CONVERSION_RATE = 8;

        /**
         * Construct a Unit enum object.
         * @param level the numeric representation of the Unit object
         */
        Unit(int level) {
            this.level = level;
        }

        @Override
        public int getLevel() {
            return this.level;
        }

        @Override
        public String getUnitString() {
            switch (this) {
                case Bit:
                    return "b";
                case Byte:
                    return "B";
            }
            return null;
        }
    }
}
