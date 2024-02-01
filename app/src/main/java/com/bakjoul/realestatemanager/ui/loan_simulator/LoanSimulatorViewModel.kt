package com.bakjoul.realestatemanager.ui.loan_simulator

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.loan_simulator.model.DurationUnit
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.domain.loan_simulator.GetLoanSimulatorResultsUseCase
import com.bakjoul.realestatemanager.domain.loan_simulator.model.LoanSimulatorFormEntity
import com.bakjoul.realestatemanager.domain.loan_simulator.model.LoanSimulatorResultsEntity
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import com.bakjoul.realestatemanager.ui.utils.NativeText
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.getCurrencyFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LoanSimulatorViewModel @Inject constructor(
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val getLoanSimulatorResultsUseCase: GetLoanSimulatorResultsUseCase,
    private val getCurrentNavigationUseCase: GetCurrentNavigationUseCase,
    private val navigateUseCase: NavigateUseCase
) : ViewModel() {

    private val loanSimulatorFormMutableStateFlow: MutableStateFlow<LoanSimulatorFormEntity> = MutableStateFlow(LoanSimulatorFormEntity())
    private val resultsMutableStateFlow: MutableStateFlow<LoanSimulatorResultsEntity> = MutableStateFlow(LoanSimulatorResultsEntity())
    private val errorsMutableStateFlow: MutableStateFlow<LoanSimulatorErrors> = MutableStateFlow(LoanSimulatorErrors())

    val viewStateLiveData: LiveData<LoanSimulatorViewState> = liveData {
        combine(
            getCurrentCurrencyUseCase.invoke(),
            loanSimulatorFormMutableStateFlow,
            errorsMutableStateFlow,
            resultsMutableStateFlow
        ) { currency, form, errors, results ->
            emit(
                LoanSimulatorViewState(
                    currencyIcon = getCurrencyIcon(currency),
                    currencyFormat = getCurrencyFormat(currency),
                    durationUnit = form.durationUnit,
                    monthlyPayment = formatPrice(results.monthlyPayment, currency),
                    yearlyPayment = formatPrice(results.yearlyPayment, currency),
                    totalInterest = formatPrice(results.totalInterest, currency),
                    totalPayment = formatPrice(results.totalPayment, currency),
                    amountError = errors.amountError,
                    interestRateError = errors.interestRateError,
                    durationError = errors.durationError
                )
            )
        }.collect()
    }

    val viewActionLiveData: LiveData<Event<LoanSimulatorViewAction>> = liveData {
        getCurrentNavigationUseCase.invoke().collect {
            when (it) {
                is To.CloseLoanSimulator -> emit(Event(LoanSimulatorViewAction.CloseDialog))
                else -> Unit
            }
        }
    }

    private fun getCurrencyIcon(currency: AppCurrency): Int {
        return when (currency) {
            AppCurrency.EUR -> R.drawable.euro_24
            AppCurrency.USD -> R.drawable.dollar_24
        }
    }

    private fun formatPrice(price: BigDecimal?, currency: AppCurrency): String {
        if (price == null) {
            return ""
        }

        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.groupingSeparator = if (currency == AppCurrency.EUR) ' ' else ','
        symbols.decimalSeparator = if (currency == AppCurrency.EUR) ',' else '.'
        val decimalFormat = DecimalFormat("#,###.##", symbols)

        val formattedPrice = when (currency) {
            AppCurrency.USD -> "$" + decimalFormat.format(price)
            AppCurrency.EUR -> decimalFormat.format(price) + "€"
        }
        return formattedPrice
    }

    fun onResetButtonClicked() {
        loanSimulatorFormMutableStateFlow.update {
            it.copy(
                amount = null,
                downPayment = null,
                interestRate = null,
                duration = null
            )
        }
        resultsMutableStateFlow.update {
            it.copy(
                monthlyPayment = null,
                yearlyPayment = null,
                totalInterest = null,
                totalPayment = null
            )
        }
    }

    fun onAmountChanged(amount: BigDecimal) {
        loanSimulatorFormMutableStateFlow.update {
            it.copy(amount = amount)
        }

        errorsMutableStateFlow.update {
            it.copy(amountError = null)
        }
    }

    fun onDownPaymentChanged(downPayment: BigDecimal) {
        loanSimulatorFormMutableStateFlow.update {
            it.copy(downPayment = downPayment)
        }
    }

    fun onInterestRateChanged(interestRate: Editable?) {
        if (!interestRate.isNullOrEmpty()) {
            loanSimulatorFormMutableStateFlow.update {
                it.copy(interestRate = BigDecimal(interestRate.toString()))
            }

            errorsMutableStateFlow.update {
                it.copy(interestRateError = null)
            }
        }
    }

    fun onDurationChanged(duration: Editable?) {
        if (!duration.isNullOrEmpty()) {
            loanSimulatorFormMutableStateFlow.update {
                it.copy(duration = BigDecimal(duration.toString()))
            }

            errorsMutableStateFlow.update {
                it.copy(durationError = null)
            }
        }
    }

    fun onDurationUnitChanged(durationUnit: DurationUnit) {
        loanSimulatorFormMutableStateFlow.update {
            it.copy(
                durationUnit = when (durationUnit) {
                    DurationUnit.YEARS -> DurationUnit.YEARS
                    DurationUnit.MONTHS -> DurationUnit.MONTHS
                }
            )
        }
    }

    private fun isFormValid(): Boolean {
        var isFormValid = true

        if (loanSimulatorFormMutableStateFlow.value.amount == null) {
            errorsMutableStateFlow.update {
                it.copy(amountError = NativeText.Resource(R.string.loan_simulator_error_amount))
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(amountError = null)
            }
        }

        if (loanSimulatorFormMutableStateFlow.value.interestRate == null) {
            errorsMutableStateFlow.update {
                it.copy(interestRateError = NativeText.Resource(R.string.loan_simulator_error_interest_rate))
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(interestRateError = null)
            }
        }

        if (loanSimulatorFormMutableStateFlow.value.duration == null) {
            errorsMutableStateFlow.update {
                it.copy(durationError = NativeText.Resource(R.string.loan_simulator_error_duration))
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(durationError = null)
            }
        }

        return isFormValid
    }

    fun onCalculateButtonClicked() {
        if (isFormValid()) {
            val results = getLoanSimulatorResultsUseCase.invoke(
                loanSimulatorFormMutableStateFlow.value.amount!!,
                loanSimulatorFormMutableStateFlow.value.downPayment,
                loanSimulatorFormMutableStateFlow.value.interestRate!!,
                loanSimulatorFormMutableStateFlow.value.duration!!,
                loanSimulatorFormMutableStateFlow.value.durationUnit
            )

            resultsMutableStateFlow.update {
                it.copy(
                    monthlyPayment = results.monthlyPayment,
                    yearlyPayment = results.yearlyPayment,
                    totalInterest = results.totalInterest,
                    totalPayment = results.totalPayment
                )
            }
        }
    }

    fun onCloseButtonClicked() {
        navigateUseCase.invoke(To.CloseLoanSimulator)
    }

    data class LoanSimulatorErrors(
        val amountError: NativeText? = null,
        val interestRateError: NativeText? = null,
        val durationError: NativeText? = null
    )
}
