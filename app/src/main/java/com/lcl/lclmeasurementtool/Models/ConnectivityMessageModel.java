//package com.lcl.lclmeasurementtool.Models;
//
//import com.squareup.moshi.Json;
//
///**
// * A model representing the message of a connectivity measurement
// */
//public class ConnectivityMessageModel extends MeasurementDataModel {
//
//    @Json(name = "upload_speed")
//    double upload_speed;
//
//    @Json(name = "download_speed")
//    double download_speed;
//
//    @Json(name = "ping")
//    double ping;
//
//    public ConnectivityMessageModel(double latitude,
//                                    double longitude,
//                                    String timestamp,
//                                    double upload_speed,
//                                    double download_speed,
//                                    double ping,
//                                    String cell_id,
//                                    String device_id) {
//        this.latitude = latitude;
//        this.longitude = longitude;
//        this.timestamp = timestamp;
//        this.upload_speed = upload_speed;
//        this.download_speed = download_speed;
//        this.ping = ping;
//        this.cell_id = cell_id;
//        this.device_id = device_id;
//    }
//}