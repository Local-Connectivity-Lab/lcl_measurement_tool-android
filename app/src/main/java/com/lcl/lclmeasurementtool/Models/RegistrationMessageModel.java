//package com.lcl.lclmeasurementtool.Models;
//
//import com.lcl.lclmeasurementtool.Utils.Hex;
//import com.squareup.moshi.Json;
//
//import java.io.Serializable;
//
///**
// * A model representing the message of a registration data report
// */
//public class RegistrationMessageModel implements Serializable {
//
//    @Json(name = "sigma_r")
//    String sigma_r;
//
//    @Json(name = "h")
//    String h;
//
//    @Json(name = "R")
//    String R;
//
//    public RegistrationMessageModel(byte[] sigma_r, byte[] h, byte[] R) {
//        this.sigma_r = Hex.encodeHexString(sigma_r, false);
//        this.h = Hex.encodeHexString(h, false);
//        this.R = Hex.encodeHexString(R, false);
//    }
//}