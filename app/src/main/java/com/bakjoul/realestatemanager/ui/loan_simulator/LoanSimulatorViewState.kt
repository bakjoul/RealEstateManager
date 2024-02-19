package com.bakjoul.realestatemanager.ui.loan_simulator

import androidx.annotation.DrawableRes
import com.bakjoul.realestatemanager.data.loan_simulator.model.DurationUnit
import com.bakjoul.realestatemanager.ui.utils.NativeText
import java.text.DecimalFormat

data class LoanSimulatorViewState (
    @DrawableRes val currencyIcon: Int,
    val currencyFormat: DecimalFormat,
    val durationUnit: DurationUnit,
    val monthlyPayment: String,
    val monthlyInterest: String,
    val monthlyInsurance: String,
    val yearlyPayment: String,
    val yearlyInterest: String,
    val yearlyInsurance: String,
    val totalInterest: String,
    val totalInsurance: String,
    val totalPayment: String,
    val amountError: NativeText?,
    val downPaymentError: NativeText?,
    val interestRateError: NativeText?,
    val insuranceRateError: NativeText?,
    val durationError: NativeText?,
)
