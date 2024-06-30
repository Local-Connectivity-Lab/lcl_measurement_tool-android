package com.lcl.lclmeasurementtool.networking

import kotlinx.coroutines.flow.Flow

interface SimStateMonitor {
    val isSimCardInserted: Flow<Boolean>
}