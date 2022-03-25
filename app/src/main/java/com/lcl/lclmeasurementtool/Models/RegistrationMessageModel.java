package com.lcl.lclmeasurementtool.Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lcl.lclmeasurementtool.Utils.Hex;

import java.io.Serializable;

/**
 * A model representing the message of a registration data report
 */
public class RegistrationMessageModel implements Serializable {

    @JsonProperty("sigma_r")
    String sigma_r;

    @JsonProperty("h")
    String h;

    @JsonProperty("R")
    String R;

    public RegistrationMessageModel(byte[] sigma_r, byte[] h, byte[] R) {
        this.sigma_r = Hex.encodeHexString(sigma_r, false);
        this.h = Hex.encodeHexString(h, false);
        this.R = Hex.encodeHexString(R, false);
    }
}