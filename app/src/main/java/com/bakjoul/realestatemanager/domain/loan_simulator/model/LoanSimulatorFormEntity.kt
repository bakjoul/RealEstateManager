package com.bakjoul.realestatemanager.domain.loan_simulator.model

import com.bakjoul.realestatemanager.data.loan_simulator.model.DurationUnit
import java.math.BigDecimal

data class LoanSimulatorFormEntity(
    val amount: BigDecimal? = null,
    val downPayment: BigDecimal? = null,
    val interestRate: BigDecimal? = null,
    val duration: BigDecimal? = null,
    val durationUnit: DurationUnit = DurationUnit.YEARS
)
