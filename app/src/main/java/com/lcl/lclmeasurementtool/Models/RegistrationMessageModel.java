package com.lcl.lclmeasurementtool.Models;

import com.jsoniter.annotation.JsonCreator;
import com.jsoniter.annotation.JsonProperty;

import org.apache.commons.codec.binary.Hex;

// TODO(sudheesh001) security check
public class RegistrationMessageModel {

    @JsonProperty
    String sigma_r;

    @JsonProperty
    String h;

    @JsonProperty
    String R;

    @JsonCreator
    public RegistrationMessageModel(byte[] sigma_r, byte[] h, byte[] R) {
        this.sigma_r = Hex.encodeHexString(sigma_r, false);
        this.h = Hex.encodeHexString(h, false);
        this.R = Hex.encodeHexString(R, false);
    }
}
// TODO(sudheesh001) security check