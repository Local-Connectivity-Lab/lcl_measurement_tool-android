package com.lcl.lclmeasurementtool.Constants;

/**
 * Constants that keep track of SIM card state
 */
public class SimCardConstants {
    /* The extra data for broacasting intent INTENT_ICC_STATE_CHANGE */
    public static final String INTENT_KEY_ICC_STATE = "ss";
    /* READY means ICC is ready to access */
    public static final String INTENT_VALUE_ICC_READY = "READY";
    /* IMSI means ICC IMSI is ready in property */
    public static final String INTENT_VALUE_ICC_IMSI = "IMSI";
    /* LOADED means all ICC records, including IMSI, are loaded */
    public static final String INTENT_VALUE_ICC_LOADED = "LOADED";

}
