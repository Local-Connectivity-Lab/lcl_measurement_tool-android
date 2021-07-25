package com.lcl.lclmeasurementtool.Functionality;

public class IperfError {
    private String description;
    private int code;
    public IperfError (String description, int code) {
        this.description = description;
        this.code = code;
    }
    public void setDescription(String d) {
        description = d;
    }
    public void setCode (int errorCode) {
        code = errorCode;
    }

}
