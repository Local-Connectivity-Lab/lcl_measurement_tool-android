package com.lcl.lclmeasurementtool.Utils;

import android.graphics.Color;
import android.content.Context;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.lcl.lclmeasurementtool.R;

/**
 * The SignalStrength represents the cellular signal strength
 * received from the mobile network system.
 */
public enum SignalStrengthLevel {
    POOR(0),      // the signal strength is poor or unknown
    WEAK(1),      // the signal strength is weak
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
        Log.i("SIG", "levelCode is :" + levelCode);
        switch (levelCode) {
            case 0:
                return SignalStrengthLevel.POOR;
            case 1:
                return SignalStrengthLevel.WEAK;
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

    /**
     * Retrieve the name of the signal strength.
     *
     * @return the string representation of the signal strength level.
     */
    public String getName() {
        switch (this) {
            case POOR:
                return "Poor";
            case WEAK:
                return "Weak";
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

    /**
     * Retrieve the color representation of the signal strength.
     * @param context the context of the application/activity
     * @return the color representation of the signal strength.
     */
    public int getColor(Context context) {
        switch (this) {
            case GREAT:
                return Color.GREEN;
            case GOOD:
                return ContextCompat.getColor(context, R.color.light_green);
            case MODERATE:
                return ContextCompat.getColor(context, R.color.orange);
            case WEAK:
                return Color.RED;
            case POOR:
                return ContextCompat.getColor(context, R.color.light_gray);
            default:break;
        }

        return -1;
    }
}
