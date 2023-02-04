package com.lcl.lclmeasurementtool.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationService {
//    val currentLocation: Flow<Location>
    fun lastLocation(): Flow<Location>
}