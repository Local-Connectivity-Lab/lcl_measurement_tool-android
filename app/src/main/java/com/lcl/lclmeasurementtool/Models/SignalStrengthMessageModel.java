//package com.lcl.lclmeasurementtool.Models;
//
//import com.squareup.moshi.Json;
//
///**
// * A model representing the message of a signal strength measurement
// */
//public class SignalStrengthMessageModel extends MeasurementDataModel {
//
//    @Json(name = "dbm")
//    int dbm;
//
//    @Json(name = "level_code")
//    int level_code;
//
//    public SignalStrengthMessageModel(double latitude,
//                                      double longitude,
//                                      String timestamp,
//                                      int dBm,
//                                      int level_code,
//                                      String cell_id,
//                                      String device_id) {
//        this.latitude = latitude;
//        this.longitude = longitude;
//        this.timestamp = timestamp;
//        this.dbm = dBm;
//        this.level_code = level_code;
//        this.cell_id = cell_id;
//        this.device_id = device_id;
//    }
//}