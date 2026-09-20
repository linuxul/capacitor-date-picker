package com.getcapacitor.community.datepicker

import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import java.text.ParseException

@CapacitorPlugin(name = "DatePicker")
public class DatePickerPlugin : Plugin() {
    private var options = DatePickerOptions()

    override fun load() {
        options = getDatePickerConfig()
    }

    @PluginMethod
    @Throws(ParseException::class)
    public fun present(call: PluginCall) {
        val datePicker = DatePicker(getDatePickerConfigCall(call), context)

        datePicker.open(
            object : DatePickerResolve {
                override fun resolve(date: String?) {
                    val response = JSObject()
                    response.put("value", date)
                    call.resolve(response)
                }

                override fun reject(message: String?) {
                    call.reject(message)
                }
            }
        )
    }

    private fun getDatePickerConfig(): DatePickerOptions = DatePickerOptions(
        theme = config.getString("android.theme", config.getString("theme", "light")),
        mode = config.getString("android.mode", config.getString("mode", "dateAndTime")),
        format = config.getString("android.format", config.getString("format", "yyyy-MM-dd'T'HH:mm:ss.sss")),
        timezone = config.getString("android.timezone", config.getString("timezone", "UTC")),
        locale = config.getString("android.locale", config.getString("locale", null)),
        cancelText = config.getString("android.cancelText", config.getString("cancelText", null)),
        doneText = config.getString("android.doneText", config.getString("doneText", null)),
        is24h = config.getBoolean("android.is24h", config.getBoolean("is24h", false))
    )

    @Throws(ParseException::class)
    private fun getDatePickerConfigCall(call: PluginCall): DatePickerOptions {
        val defaults = options
        val options = defaults.copy(
            theme = call.getString("android.theme", call.getString("theme", defaults.theme)),
            mode = call.getString("android.mode", call.getString("mode", defaults.mode)),
            format = call.getString("android.format", call.getString("format", defaults.format)),
            timezone = call.getString("android.timezone", call.getString("timezone", defaults.timezone)),
            locale = call.getString("android.locale", call.getString("locale", defaults.locale)),
            cancelText = call.getString("android.cancelText", call.getString("cancelText", defaults.cancelText)),
            doneText = call.getString("android.doneText", call.getString("doneText", defaults.doneText)),
            is24h = call.getBoolean("android.is24h", call.getBoolean("is24h", defaults.is24h)) ?: defaults.is24h,
            title = call.getString("android.title", call.getString("title", defaults.title))
        )

        call.getString("date")?.let { options.date = Parse.dateFromString(it, options.format, options.timezone) }
        call.getString("min")?.let { options.min = Parse.dateFromString(it, options.format, options.timezone) }
        call.getString("max")?.let { options.max = Parse.dateFromString(it, options.format, options.timezone) }

        return options
    }
}
