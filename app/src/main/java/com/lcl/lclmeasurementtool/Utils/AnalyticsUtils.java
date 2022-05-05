package com.lcl.lclmeasurementtool.Utils;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsUtils {

    public static final String SK = "b501b642-ea97-4b79-b309-012091abdecf";
    public static final String QR_CODE_PARSING_FAILED = "QRCode Parsing Failed";
    public static final String INVALID_KEYS = "Invalid Keys";

    public static final String REGISTRATION_FAILED = "Registration Failed";
    public static final String LOCATION_NOT_FOUND = "Location Not Found";
    public static final String UPLOAD_FAILED = "Data Upload Failed";
    public static final String DATA_UPLOADED = "Data Uploaded";



    public static Map<String, String> formatProperties(String... reasons) {
        Map<String, String> properties = new HashMap<>();
        for (int i = 0; i < reasons.length - 1; i++) {
            properties.put(reasons[i], reasons[i+1]);
            i++;
        }

        return properties;
    }

}
