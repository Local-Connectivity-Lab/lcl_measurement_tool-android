package com.lcl.lclmeasurementtool.Utils;

public enum DataTransferRateUnit {
    Kilobit(Unit.Bit, Magnitude.Kilo),
    Kilobyte(Unit.Byte, Magnitude.Kilo),
    Megabit(Unit.Bit, Magnitude.Mega),
    Megabyte(Unit.Byte, Magnitude.Mega),
    Gigabit(Unit.Bit, Magnitude.Giga),
    Gigabyte(Unit.Byte, Magnitude.Giga);


    private Unit unit;
    private Magnitude magnitude;
    DataTransferRateUnit(Unit unit, Magnitude magnitude) {
        this.unit = unit;
        this.magnitude = magnitude;
    }

    public Unit getUnit() {
        return unit;
    }

    public Magnitude getMagnitude() {
        return magnitude;
    }

    public enum Magnitude {
        Kilo(1),
        Mega(2),
        Giga(3);

        private final int level;
        public static final int BASE_CONVERSION_RATE = 1000;

        Magnitude(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    public enum Unit {
        Bit(1),
        Byte(2);

        private final int level;
        public static final int BASE_CONVERSION_RATE = 8;

        Unit(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }
}
