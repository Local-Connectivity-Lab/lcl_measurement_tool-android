package com.lcl.lclmeasurementtool.Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lcl.lclmeasurementtool.Utils.Hex;

import java.io.Serializable;

public class MeasurementDataReportModel implements Serializable {

    @JsonProperty("sigma_m")
    String sigma_m;

    @JsonProperty("h_pkr")
    String h_pkr;

    @JsonProperty("M")
    String M;

    public MeasurementDataReportModel(byte[] sigma_m, byte[] h_pkr, byte[] M) {
        this.sigma_m = Hex.encodeHexString(sigma_m, false);
        this.h_pkr = Hex.encodeHexString(h_pkr, false);
        this.M = Hex.encodeHexString(M, false);
    }
}
