package com.getcapacitor.community.datepicker

import java.util.Date

public data class DatePickerOptions(
    var format: String? = null,
    var locale: String? = null,
    var date: Date? = null,
    var mode: String? = null,
    var theme: String? = null,
    var timezone: String? = null,
    var min: Date? = null,
    var max: Date? = null,
    var doneText: String? = null,
    var cancelText: String? = null,
    var is24h: Boolean = false,
    var title: String? = null
)
