package com.lcl.lclmeasurementtool.modules

import com.lcl.lclmeasurementtool.networking.RetrofitLCLNetwork
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providesLCLNetworkApi(): RetrofitLCLNetwork = RetrofitLCLNetwork()
}