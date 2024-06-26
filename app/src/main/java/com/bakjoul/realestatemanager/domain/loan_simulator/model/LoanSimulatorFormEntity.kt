package com.bakjoul.realestatemanager.domain.loan_simulator.model

import com.bakjoul.realestatemanager.data.loan_simulator.model.LoanDurationUnit
import java.math.BigDecimal

data class LoanSimulatorFormEntity(
    val amount: BigDecimal? = null,
    val downPayment: BigDecimal? = null,
    val interestRate: BigDecimal? = null,
    val insuranceRate: BigDecimal? = null,
    val duration: BigDecimal? = null,
    val loanDurationUnit: LoanDurationUnit = LoanDurationUnit.YEARS
)
