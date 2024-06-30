package com.lcl.lclmeasurementtool.util

import com.lcl.lclmeasurementtool.model.datamodel.BaseMeasureDataModel
import com.lcl.lclmeasurementtool.model.datamodel.MeasurementReportModel
import com.lcl.lclmeasurementtool.model.datamodel.UserData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun prepareReportData(measureDataModel: BaseMeasureDataModel, userData: UserData): String {
    val serialized = Json.encodeToString(measureDataModel).toByteArray()
    val sig_m = ECDSA.Sign(serialized, ECDSA.DeserializePrivateKey(userData.skT.toByteArray()))
    val report = MeasurementReportModel(Hex.encodeHexString(sig_m), Hex.encodeHexString(userData.hPKR.toByteArray()), Hex.encodeHexString(serialized), userData.showData)
    return Json.encodeToString(report)
}