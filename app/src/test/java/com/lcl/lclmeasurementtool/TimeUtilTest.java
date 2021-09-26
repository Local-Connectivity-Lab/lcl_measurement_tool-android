package com.lcl.lclmeasurementtool;

import com.lcl.lclmeasurementtool.Utils.TimeUtils;

import org.junit.Test;

import java.time.ZoneId;

public class TimeUtilTest {

    @Test
    public void testTimeConverter() {
        String time = TimeUtils.getTimeStamp(ZoneId.systemDefault());
        System.out.printf(time);
    }
}
