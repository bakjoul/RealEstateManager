package com.bakjoul.realestatemanager.ui.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

sealed class NativeText {

    abstract fun toCharSequence(context: Context): CharSequence

    data class Simple(val text: CharSequence) : NativeText() {
        override fun toCharSequence(context: Context): CharSequence = text
    }

    data class Resource(@StringRes val id: Int) : NativeText() {
        override fun toCharSequence(context: Context): CharSequence = context.getString(id)
    }

    data class Date(@StringRes val formatterPatternStringRes: Int, val date: TemporalAccessor) : NativeText() {
        override fun toCharSequence(context: Context): CharSequence =
            DateTimeFormatter
                .ofPattern(context.getString(formatterPatternStringRes))
                .format(date)
    }

    data class Plural(@PluralsRes val id: Int, val number: Int, val args: List<Any>) : NativeText() {
        override fun toCharSequence(context: Context): CharSequence =
            context.resources.getQuantityString(
                id,
                number,
                *args.map {
                    if (it is NativeText) {
                        it.toCharSequence(context)
                    } else {
                        it
                    }
                }.toTypedArray()
            )
    }

    data class Argument(@StringRes val id: Int, val arg: Any) : NativeText() {
        override fun toCharSequence(context: Context): CharSequence = context.getString(
            id,
            if (arg is NativeText) {
                arg.toCharSequence(context)
            } else {
                arg
            }
        )
    }

    data class Arguments(@StringRes val id: Int, val args: List<Any>) : NativeText() {
        override fun toCharSequence(context: Context): CharSequence = context.getString(
            id,
            *args.map {
                if (it is NativeText) {
                    it.toCharSequence(context)
                } else {
                    it
                }
            }.toTypedArray()
        )
    }

    data class Multi(val text: List<NativeText>, val separator: CharSequence? = null) : NativeText() {
        override fun toCharSequence(context: Context): CharSequence =
            text.joinToString(separator = separator ?: "") { it.toCharSequence(context) }
    }
}

fun NativeText.showAsToast(context: Context, duration: Int = Toast.LENGTH_SHORT): Toast {
    return Toast.makeText(context, toCharSequence(context), duration).apply { show() }
}
