package com.lcl.lclmeasurementtool.Utils;

/**
 * The SignalStrength represents the cellular signal strength
 * received from the mobile network system.
 */
public enum SignalStrengthLevel {
    NONE(0),      // the signal strength is none or unknown
    POOR(1),      // the signal strength is poor
    MODERATE(2),  // the signal strength is moderate
    GOOD(3),      // the signal strength is good
    GREAT(4);     // the signal strength is great

    // the numerical equivalence of the signal strength
    private final int levelCode;

    // constructor
    SignalStrengthLevel(int levelCode) {
        this.levelCode = levelCode;
    }

    /**
     * TODO: finish comment
     * @param levelCode
     * @return
     */
    public static SignalStrengthLevel init(int levelCode) {
        switch (levelCode) {
            case 0:
                return SignalStrengthLevel.NONE;
            case 1:
                return SignalStrengthLevel.POOR;
            case 2:
                return SignalStrengthLevel.MODERATE;
            case 3:
                return SignalStrengthLevel.GOOD;
            case 4:
                return SignalStrengthLevel.GREAT;
            default: throw new IllegalArgumentException("Signal Strength levelCode should be >=0 and <= 4 ");
        }
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
