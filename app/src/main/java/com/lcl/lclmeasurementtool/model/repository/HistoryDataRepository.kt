package com.lcl.lclmeasurementtool.model.repository

import com.lcl.lclmeasurementtool.sync.Syncable
import kotlinx.coroutines.flow.Flow

interface HistoryDataRepository<T>: Syncable {
    fun getAll(): Flow<List<T>>
    suspend fun insert(data: T)
    suspend fun update(data: T)
}