package com.getcapacitor.community.datepicker

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Configuration
import java.text.ParseException
import java.util.Calendar
import java.util.Locale

public class DatePicker(private val options: DatePickerOptions, private val context: Context) {
    private val calendar: Calendar = Calendar.getInstance()
    private val theme: Int = DatePickerTheme.get(options.theme, context)

    init {
        options.locale?.let { language ->
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = Configuration()
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }

    public fun launchTime(callback: DatePickerResolve) {
        // Set once the dialog has answered, so that the dismissal that follows does not answer again
        var answered = false
        val timePicker = TimePickerDialog(
            context,
            theme,
            { _, hourOfDay, minute ->
                answered = true
                calendar.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    hourOfDay,
                    minute
                )
                callback.resolve(Parse.dateToString(calendar.time, options.format))
            },
            calendar.get(Calendar.HOUR),
            calendar.get(Calendar.MINUTE),
            options.is24h
        )

        timePicker.create()

        val doneButton = timePicker.getButton(Dialog.BUTTON_POSITIVE)
        val cancelButton = timePicker.getButton(Dialog.BUTTON_NEGATIVE)

        options.date?.let { calendar.time = it }
        options.title?.let { timePicker.setTitle(it) }
        options.doneText?.let { doneButton.text = it }
        options.cancelText?.let { cancelButton.text = it }

        cancelButton.setOnClickListener {
            answered = true
            callback.resolve(null)
            timePicker.dismiss()
        }
        // Closed without an answer (the back button, a touch outside, or by code): answers as the cancel button does
        timePicker.setOnDismissListener {
            if (!answered) {
                answered = true
                callback.resolve(null)
            }
        }

        timePicker.updateTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

        timePicker.show()
    }

    public fun launchDate(callback: DatePickerResolve) {
        options.date?.let { calendar.time = it }

        // Set once the dialog has answered or handed over to the time dialog, so that its dismissal does not answer
        var answered = false
        val datePicker = DatePickerDialog(
            context,
            theme,
            { _, year, month, dayOfMonth ->
                answered = true
                calendar.set(year, month, dayOfMonth)
                if (options.mode == "dateAndTime") {
                    options.date = calendar.time
                    launchTime(callback)
                } else {
                    callback.resolve(Parse.dateToString(calendar.time, options.format))
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.create()
        val picker = datePicker.datePicker
        val doneButton = datePicker.getButton(Dialog.BUTTON_POSITIVE)
        val cancelButton = datePicker.getButton(Dialog.BUTTON_NEGATIVE)

        options.title?.let { datePicker.setTitle(it) }
        options.max?.let { picker.maxDate = it.time }
        options.min?.let { picker.minDate = it.time }
        options.doneText?.let { doneButton.text = it }
        options.cancelText?.let { cancelButton.text = it }

        cancelButton.setOnClickListener {
            answered = true
            callback.resolve(null)
            datePicker.cancel()
        }
        // Closed without an answer (the back button, a touch outside, or by code): answers as the cancel button does
        datePicker.setOnDismissListener {
            if (!answered) {
                answered = true
                callback.resolve(null)
            }
        }

        datePicker.show()
    }

    /**
     * Shows the picker. [callback] answers once: with the picked date, or with null when the user cancels or the
     * dialog closes without an answer. Call on the main thread.
     */
    @Throws(ParseException::class)
    public fun open(callback: DatePickerResolve) {
        if (options.mode == "time") {
            launchTime(callback)
        } else {
            launchDate(callback)
        }
    }
}
