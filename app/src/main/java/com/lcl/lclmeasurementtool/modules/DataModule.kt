package com.lcl.lclmeasurementtool.modules

import com.lcl.lclmeasurementtool.datasource.ConnectivityMonitorDataSource
import com.lcl.lclmeasurementtool.datasource.LocationDataSource
import com.lcl.lclmeasurementtool.datasource.SignalStrengthDataSource
import com.lcl.lclmeasurementtool.datasource.SimStateMonitorDataSource
import com.lcl.lclmeasurementtool.location.LocationService
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel
import com.lcl.lclmeasurementtool.model.repository.*
import com.lcl.lclmeasurementtool.networking.NetworkMonitor
import com.lcl.lclmeasurementtool.networking.SimStateMonitor
import com.lcl.lclmeasurementtool.telephony.SignalStrengthMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindsUserDataRepository(
        userDataRepository: LocalUserDataRepository
    ): UserDataRepository

    @Binds
    fun bindsNetworkMonitor(
        networkMonitor: ConnectivityMonitorDataSource
    ): NetworkMonitor

    @Binds
    fun bindsSimStateMonitor(
        simStateMonitor: SimStateMonitorDataSource
    ): SimStateMonitor

    @Binds
    fun bindsLocationService(
        locationService: LocationDataSource
    ): LocationService

    @Binds
    fun bindsSignalStrengthMonitor(
        signalStrengthDataSource: SignalStrengthDataSource
    ): SignalStrengthMonitor

    @Binds
    fun bindsNetworkAPI(
        lclApiRepository: LCLApiRepository
    ): NetworkApiRepository

    @Binds
    fun bindsSignalStrengthRepository(
        measurementRepository: SignalStrengthRepository
    ): HistoryDataRepository<SignalStrengthReportModel>

    @Binds
    fun bindsConnectivityRepository(
        measurementRepository: ConnectivityRepository
    ): HistoryDataRepository<ConnectivityReportModel>
}