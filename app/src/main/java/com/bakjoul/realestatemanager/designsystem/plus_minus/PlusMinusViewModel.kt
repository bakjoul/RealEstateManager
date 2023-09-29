package com.bakjoul.realestatemanager.designsystem.plus_minus

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class PlusMinusViewModel @Inject constructor() : ViewModel() {

    private val isBigDecimal: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val value: MutableStateFlow<Number?> = MutableStateFlow(null)

    fun getValue(): LiveData<Number?> = value.asLiveData()

    fun setBigDecimal(isBigDecimal: Boolean) {
        this.isBigDecimal.value = isBigDecimal
        value.value = if (this.isBigDecimal.value) BigDecimal.ZERO else 0
    }

    fun setValue(newValue: String) {
        value.value = if (isBigDecimal.value) {
            newValue.toBigDecimalOrNull()
        } else {
            newValue.toIntOrNull()
        }
    }

    fun incrementValue() {
        value.value = if (isBigDecimal.value) {
            (value.value as? BigDecimal)?.add(BigDecimal.ONE)
        } else {
            (value.value as? Int)?.plus(1)
        }
    }

    fun decrementValue() {
        value.value = if (isBigDecimal.value) {
            (value.value as? BigDecimal)?.subtract(BigDecimal.ONE)
        } else {
            (value.value as? Int)?.minus(1)
        }
    }
}
