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
        interestRate: BigDecimal,
        insuranceRate: BigDecimal?,
        duration: BigDecimal,
        durationUnit: DurationUnit
    ): LoanSimulatorResultsEntity {

        val amountAfterDownPayment = downPayment?.let { amount.subtract(it) } ?: amount
        val durationInMonths = if (durationUnit == DurationUnit.YEARS) {
            duration.multiply(BigDecimal(12))
        } else {
            duration
        }

        // Insurance calculation
        val monthlyInsurance = if (insuranceRate != null) {
            amountAfterDownPayment.multiply(insuranceRate.divide(BigDecimal(12 * 100), 8, RoundingMode.HALF_EVEN))
        } else {
            BigDecimal.ZERO
        }
        val yearlyInsurance = if (insuranceRate != null) {
            amountAfterDownPayment.multiply(insuranceRate.divide(BigDecimal(100), 8, RoundingMode.HALF_EVEN))
        } else {
            BigDecimal.ZERO
        }
        val totalInsurance = if (insuranceRate != null) {
            yearlyInsurance.multiply(durationInMonths.divide(BigDecimal(12), 2, RoundingMode.HALF_EVEN))
        } else {
            BigDecimal.ZERO
        }

        // No interest case
        if (interestRate.compareTo(BigDecimal.ZERO) == 0) {
            val monthlyPayment = amountAfterDownPayment.divide(durationInMonths, 2, RoundingMode.HALF_EVEN).add(monthlyInsurance)
            val yearlyPayment = monthlyPayment.multiply(BigDecimal(12)).add(yearlyInsurance)

            return LoanSimulatorResultsEntity(
                monthlyPayment = monthlyPayment,
                monthlyInterest = BigDecimal.ZERO,
                monthlyInsurance = monthlyInsurance,
                yearlyPayment = yearlyPayment,
                yearlyInterest = BigDecimal.ZERO,
                yearlyInsurance = yearlyInsurance,
                totalPayment = amountAfterDownPayment,
                totalInterest = BigDecimal.ZERO,
                totalInsurance = totalInsurance
            )
        }

        // Nominal case
        val monthlyInterestRate = interestRate.divide(BigDecimal(12 * 100), 8, RoundingMode.HALF_EVEN)
        val numerator = amountAfterDownPayment
            .multiply(monthlyInterestRate)
            .multiply((BigDecimal.ONE + monthlyInterestRate).pow(durationInMonths.toInt()))
        val denominator = (BigDecimal.ONE + monthlyInterestRate).pow(durationInMonths.toInt()) - BigDecimal.ONE

        val monthlyPayment = numerator.divide(denominator, 2, RoundingMode.HALF_EVEN).add(monthlyInsurance)
        val monthlyInterest = amountAfterDownPayment.multiply(monthlyInterestRate)
        val yearlyPayment = monthlyPayment.multiply(BigDecimal(12)).add(yearlyInsurance)
        val yearlyInterest = monthlyInterest.multiply(BigDecimal(12))
        val totalInterest = monthlyPayment.multiply(durationInMonths).subtract(amountAfterDownPayment)
        val totalPayment = amountAfterDownPayment.add(totalInterest).add(totalInsurance)

        return LoanSimulatorResultsEntity(
            monthlyPayment = monthlyPayment,
            monthlyInterest = monthlyInterest,
            monthlyInsurance = monthlyInsurance,
            yearlyPayment = yearlyPayment,
            yearlyInterest = yearlyInterest,
            yearlyInsurance = yearlyInsurance,
            totalInterest = totalInterest,
            totalInsurance = totalInsurance,
            totalPayment = totalPayment
        )
    }
}
