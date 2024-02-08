package com.bakjoul.realestatemanager.domain.loan_simulator

import com.bakjoul.realestatemanager.data.loan_simulator.model.DurationUnit
import com.bakjoul.realestatemanager.domain.loan_simulator.model.LoanSimulatorResultsEntity
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class GetLoanSimulatorResultsUseCase @Inject constructor() {
    fun invoke(
        amount: BigDecimal,
        downPayment: BigDecimal?,
        interest: BigDecimal,
        duration: BigDecimal,
        durationUnit: DurationUnit
    ): LoanSimulatorResultsEntity {

        val amountAfterDownPayment = downPayment?.let { amount.subtract(it) } ?: amount
        val monthlyInterestRate = interest.divide(BigDecimal(12 * 100), 8, RoundingMode.HALF_EVEN)
        val durationInMonths = if (durationUnit == DurationUnit.YEARS) {
            duration.multiply(BigDecimal(12))
        } else {
            duration
        }

        if (interest.compareTo(BigDecimal.ZERO) == 0) {
            val monthlyPayment = amountAfterDownPayment.divide(durationInMonths, 2, RoundingMode.HALF_EVEN)
            val yearlyPayment = monthlyPayment.multiply(BigDecimal(12))

            return LoanSimulatorResultsEntity(
                monthlyPayment = monthlyPayment,
                yearlyPayment = yearlyPayment,
                totalInterest = BigDecimal.ZERO,
                totalPayment = amountAfterDownPayment
            )
        }

        val numerator = amountAfterDownPayment
            .multiply(monthlyInterestRate)
            .multiply((BigDecimal.ONE + monthlyInterestRate).pow(durationInMonths.toInt()))
        val denominator = (BigDecimal.ONE + monthlyInterestRate).pow(durationInMonths.toInt()) - BigDecimal.ONE

        val monthlyPayment = numerator.divide(denominator, 2, RoundingMode.HALF_EVEN)
        val yearlyPayment = monthlyPayment.multiply(BigDecimal(12))
        val totalInterest = monthlyPayment.multiply(durationInMonths).subtract(amountAfterDownPayment)
        val totalPayment = amountAfterDownPayment.add(totalInterest)

        return LoanSimulatorResultsEntity(
            monthlyPayment = monthlyPayment,
            yearlyPayment = yearlyPayment,
            totalInterest = totalInterest,
            totalPayment = totalPayment
        )
    }
}
