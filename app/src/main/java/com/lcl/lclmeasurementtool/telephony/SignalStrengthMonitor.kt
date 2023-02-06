package com.lcl.lclmeasurementtool.telephony

import android.telephony.SignalStrength
import kotlinx.coroutines.flow.Flow

interface SignalStrengthMonitor {
    val signalStrength: Flow<SignalStrength>
}