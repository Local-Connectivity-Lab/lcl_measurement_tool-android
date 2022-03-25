package com.lcl.lclmeasurementtool.Utils;

import android.os.Build;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    /**
     * Return the current time stamp
     * according to the ISO_LOCAL_DATE_TIME format from the given location
     *
     * @param zoneId  the time zone ID to offset the time
     * @return a String representation of current time in ISO_LOCAL_DATE_TIME format
     */
    public static String getTimeStamp(ZoneId zoneId) {
        return Instant.now().atZone(zoneId).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
