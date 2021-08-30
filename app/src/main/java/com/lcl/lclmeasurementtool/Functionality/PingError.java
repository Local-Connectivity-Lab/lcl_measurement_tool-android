package com.lcl.lclmeasurementtool.Functionality;

public class PingError {
    private String message;
    private int code;

    public PingError(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public PingError() {}

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return "PingError{" +
                "message='" + message + '\'' +
                ", code=" + code +
                '}';
    }
}