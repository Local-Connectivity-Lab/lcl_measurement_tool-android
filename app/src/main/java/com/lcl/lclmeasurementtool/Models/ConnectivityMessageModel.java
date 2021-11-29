package com.lcl.lclmeasurementtool.Models;

import com.fasterxml.jackson.annotation.JsonProperty;

// TODO(sudheesh001) security check
public class ConnectivityMessageModel {

    @JsonProperty("latitude")
    double latitude;

    @JsonProperty("longitude")
    double longitude;

    @JsonProperty("timestamp")
    String timestamp;

    @JsonProperty("upload_speed")
    double upload_speed;

    @JsonProperty("download_speed")
    double download_speed;

    @JsonProperty("ping")
    double ping;

    @JsonProperty("cell_id")
    String cell_id;

    @JsonProperty("device_id")
    String device_id;

    public ConnectivityMessageModel(double latitude,
                                    double longitude,
                                    String timestamp,
                                    double upload_speed,
                                    double download_speed,
                                    double ping,
                                    String cell_id,
                                    String device_id) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.upload_speed = upload_speed;
        this.download_speed = download_speed;
        this.ping = ping;
        this.cell_id = cell_id;
        this.device_id = device_id;
    }
}
// TODO(sudheesh001) security check