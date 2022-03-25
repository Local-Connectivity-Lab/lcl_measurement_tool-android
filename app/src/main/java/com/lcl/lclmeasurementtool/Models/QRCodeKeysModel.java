package com.lcl.lclmeasurementtool.Models;

import com.jsoniter.annotation.JsonCreator;
import com.jsoniter.annotation.JsonProperty;

/**
 * A model representing the message from a QR code scan
 */
public class QRCodeKeysModel {

    @JsonProperty
    String sigma_t;

    @JsonProperty
    String sk_t;

    @JsonProperty
    String pk_a;

    @JsonCreator
    public QRCodeKeysModel(String sigma_t, String sk_t, String pk_a) {
        this.sigma_t = sigma_t;
        this.sk_t = sk_t;
        this.pk_a = pk_a;
    }

    public String getSigma_t() {
        return sigma_t;
    }

    public String getSk_t() {
        return sk_t;
    }

    public String getPk_a() {
        return pk_a;
    }
}
