package com.lcl.lclmeasurementtool.telephony

import android.telephony.SignalStrength
import com.lcl.lclmeasurementtool.model.datamodel.Site
import kotlinx.coroutines.flow.Flow

interface SignalStrengthMonitor {
    val signalStrength: Flow<SignalStrength>
    fun getCellID(): String
    fun getCellIDFromSite(latitude: Double, longitude: Double, sites: List<Site>): String?
}