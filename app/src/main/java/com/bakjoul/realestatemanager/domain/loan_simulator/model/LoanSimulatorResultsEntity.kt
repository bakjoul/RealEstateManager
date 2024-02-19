package com.bakjoul.realestatemanager.domain.loan_simulator.model

import java.math.BigDecimal

data class LoanSimulatorResultsEntity(
    val monthlyPayment: BigDecimal? = null,
    val monthlyInterest: BigDecimal? = null,
    val monthlyInsurance: BigDecimal? = null,
    val yearlyPayment: BigDecimal? = null,
    val yearlyInterest: BigDecimal? = null,
    val yearlyInsurance: BigDecimal? = null,
    val totalPayment: BigDecimal? = null,
    val totalInterest: BigDecimal? = null,
    val totalInsurance: BigDecimal? = null
)
