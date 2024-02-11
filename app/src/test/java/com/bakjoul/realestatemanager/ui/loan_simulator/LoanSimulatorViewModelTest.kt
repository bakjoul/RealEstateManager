package com.bakjoul.realestatemanager.ui.loan_simulator

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.loan_simulator.model.DurationUnit
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.domain.loan_simulator.GetLoanSimulatorResultsUseCase
import com.bakjoul.realestatemanager.domain.loan_simulator.model.LoanSimulatorResultsEntity
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import com.bakjoul.realestatemanager.ui.utils.NativeText
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.getCurrencyFormat
import com.bakjoul.realestatemanager.utils.TestCoroutineRule
import com.bakjoul.realestatemanager.utils.observeForTesting
import io.mockk.coJustRun
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        confirmVerified(getCurrentCurrencyUseCase)
    }

    @Test
    fun `initial case - euro`() = testCoroutineRule.runTest {
        // Given
        every { getCurrentCurrencyUseCase.invoke() } returns flowOf(AppCurrency.EUR)

        // When
        viewModel.viewStateLiveData.observeForTesting(this) {
            // Then
            assertThat(it.value).isEqualTo(
                getDefaultViewState().copy(
                    currencyIcon = R.drawable.euro_24,
                    currencyFormat = getCurrencyFormat(AppCurrency.EUR)
                )
            )
        }

        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        confirmVerified(getCurrentCurrencyUseCase)
    }

    @Test
    fun `nominal case followed by reset`() = testCoroutineRule.runTest {
        // Given
        val amount = "100000"
        val downPayment = "20000"
        val interestRate = "3.5"
        val duration = "10"
        every {
            getLoanSimulatorResultUseCase.invoke(
                BigDecimal(100000),
                BigDecimal(20000),
                BigDecimal(3.5),
                BigDecimal(10),
                DurationUnit.YEARS
            )
        } returns LoanSimulatorResultsEntity(
            monthlyPayment = BigDecimal(791.09),
            yearlyPayment = BigDecimal(9493.08),
            totalInterest = BigDecimal(14930.80),
            totalPayment = BigDecimal(94930.80)
        )

        // When
        viewModel.onAmountChanged(amount)
        viewModel.onDownPaymentChanged(downPayment)
        viewModel.onInterestRateChanged(interestRate) // For coverage
        viewModel.onInterestRateChanged("") // We suppose that the user cleared the input
        viewModel.onInterestRateChanged(interestRate)
        viewModel.onDurationChanged(duration)
        viewModel.onDurationChanged("") // For coverage
        viewModel.onDurationChanged(duration) // We suppose that the user cleared the input
        viewModel.onDurationUnitChanged(DurationUnit.YEARS)
        viewModel.onCalculateButtonClicked()

        // Then
        viewModel.viewStateLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(
                getDefaultViewState().copy(
                    monthlyPayment = "$791.09",
                    yearlyPayment = "$9,493.08",
                    totalInterest = "$14,930.80",
                    totalPayment = "$94,930.80"
                )
            )
        }

        // When
        viewModel.onResetButtonClicked()

        // Then
        viewModel.viewStateLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(getDefaultViewState())
        }

        verify(exactly = 1) {
            getCurrentCurrencyUseCase.invoke()
            getLoanSimulatorResultUseCase.invoke(
                BigDecimal(100000),
                BigDecimal(20000),
                BigDecimal(3.5),
                BigDecimal(10),
                DurationUnit.YEARS
            )
        }
        confirmVerified(
            getCurrentCurrencyUseCase,
            getLoanSimulatorResultUseCase
        )
    }

    @Test
    fun `nominal case - euros and months`() = testCoroutineRule.runTest {
        // Given
        every { getCurrentCurrencyUseCase.invoke() } returns flowOf(AppCurrency.EUR)
        val amount = "100000"
        val interestRate = "4"
        val duration = "15"
        every {
            getLoanSimulatorResultUseCase.invoke(
                BigDecimal(100000),
                null,
                BigDecimal(4),
                BigDecimal(15),
                DurationUnit.MONTHS
            )
        } returns LoanSimulatorResultsEntity(
            monthlyPayment = BigDecimal(1841.65),
            yearlyPayment = BigDecimal(22099.80),
            totalInterest = BigDecimal(10499.0),
            totalPayment = BigDecimal(110499.0)
        )

        // When
        viewModel.onAmountChanged(amount)
        viewModel.onInterestRateChanged(interestRate)
        viewModel.onDurationUnitChanged(DurationUnit.MONTHS)
        viewModel.onDurationChanged(duration)
        viewModel.onCalculateButtonClicked()

        // Then
        viewModel.viewStateLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(
                getDefaultViewState().copy(
                    currencyIcon = R.drawable.euro_24,
                    currencyFormat = getCurrencyFormat(AppCurrency.EUR),
                    durationUnit = DurationUnit.MONTHS,
                    monthlyPayment = "1 841,65€",
                    yearlyPayment = "22 099,80€",
                    totalInterest = "10 499,00€",
                    totalPayment = "110 499,00€"
                )
            )
        }

        verify(exactly = 1) {
            getCurrentCurrencyUseCase.invoke()
            getLoanSimulatorResultUseCase.invoke(
                BigDecimal(100000),
                null,
                BigDecimal(4),
                BigDecimal(15),
                DurationUnit.MONTHS
            )
        }
        confirmVerified(
            getCurrentCurrencyUseCase,
            getLoanSimulatorResultUseCase
        )
    }

    @Test
    fun `empty fields should show errors on calculate button clicked`() =
        testCoroutineRule.runTest {
            // When
            viewModel.onCalculateButtonClicked()

            viewModel.viewStateLiveData.observeForTesting(this) {
                // Then
                assertThat(it.value).isEqualTo(
                    getDefaultViewState().copy(
                        amountError = NativeText.Resource(R.string.loan_simulator_error_amount),
                        interestRateError = NativeText.Resource(R.string.loan_simulator_error_interest_rate_required),
                        durationError = NativeText.Resource(R.string.loan_simulator_error_duration)
                    )
                )
            }

            verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
            confirmVerified(getCurrentCurrencyUseCase)
        }

    @Test
    fun `down payment greater than loan amount should show error on calculate button clicked`() =
        testCoroutineRule.runTest {
            // Given
            val amount = "100000"
            val downPayment = "200000"

            // When
            viewModel.onAmountChanged(amount)
            viewModel.onDownPaymentChanged(downPayment)
            viewModel.onCalculateButtonClicked()

            // Then
            viewModel.viewStateLiveData.observeForTesting(this) {
                assertThat(it.value).isEqualTo(
                    getDefaultViewState().copy(
                        downPaymentError = NativeText.Resource(R.string.loan_simulator_error_down_payment),
                        interestRateError = NativeText.Resource(R.string.loan_simulator_error_interest_rate_required),
                        durationError = NativeText.Resource(R.string.loan_simulator_error_duration)
                    )
                )
            }

            verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
            confirmVerified(getCurrentCurrencyUseCase)
        }

    @Test
    fun `down payment not null but amount null should show amount error on calculate button clicked`() =
        testCoroutineRule.runTest {
            // Given
            val downPayment = "200000"

            // When
            viewModel.onDownPaymentChanged(downPayment)
            viewModel.onCalculateButtonClicked()

            // Then
            viewModel.viewStateLiveData.observeForTesting(this) {
                assertThat(it.value).isEqualTo(
                    getDefaultViewState().copy(
                        amountError = NativeText.Resource(R.string.loan_simulator_error_amount),
                        interestRateError = NativeText.Resource(R.string.loan_simulator_error_interest_rate_required),
                        durationError = NativeText.Resource(R.string.loan_simulator_error_duration)
                    )
                )
            }

            verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
            confirmVerified(getCurrentCurrencyUseCase)
        }

    @Test
    fun `interest rate greater than 100 should show error on calculate button clicked`() =
        testCoroutineRule.runTest {
            // Given
            val interestRate = "101"

            // When
            viewModel.onInterestRateChanged(interestRate)
            viewModel.onCalculateButtonClicked()

            // Then
            viewModel.viewStateLiveData.observeForTesting(this) {
                assertThat(it.value).isEqualTo(
                    getDefaultViewState().copy(
                        amountError = NativeText.Resource(R.string.loan_simulator_error_amount),
                        interestRateError = NativeText.Resource(R.string.loan_simulator_error_interest_rate_invalid),
                        durationError = NativeText.Resource(R.string.loan_simulator_error_duration)
                    )
                )
            }

            verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
            confirmVerified(getCurrentCurrencyUseCase)
        }

    @Test
    fun `on close button clicked, view action should expose close dialog`() =
        testCoroutineRule.runTest {
            // Given
            coJustRun { navigateUseCase.invoke(To.CloseLoanSimulator) }
            every { getCurrentNavigationUseCase.invoke() } returns flowOf(To.CloseLoanSimulator)
            viewModel.viewStateLiveData.observeForTesting(this) {}

            // When
            viewModel.onCloseButtonClicked()

            // Then
            viewModel.viewActionLiveData.observeForTesting(this) {
                assertThat(it.value).isEqualTo(Event(LoanSimulatorViewAction.CloseDialog))
            }

            verify(exactly = 1) {
                getCurrentCurrencyUseCase.invoke()
                getCurrentNavigationUseCase.invoke()
                navigateUseCase.invoke(To.CloseLoanSimulator)
            }
            confirmVerified(
                getCurrentCurrencyUseCase,
                getCurrentNavigationUseCase,
                navigateUseCase
            )
        }

    @Test
    fun `edge case - on close button clicked, view action exposes another event`() =
        testCoroutineRule.runTest {
            // Given
            coJustRun { navigateUseCase.invoke(To.CloseLoanSimulator) }
            every { getCurrentNavigationUseCase.invoke() } returns flowOf(To.CloseAddProperty)
            viewModel.viewStateLiveData.observeForTesting(this) {}

            // When
            viewModel.onCloseButtonClicked()

            // Then
            viewModel.viewActionLiveData.observeForTesting(this) {
                assertThat(it.value).isNotEqualTo(Event(LoanSimulatorViewAction.CloseDialog))
            }

            verify(exactly = 1) {
                getCurrentCurrencyUseCase.invoke()
                getCurrentNavigationUseCase.invoke()
                navigateUseCase.invoke(To.CloseLoanSimulator)
            }
            confirmVerified(
                getCurrentCurrencyUseCase,
                getCurrentNavigationUseCase,
                navigateUseCase
            )
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
