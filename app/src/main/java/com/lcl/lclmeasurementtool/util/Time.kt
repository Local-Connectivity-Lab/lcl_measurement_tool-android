package com.lcl.lclmeasurementtool.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimeUtil {
    companion object {
        fun getCurrentTime(): String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).toString()
    }
}