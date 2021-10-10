package com.lcl.lclmeasurementtool.Utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    public static String getTimeStamp(ZoneId zoneId) {
        return Instant.now().atZone(zoneId).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
