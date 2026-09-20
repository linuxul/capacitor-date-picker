package com.getcapacitor.community.datepicker

public interface DatePickerResolve {
    public fun resolve(date: String?)

    public fun reject(message: String?)
}
