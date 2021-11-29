package com.lcl.lclmeasurementtool.Models;

import com.fasterxml.jackson.annotation.JsonProperty;

// TODO(sudheesh001) security check
public class SignalStrengthMessageModel {
    @JsonProperty("latitude")
    double latitude;

    @JsonProperty("longitude")
    double longitude;

    @JsonProperty("timestamp")
    String timestamp;

    @JsonProperty("dBm")
    int dBm;

    @JsonProperty("level_code")
    int level_code;

    @JsonProperty("cell_id")
    String cell_id;

    @JsonProperty("device_id")
    String device_id;

    public SignalStrengthMessageModel(double latitude,
                                      double longitude,
                                      String timestamp,
                                      int dBm,
                                      int level_code,
                                      String cell_id,
                                      String device_id) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.dBm = dBm;
        this.level_code = level_code;
        this.cell_id = cell_id;
        this.device_id = device_id;
    }
}
// TODO(sudheesh001) security check