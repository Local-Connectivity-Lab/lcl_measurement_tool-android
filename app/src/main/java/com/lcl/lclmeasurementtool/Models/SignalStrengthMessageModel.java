package com.lcl.lclmeasurementtool.Models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * A model representing the message of a signal strength measurement
 */
public class SignalStrengthMessageModel extends MeasurementDataModel {

    @JsonProperty("dbm")
    int dbm;

    @JsonProperty("level_code")
    int level_code;

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
        this.dbm = dBm;
        this.level_code = level_code;
        this.cell_id = cell_id;
        this.device_id = device_id;
    }
}