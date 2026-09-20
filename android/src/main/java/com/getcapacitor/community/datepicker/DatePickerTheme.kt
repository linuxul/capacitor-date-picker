package com.getcapacitor.community.datepicker

import android.annotation.SuppressLint
import android.content.Context

public object DatePickerTheme {
    /** A style the app declares under the same name wins over the built-in themes. */
    @SuppressLint("DiscouragedApi")
    public fun get(theme: String?, context: Context): Int {
        val result = context.resources.getIdentifier(theme, "style", context.packageName)
        if (result != 0) return result

        return when (theme) {
            "dark" -> R.style.MateriaDarkTheme
            "light" -> R.style.MaterialLightTheme
            "legacyDark" -> R.style.SpinnerDarkTheme
            "legacyLight" -> R.style.SpinnerLightTheme
            else -> R.style.MaterialLightTheme
        }
    }
}
