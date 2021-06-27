package com.lcl.lclmeasurementtool.Utils;

public enum SignalStrength {
    NONE(0),      // the signal strength is none or unknown
    POOR(1),      // the signal strength is poor
    MODERATE(2),  // the signal strength is moderate
    GOOD(3),      // the signal strength is good
    GREAT(4);     // the signal strength is great

    // the numerical equivalence of the signal strength
    private final int levelCode;

    // constructor
    SignalStrength(int levelCode) {
        this.levelCode = levelCode;
    }

    /**
     * Get the numerical level code corresponding to the signal strength
     * @return an integer associates with the signal strength
     */
    public int getLevelCode() {
        return this.levelCode;
    }

    @Override
    public String toString() {
        return "SignalStrength{" +
                this.name() + " " +
                "levelCode=" + levelCode +
                '}';
    }
}
