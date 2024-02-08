package com.bakjoul.realestatemanager.ui.utils

import android.text.InputFilter
import android.text.Spanned

class PointBeforeNumberFilter : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        if (source.isNullOrEmpty()) {
            return null
        }

        if (source == "." && dstart == 0) {
            return "0."
        }

        return null
    }
}
