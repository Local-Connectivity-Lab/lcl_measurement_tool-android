package com.lcl.lclmeasurementtool.networking

import com.lcl.lclmeasurementtool.datasource.APKAutoUpdaterDataSource

interface APKAutoUpdater {

    var shouldForceUpdate: Boolean
    var latestRelease: APKAutoUpdaterDataSource.Release
    suspend fun canUpdate(): Boolean
}