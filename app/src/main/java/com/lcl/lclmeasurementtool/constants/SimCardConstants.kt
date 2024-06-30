package com.lcl.lclmeasurementtool.constants

class SimCardConstants {
    companion object {
        /* The extra data for broacasting intent INTENT_ICC_STATE_CHANGE */
        const val INTENT_KEY_ICC_STATE = "ss"

        /* READY means ICC is ready to access */
        const val INTENT_VALUE_ICC_READY = "READY"

        /* IMSI means ICC IMSI is ready in property */
        const val INTENT_VALUE_ICC_IMSI = "IMSI"

        /* LOADED means all ICC records, including IMSI, are loaded */
        const val INTENT_VALUE_ICC_LOADED = "LOADED"
    }
}