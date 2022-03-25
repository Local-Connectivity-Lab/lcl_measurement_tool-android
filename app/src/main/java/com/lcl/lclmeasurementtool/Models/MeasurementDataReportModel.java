package com.lcl.lclmeasurementtool.Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lcl.lclmeasurementtool.Utils.Hex;

import java.io.Serializable;

/**
 * A model representing the message of a measurement data report
 */
public class MeasurementDataReportModel implements Serializable {

    @JsonProperty("sigma_m")
    String sigma_m;

    @JsonProperty("h_pkr")
    String h_pkr;

    @JsonProperty("M")
    String M;

    @JsonProperty("show_data")
    boolean show_data;

    public MeasurementDataReportModel(byte[] sigma_m, byte[] h_pkr, byte[] M, boolean show_data) {
        this.sigma_m = Hex.encodeHexString(sigma_m, false);
        this.h_pkr = Hex.encodeHexString(h_pkr, false);
        this.M = Hex.encodeHexString(M, false);
        this.show_data = show_data;
    }
}
