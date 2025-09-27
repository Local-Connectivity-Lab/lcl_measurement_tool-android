package com.lcl.lclmeasurementtool.util

import com.lcl.lclmeasurementtool.model.datamodel.BaseMeasureDataModel
import com.lcl.lclmeasurementtool.model.datamodel.MeasurementReportModel
import com.lcl.lclmeasurementtool.model.datamodel.UserData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun prepareReportData(measureDataModel: BaseMeasureDataModel, userData: UserData): String {
    // Validate that required keys are present
    if (userData.skT.isEmpty || userData.hPKR.isEmpty) {
        throw IllegalStateException("Missing required cryptographic keys. User needs to re-authenticate.")
    }
    
    val serialized = Json.encodeToString(measureDataModel).toByteArray()
    val skTBytes = userData.skT.toByteArray()
    
    // Additional validation to ensure we have valid key data
    if (skTBytes.isEmpty()) {
        throw IllegalStateException("Invalid private key data. User needs to re-authenticate.")
    }
    
    val sig_m = ECDSA.Sign(serialized, ECDSA.DeserializePrivateKey(skTBytes))
    val report = MeasurementReportModel(Hex.encodeHexString(sig_m), Hex.encodeHexString(userData.hPKR.toByteArray()), Hex.encodeHexString(serialized), userData.showData)
    return Json.encodeToString(report)
}