package com.lcl.lclmeasurementtool.Utils;

import android.graphics.Color;
import android.content.Context;

import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;
import com.lcl.lclmeasurementtool.R;

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
     * Initialize a SignalStrengthLevel based on input levelCode
     * @param levelCode the input abstract representation of the Signal Strength;
     * @throws IllegalArgumentException if the input levelCode is less than 0 or greater than 4.
     * @return a SignalStrengthLevel Enum associated with the input levelCode.
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
            default: throw new IllegalArgumentException("Signal Strength levelCode should be >=0 and <= 4. Current value is " + levelCode);
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
        return this.name() + " " +
                "levelCode=" + levelCode;
    }

    public String getName() {
        switch (this) {
            case NONE:
                return "No Signal";
            case POOR:
                return "Poor";
            case MODERATE:
                return "Moderate";
            case GOOD:
                return "Good";
            case GREAT:
                return "Great";
            default:
                break;
        }

        return "";
    }

    public int getColor(Context context) {
        switch (this) {
            case GREAT:
                return Color.GREEN;
            case GOOD:
                return ContextCompat.getColor(context, R.color.light_green);
            case MODERATE:
                return ContextCompat.getColor(context, R.color.orange);
            case POOR:
                return Color.RED;
            case NONE:
                return ContextCompat.getColor(context, R.color.light_gray);
            default:break;
        }

        return -1;
    }
}
