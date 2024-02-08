package com.bakjoul.realestatemanager.ui.loan_simulator

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.loan_simulator.model.DurationUnit
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.domain.loan_simulator.GetLoanSimulatorResultsUseCase
import com.bakjoul.realestatemanager.domain.loan_simulator.model.LoanSimulatorResultsEntity
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.getCurrencyFormat
import com.bakjoul.realestatemanager.utils.TestCoroutineRule
import com.bakjoul.realestatemanager.utils.observeForTesting
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal

class LoanSimulatorViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase = mockk()
    private val getLoanSimulatorResultUseCase: GetLoanSimulatorResultsUseCase = mockk()
    private val getCurrentNavigationUseCase: GetCurrentNavigationUseCase = mockk()
    private val navigateUseCase: NavigateUseCase = mockk()

    private lateinit var viewModel: LoanSimulatorViewModel

    @Before
    fun setUp() {
        every { getCurrentCurrencyUseCase.invoke() } returns flowOf(AppCurrency.USD)
        viewModel = LoanSimulatorViewModel(
            getCurrentCurrencyUseCase,
            getLoanSimulatorResultUseCase,
            getCurrentNavigationUseCase,
            navigateUseCase
        )
    }

    @Test
    fun `initial case - usd`() = testCoroutineRule.runTest {
        // When
        viewModel.viewStateLiveData.observeForTesting(this) {
            // Then
            assertThat(it.value).isEqualTo(getDefaultViewState())
        }
    }

    @Test
    fun `initial case - euro`() = testCoroutineRule.runTest {
        // Given
        every { getCurrentCurrencyUseCase.invoke() } returns flowOf(AppCurrency.EUR)

        // When
        viewModel.viewStateLiveData.observeForTesting(this) {
            // Then
            assertThat(it.value).isEqualTo(getDefaultViewState().copy(
                currencyIcon = R.drawable.euro_24,
                currencyFormat = getCurrencyFormat(AppCurrency.EUR)
            ))
        }
    }

    @Test
    fun `nominal case followed by reset`() = testCoroutineRule.runTest {
        // Given
        val amount = "100000"
        val downPayment = "20000"
        val interestRate = "3.5"
        val duration = "10"
        every { getLoanSimulatorResultUseCase.invoke(any(), any(), any(), any(), any()) } returns LoanSimulatorResultsEntity(
            monthlyPayment = BigDecimal(791.09),
            yearlyPayment = BigDecimal(9493.08),
            totalInterest = BigDecimal(14930.80),
            totalPayment = BigDecimal(94930.80)
        )

        // When
        viewModel.onAmountChanged(amount)
        viewModel.onDownPaymentChanged(downPayment)
        viewModel.onInterestRateChanged(interestRate) // For coverage
        viewModel.onInterestRateChanged("") // We suppose that the user corrected the input
        viewModel.onInterestRateChanged(interestRate)
        viewModel.onDurationChanged(duration)
        viewModel.onDurationChanged("") // For coverage
        viewModel.onDurationChanged(duration) // We suppose that the user corrected the input
        viewModel.onDurationUnitChanged(DurationUnit.YEARS)
        viewModel.onCalculateButtonClicked()

        // Then
        viewModel.viewStateLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(getDefaultViewState().copy(
                monthlyPayment = "$791.09",
                yearlyPayment = "$9,493.08",
                totalInterest = "$14,930.80",
                totalPayment = "$94,930.80"
            ))
        }

        // When
        viewModel.onResetButtonClicked()

        // Then
        viewModel.viewStateLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(getDefaultViewState())
        }
    }

    // region OUT
    private fun getDefaultViewState(): LoanSimulatorViewState = LoanSimulatorViewState(
        currencyIcon = R.drawable.dollar_24,
        currencyFormat = getCurrencyFormat(AppCurrency.USD),
        durationUnit = DurationUnit.YEARS,
        monthlyPayment = "",
        yearlyPayment = "",
        totalInterest = "",
        totalPayment = "",
        amountError = null,
        downPaymentError = null,
        interestRateError = null,
        durationError = null
    )
    // endregion OUT
}
