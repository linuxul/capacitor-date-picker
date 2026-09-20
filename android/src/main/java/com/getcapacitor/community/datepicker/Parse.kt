package com.getcapacitor.community.datepicker

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

// The patterns come from the app, and the default locale is what the Java implementation formatted with.
@SuppressLint("SimpleDateFormat")
public object Parse {
    @Throws(ParseException::class)
    public fun dateFromString(date: String, format: String?, timezone: String?): Date? {
        val dateFormat = SimpleDateFormat(format)
        if (timezone != null) {
            dateFormat.timeZone = TimeZone.getTimeZone(timezone)
        }
        return dateFormat.parse(date)
    }

    public fun dateToString(date: Date, format: String?): String = SimpleDateFormat(format).format(date)
}
