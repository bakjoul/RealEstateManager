package com.bakjoul.realestatemanager.ui.utils

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class Event<T>(private val content: T) {

    private var hasBeenHandled = false

    @MainThread
    fun handleContent(block: (T) -> Unit) {
        if (!hasBeenHandled) {
            hasBeenHandled = true
            block(content)
        }
    }
}

fun <C, E> Channel<C>.asLiveDataEvent(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend EventScope<E>.(C) -> Unit
): LiveData<Event<E>> = liveData(coroutineContext) {
    receiveAsFlow().collect {
        block.invoke(
            object : EventScope<E> {
                override suspend fun emit(event: E) {
                    emit(Event(event))
                }
            },
            it
        )
    }
}

interface EventScope<E> {
    suspend fun emit(event: E)
}
