package com.lcl.lclmeasurementtool.Models;


import com.jsoniter.annotation.JsonProperty;

// TODO(sudheesh001) security check
public class SignalStrengthMessageModel {
    @JsonProperty
    double latitude;

    @JsonProperty
    double longitude;

    @JsonProperty
    String timestamp;

    @JsonProperty
    int dBm;

    @JsonProperty
    int level_code;

    @JsonProperty
    String cell_id;

    @JsonProperty
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