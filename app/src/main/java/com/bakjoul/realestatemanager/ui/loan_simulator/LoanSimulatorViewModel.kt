package com.bakjoul.realestatemanager.ui.loan_simulator

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.loan_simulator.model.LoanDurationUnit
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
                    loanDurationUnit = form.loanDurationUnit,
                    monthlyPayment = formatPrice(results.monthlyPayment, currency),
                    monthlyInterest = formatPrice(results.monthlyInterest, currency),
                    monthlyInsurance = formatPrice(results.monthlyInsurance, currency),
                    yearlyPayment = formatPrice(results.yearlyPayment, currency),
                    yearlyInterest = formatPrice(results.yearlyInterest, currency),
                    yearlyInsurance = formatPrice(results.yearlyInsurance, currency),
                    totalInterest = formatPrice(results.totalInterest, currency),
                    totalInsurance = formatPrice(results.totalInsurance, currency),
                    totalPayment = formatPrice(results.totalPayment, currency),
                    amountError = errors.amountError,
                    downPaymentError = errors.downPaymentError,
                    interestRateError = errors.interestRateError,
                    insuranceRateError = errors.insuranceError,
                    durationError = errors.durationError
                )
            )
        }.collect()
    }

    val viewActionLiveData: LiveData<Event<LoanSimulatorViewAction>> = liveData {
        getCurrentNavigationUseCase.invoke().collect {
            when (it) {
                is To.CloseLoanSimulator -> emit(Event(LoanSimulatorViewAction.CloseDialog))
                is To.Toast -> emit(Event(LoanSimulatorViewAction.ShowToast(it.message)))
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
        val decimalFormat = DecimalFormat("#,##0.00", symbols)

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
                insuranceRate = null,
                duration = null
            )
        }
        resultsMutableStateFlow.update {
            it.copy(
                monthlyPayment = null,
                monthlyInterest = null,
                monthlyInsurance = null,
                yearlyPayment = null,
                yearlyInterest = null,
                yearlyInsurance = null,
                totalInterest = null,
                totalInsurance = null,
                totalPayment = null
            )
        }
    }

    fun onAmountChanged(amount: String) {
        if (amount.isEmpty()) {
            loanSimulatorFormMutableStateFlow.update {
                it.copy(amount = null)
            }
        } else {
            val bigDecimalAmount = BigDecimal(amount.replace(",", "").replace(" ", ""))
            loanSimulatorFormMutableStateFlow.update {
                it.copy(amount = bigDecimalAmount)
            }

            errorsMutableStateFlow.update {
                it.copy(amountError = null)
            }
        }
    }

    fun onDownPaymentChanged(downPayment: String) {
        if (downPayment.isEmpty()) {
            loanSimulatorFormMutableStateFlow.update {
                it.copy(downPayment = null)
            }
        } else {
            val bigDecimalDownPayment = BigDecimal(downPayment.replace(",", "").replace(" ", ""))
            loanSimulatorFormMutableStateFlow.update {
                it.copy(downPayment = bigDecimalDownPayment)
            }
        }
    }

    fun onInterestRateChanged(interestRate: String) {
        if (interestRate.isEmpty()) {
            loanSimulatorFormMutableStateFlow.update {
                it.copy(interestRate = null)
            }
        } else {
            loanSimulatorFormMutableStateFlow.update {
                it.copy(interestRate = BigDecimal(interestRate))
            }

            errorsMutableStateFlow.update {
                it.copy(interestRateError = null)
            }
        }
    }

    fun onInsuranceRateChanged(insuranceRate: String) {
        if (insuranceRate.isEmpty()) {
            loanSimulatorFormMutableStateFlow.update {
                it.copy(insuranceRate = null)
            }
        } else {
            loanSimulatorFormMutableStateFlow.update {
                it.copy(insuranceRate = BigDecimal(insuranceRate))
            }

            errorsMutableStateFlow.update {
                it.copy(insuranceError = null)
            }
        }
    }

    fun onDurationChanged(duration: String) {
        if (duration.isEmpty()) {
            loanSimulatorFormMutableStateFlow.update {
                it.copy(duration = null)
            }
        } else {
            loanSimulatorFormMutableStateFlow.update {
                it.copy(duration = BigDecimal(duration))
            }

            errorsMutableStateFlow.update {
                it.copy(durationError = null)
            }
        }
    }

    fun onDurationUnitChanged(loanDurationUnit: LoanDurationUnit) {
        loanSimulatorFormMutableStateFlow.update {
            it.copy(
                loanDurationUnit = when (loanDurationUnit) {
                    LoanDurationUnit.YEARS -> LoanDurationUnit.YEARS
                    LoanDurationUnit.MONTHS -> LoanDurationUnit.MONTHS
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

        if (loanSimulatorFormMutableStateFlow.value.downPayment != null
            && loanSimulatorFormMutableStateFlow.value.amount != null
            && (loanSimulatorFormMutableStateFlow.value.downPayment!! >= loanSimulatorFormMutableStateFlow.value.amount)) {
            errorsMutableStateFlow.update {
                it.copy(downPaymentError = NativeText.Resource(R.string.loan_simulator_error_down_payment))
            }
        } else {
            errorsMutableStateFlow.update {
                it.copy(downPaymentError = null)
            }
        }

        if (loanSimulatorFormMutableStateFlow.value.interestRate == null) {
            errorsMutableStateFlow.update {
                it.copy(interestRateError = NativeText.Resource(R.string.loan_simulator_error_interest_rate_required))
            }
            isFormValid = false
        } else if (loanSimulatorFormMutableStateFlow.value.interestRate!! >= BigDecimal(100)) {
            errorsMutableStateFlow.update {
                it.copy(interestRateError = NativeText.Resource(R.string.loan_simulator_error_interest_rate_invalid))
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(interestRateError = null)
            }
        }

        if (loanSimulatorFormMutableStateFlow.value.insuranceRate != null &&
            loanSimulatorFormMutableStateFlow.value.insuranceRate!! >= BigDecimal(100)
        ) {
            errorsMutableStateFlow.update {
                it.copy(insuranceError = NativeText.Resource(R.string.loan_simulator_error_insurance_rate_invalid))
            }
            isFormValid = false
        } else {
            errorsMutableStateFlow.update {
                it.copy(insuranceError = null)
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
                loanSimulatorFormMutableStateFlow.value.insuranceRate,
                loanSimulatorFormMutableStateFlow.value.duration!!,
                loanSimulatorFormMutableStateFlow.value.loanDurationUnit
            )

            resultsMutableStateFlow.update {
                it.copy(
                    monthlyPayment = results.monthlyPayment,
                    monthlyInterest = results.monthlyInterest,
                    monthlyInsurance = results.monthlyInsurance,
                    yearlyPayment = results.yearlyPayment,
                    yearlyInterest = results.yearlyInterest,
                    yearlyInsurance = results.yearlyInsurance,
                    totalInterest = results.totalInterest,
                    totalInsurance = results.totalInsurance,
                    totalPayment = results.totalPayment
                )
            }
        }
    }

    fun onCloseButtonClicked() {
        navigateUseCase.invoke(To.CloseLoanSimulator)
    }

    fun onResultClicked() {
        navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.loan_simulator_result_clipboard)))
    }

    data class LoanSimulatorErrors(
        val amountError: NativeText? = null,
        val downPaymentError: NativeText? = null,
        val interestRateError: NativeText? = null,
        val insuranceError: NativeText? = null,
        val durationError: NativeText? = null
    )
}
