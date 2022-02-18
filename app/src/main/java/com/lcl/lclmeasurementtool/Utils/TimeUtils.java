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

    public static String getTimeStamp(ZoneId zoneId) {
        return Instant.now().atZone(zoneId).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
