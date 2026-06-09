package com.lcl.lclmeasurementtool.model.repository

import android.util.Log
import com.lcl.lclmeasurementtool.model.datamodel.Site
import com.lcl.lclmeasurementtool.networking.RetrofitLCLNetwork
import javax.inject.Inject

interface SiteRepository {
    suspend fun getSites(): List<Site>
}

class LCLSiteRepository @Inject constructor(
    private val dataSource: RetrofitLCLNetwork
) : SiteRepository {
    
    companion object {
        private const val TAG = "LCLSiteRepository"
    }
    
    override suspend fun getSites(): List<Site> {
        return try {
            dataSource.getSites()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch sites: ${e.message}", e)
            emptyList()
        }
    }
}
