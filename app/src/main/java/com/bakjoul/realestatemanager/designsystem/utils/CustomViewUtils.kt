package com.bakjoul.realestatemanager.designsystem.utils

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager

object CustomViewUtils {

    fun hideKeyboard(view: View) {
        val context = view.context
        val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
