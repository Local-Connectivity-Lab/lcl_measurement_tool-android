package com.lcl.lclmeasurementtool.model.repository

import kotlinx.coroutines.flow.Flow

interface HistoryDataRepository<T> {
    fun getAll(): Flow<List<T>>
    suspend fun insert(data: T)
}