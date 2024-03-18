package com.jhb.crosswordScan.util

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

//import com.google.gson.internal.bind.util.ISO8601Utils
class TimeStampFormatter {

    private val timeStampPattern = "yyyy-MM-dd HH:mm:ss.SSSSSSZ"
    private val formatter = DateTimeFormatter.ofPattern(timeStampPattern)

    fun generateTimeStamp() : String {
        val dateTime = ZonedDateTime.now()
        val formattedDateTime = dateTime.format(formatter)
        return formattedDateTime
    }

    fun friendlyTimeStamp(timeStamp : String) : String {
        try {
            val zoneDateTime = getDateTime(timeStamp)
            val timeStampPattern = "MMM dd, HH:mm"
            val formatter = DateTimeFormatter.ofPattern(timeStampPattern)
            return zoneDateTime.format(formatter)
        } catch (e: java.time.format.DateTimeParseException) {
            return timeStamp
        }
    }


    fun getDateTime(timeStamp: String) : ZonedDateTime {
        return ZonedDateTime.parse(timeStamp, formatter)
    }

}

//fun friendlyDateTime(date : String)  :String {
//
//    ISO8601Utils.format()
//
//    return ""
//}