package com.lcl.lclmeasurementtool.Functionality;

/**
 * A class representing the error during a ping test
 */
public class PingError {

    // the error message
    private String message;

    // the error code
    private int code;

    public PingError(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public PingError() {}

    /**
     * Set the error message
     * @param message the error message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Set the error code
     * @param code the error code
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Retrieve the error code
     * @return the error code associated with the error
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Retrieve the error message
     * @return the error message associated with the error
     */
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