package com.bakjoul.realestatemanager.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent

class TestObserver<T>(
    testScope: TestScope,
    liveData: LiveData<T>
) {
    private val observer = Observer<T> {}
    val values = mutableListOf<T>()

    init {
        try {
            liveData.observeForever {
                values.add(it)
            }
            testScope.runCurrent()
        } finally {
            liveData.removeObserver(observer)
        }
    }
}

fun <T> LiveData<T>.observe(testScope: TestScope): TestObserver<T> {
    return TestObserver(testScope, this)
}
