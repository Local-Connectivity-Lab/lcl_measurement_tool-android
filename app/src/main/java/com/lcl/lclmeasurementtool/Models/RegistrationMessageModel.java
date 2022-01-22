package com.lcl.lclmeasurementtool.Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import android.org.apache.commons.codec.binary.Hex;

import java.io.Serializable;

// TODO(sudheesh001) security check
public class RegistrationMessageModel implements Serializable {

    @JsonProperty
    String sigma_r;

    @JsonProperty
    String h;

    @JsonProperty
    String R;

    public RegistrationMessageModel(byte[] sigma_r, byte[] h, byte[] R) {
        this.sigma_r = Hex.encodeHexString(sigma_r, false);
        this.h = Hex.encodeHexString(h, false);
        this.R = Hex.encodeHexString(R, false);
    }

    public byte[] serializeToBytes() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(this);
    }
}
// TODO(sudheesh001) security check