package com.lcl.lclmeasurementtool.telephony

import androidx.compose.ui.graphics.Color


enum class SignalStrengthLevelEnum(val level: Int) {

    NONE(0) {
        override fun color(): Color {
            return Color.LightGray
        }
    },

    // the signal strength is poor or unknown
    POOR(1) {
        override fun color(): Color {
            return Color.Red
        }
    },

    // the signal strength is weak
    MODERATE(2) {
        override fun color(): Color {
            return Color(0xFFF47E4C) // orange
        }
    },

    // the signal strength is moderate
    GOOD(3) {
        override fun color(): Color {
            return Color(0xFF81FF50) // light green
        }
    },

    // the signal strength is good
    GREAT(4) {
        override fun color(): Color {
            return Color.Green
        }
    };

    abstract fun color(): Color

    companion object {
        fun init(value: Int): SignalStrengthLevelEnum {
            return SignalStrengthLevelEnum.values().first { it.level == value }
        }
    }
}