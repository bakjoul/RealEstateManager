package com.bakjoul.realestatemanager.domain.loan_simulator.model

import java.math.BigDecimal

data class LoanSimulatorResultsEntity(
    val monthlyPayment: BigDecimal? = null,
    val yearlyPayment: BigDecimal? = null,
    val totalInterest: BigDecimal? = null,
    val totalPayment: BigDecimal? = null
)
