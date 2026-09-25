package com.getcapacitor.community.datepicker

import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginException
import com.getcapacitor.PluginMethod
import com.getcapacitor.PluginThread
import com.getcapacitor.annotation.CapacitorPlugin
import java.text.ParseException
import kotlin.coroutines.suspendCoroutine

@CapacitorPlugin(name = "DatePicker")
public class DatePickerPlugin : Plugin() {
    private var options = DatePickerOptions()

    override fun load() {
        options = getDatePickerConfig()
    }

    // The dialogs are shown from the main thread, and the method returns once the user has answered: { value } with
    // the picked date, or without a value when they cancel or close the dialog.
    @PluginMethod(thread = PluginThread.MAIN)
    @Throws(ParseException::class)
    public suspend fun present(call: PluginCall): JSObject {
        val datePicker = DatePicker(getDatePickerConfigCall(call), context)

        val answer =
            suspendCoroutine { continuation ->
                val once = ResumeOnce(continuation)
                datePicker.open(
                    object : DatePickerResolve {
                        override fun resolve(date: String?) {
                            once.resume(Result.success(date))
                        }

                        override fun reject(message: String?) {
                            once.resume(Result.failure(PluginException(message.orEmpty())))
                        }
                    }
                )
            }
        return JSObject().put("value", answer.getOrThrow())
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
