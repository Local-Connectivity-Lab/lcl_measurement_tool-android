package com.lcl.lclmeasurementtool.Utils;

import com.jsoniter.annotation.JsonProperty;

public class RegistrationMessageModel {

    @JsonProperty("publicKey")
    byte[] publicKey;

    @JsonProperty("identity")
    byte[] identity; // h(IMSI);

    @JsonProperty("attestation")
    byte[][] attestation;

    public RegistrationMessageModel(byte[] pk, byte[] identity, byte[][] attestation) {
        this.publicKey = pk;
        this.identity = identity;
        this.attestation = attestation;
    }
}
