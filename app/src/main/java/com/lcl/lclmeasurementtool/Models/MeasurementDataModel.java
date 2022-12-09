package com.lcl.lclmeasurementtool.Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * An abstract class of a basic measurement data
 */
public abstract class MeasurementDataModel implements Serializable {
    @JsonProperty("latitude")
    double latitude;

    @JsonProperty("longitude")
    double longitude;

    @JsonProperty("timestamp")
    String timestamp;

    @JsonProperty("cell_id")
    String cell_id;

    @JsonProperty("device_id")
    String device_id;
}
