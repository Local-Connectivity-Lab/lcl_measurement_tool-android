package com.lcl.lclmeasurementtool.modules

import com.lcl.lclmeasurementtool.model.repository.LocalUserDataRepository
import com.lcl.lclmeasurementtool.model.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindsUserDataRepository(userDataRepository: LocalUserDataRepository): UserDataRepository
}