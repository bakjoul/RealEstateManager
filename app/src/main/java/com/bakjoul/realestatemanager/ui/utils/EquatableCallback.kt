package com.bakjoul.realestatemanager.ui.utils

class EquatableCallback(private val callback: () -> Unit) {

    operator fun invoke() {
        callback.invoke()
    }

    override fun equals(other: Any?): Boolean = if (other is EquatableCallback) {
        true
    } else {
        super.equals(other)
    }

    override fun hashCode(): Int = 2051656923
}

class EquatableCallbackWithTwoParams<A, B>(private val callback: (A, B) -> Unit) {

    operator fun invoke(a: A, b: B) {
        callback.invoke(a, b)
    }

    override fun equals(other: Any?): Boolean = if (other is EquatableCallbackWithTwoParams<*, *>) {
        true
    } else {
        super.equals(other)
    }

    override fun hashCode(): Int = 1549643163
}
