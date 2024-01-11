package com.bakjoul.realestatemanager.ui.add

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateWrapper
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.autocomplete.GetAddressPredictionsUseCase
import com.bakjoul.realestatemanager.domain.autocomplete.model.AutocompleteWrapper
import com.bakjoul.realestatemanager.domain.autocomplete.model.PredictionEntity
import com.bakjoul.realestatemanager.domain.currency_rate.GetEuroRateUseCase
import com.bakjoul.realestatemanager.domain.currency_rate.model.CurrencyRateEntity
import com.bakjoul.realestatemanager.domain.geocoding.GetAddressDetailsUseCase
import com.bakjoul.realestatemanager.domain.geocoding.model.GeocodingWrapper
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.photos.DeletePhotoUseCase
import com.bakjoul.realestatemanager.domain.photos.GetPhotosForPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.property.AddPropertyUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.DeletePropertyDraftUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.GetPropertyDraftByIdUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.UpdatePropertyDraftUseCase
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormEntity
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.NativeText
import com.bakjoul.realestatemanager.utils.TestCoroutineRule
import com.bakjoul.realestatemanager.utils.observeForTesting
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import org.junit.Assert.assertThrows

class AddPropertyViewModelTestNewDraft {

    companion object {
        private const val DEFAULT_DRAFT_ID = 1L
        private const val DEFAULT_EURO_RATE = 1.1109
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase = mockk()
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase = mockk()
    private val savedStateHandle: SavedStateHandle = mockk()
    private val getEuroRateUseCase: GetEuroRateUseCase = mockk()
    private val getPropertyDraftByIdUseCase: GetPropertyDraftByIdUseCase = mockk()
    private val getCurrentNavigationUseCase: GetCurrentNavigationUseCase = mockk()
    private val getAddressPredictionsUseCase: GetAddressPredictionsUseCase = mockk()
    private val getAddressDetailsUseCase: GetAddressDetailsUseCase = mockk()
    private val getPhotosForPropertyIdUseCase: GetPhotosForPropertyIdUseCase = mockk()
    private val deletePhotoUseCase: DeletePhotoUseCase = mockk()
    private val navigateUseCase: NavigateUseCase = mockk()
    private val deletePropertyDraftUseCase: DeletePropertyDraftUseCase = mockk()
    private val updatePropertyDraftUseCase: UpdatePropertyDraftUseCase = mockk()
    private val addPropertyUseCase: AddPropertyUseCase = mockk()

    private lateinit var viewModel: AddPropertyViewModel

    @Before
    fun setUp() {
        every { getCurrentCurrencyUseCase.invoke() } returns flowOf(AppCurrency.USD)
        every { getCurrentSurfaceUnitUseCase.invoke() } returns flowOf(SurfaceUnit.FEET)
        coEvery { getEuroRateUseCase.invoke() } returns CurrencyRateWrapper.Success(
            CurrencyRateEntity(
                currency = AppCurrency.EUR,
                rate = DEFAULT_EURO_RATE,
                updateDate = LocalDate.of(2023, 12, 28))
        )
        coEvery { getPropertyDraftByIdUseCase.invoke(DEFAULT_DRAFT_ID) } returns getDefaultPropertyFormEntity()
        every { getCurrentNavigationUseCase.invoke() } returns flowOf()
        coEvery { getAddressPredictionsUseCase.invoke(any()) } returns AutocompleteWrapper.NoResults
        coEvery { getAddressDetailsUseCase.invoke(any()) } returns GeocodingWrapper.NoResults
        coEvery { getPhotosForPropertyIdUseCase.invoke(any()) } returns flowOf(emptyList())
        coJustRun { deletePhotoUseCase.invoke(any()) }
        coJustRun { navigateUseCase.invoke(any()) }
        coJustRun { deletePropertyDraftUseCase.invoke(any()) }
        coEvery { updatePropertyDraftUseCase.invoke(any(), any()) } returns 1
        coEvery { addPropertyUseCase.invoke(any()) } returns 1L
    }

    private fun initViewModel() {
        viewModel = AddPropertyViewModel(
            getCurrentCurrencyUseCase = getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase = getCurrentSurfaceUnitUseCase,
            savedStateHandle = savedStateHandle,
            getEuroRateUseCase = getEuroRateUseCase,
            getPropertyDraftByIdUseCase = getPropertyDraftByIdUseCase,
            getCurrentNavigationUseCase = getCurrentNavigationUseCase,
            getAddressPredictionsUseCase = getAddressPredictionsUseCase,
            getAddressDetailsUseCase = getAddressDetailsUseCase,
            getPhotosForPropertyIdUseCase = getPhotosForPropertyIdUseCase,
            deletePhotoUseCase = deletePhotoUseCase,
            navigateUseCase = navigateUseCase,
            deletePropertyDraftUseCase = deletePropertyDraftUseCase,
            updatePropertyDraftUseCase = updatePropertyDraftUseCase,
            addPropertyUseCase = addPropertyUseCase
        )
    }

    @Test
    fun `nominal case`() = testCoroutineRule.runTest {
        // Given
        every { savedStateHandle.get<Long>("draftId") } returns DEFAULT_DRAFT_ID
        every { savedStateHandle.get<Boolean>("isNewDraft") } returns true
        initViewModel()

        // When
        viewModel.viewStateLiveData.observeForTesting(this) {

            // Then
            assertThat(it.value).isEqualTo(getExpectedAddPropertyViewState())
        }
    }

    @Test
    fun `draftId null`() = testCoroutineRule.runTest {
        // Given
        every { savedStateHandle.get<Long>("draftId") } returns null

        // When
        assertThrows(IllegalArgumentException::class.java) {
            initViewModel()
        }.also { exception ->
            // Then
            assert(exception.message == "No ID passed as parameter !")
        }
    }

    @Test
    fun `isNewDraft null`() = testCoroutineRule.runTest {
        // Given
        every { savedStateHandle.get<Long>("draftId") } returns DEFAULT_DRAFT_ID
        every { savedStateHandle.get<Boolean>("isNewDraft") } returns null

        // When
        assertThrows(IllegalArgumentException::class.java) {
            initViewModel()
        }.also { exception ->
            // Then
            assert(exception.message == "No information about new draft passed as parameter !")
        }
    }

    @Test
    fun `current address input shorter than 5 should emit empty address predictions list`() = testCoroutineRule.runTest {
        // Given
        every { savedStateHandle.get<Long>("draftId") } returns DEFAULT_DRAFT_ID
        every { savedStateHandle.get<Boolean>("isNewDraft") } returns true
        initViewModel()

        // When
        viewModel.onAddressChanged("test")

        // Then
        viewModel.viewStateLiveData.observeForTesting(this) {
            assertThat(it.value?.addressPredictions).isEqualTo(emptyList())
        }
    }

    @Test
    fun `current address input greater than or equal to 5 should emit address predictions list`() = testCoroutineRule.runTest {
        // Given
        every { savedStateHandle.get<Long>("draftId") } returns DEFAULT_DRAFT_ID
        every { savedStateHandle.get<Boolean>("isNewDraft") } returns true

        coEvery { getAddressPredictionsUseCase.invoke("testing") } returns AutocompleteWrapper.Success(
            listOf(
                PredictionEntity(
                    address = "test address",
                    placeId = "test placeId",
                )
            )
        )
        initViewModel()

        // When
        viewModel.onAddressChanged("testing")
        advanceTimeBy(400)

        // Then
        viewModel.viewStateLiveData.observeForTesting(this) {
            assertThat(it.value?.addressPredictions).isEqualTo(
                listOf(
                    AddPropertySuggestionItemViewState(
                        id = "test placeId",
                        address = "test address",
                        onSuggestionClicked = EquatableCallback {}
                    )
                )
            )
        }
    }

    // region IN
    private fun getDefaultPropertyFormEntity() = PropertyFormEntity(
        id = DEFAULT_DRAFT_ID,
        type = null,
        isSold = null,
        forSaleSince = null,
        dateOfSale = null,
        referencePrice = null,
        priceFromUser = null,
        referenceSurface = null,
        surfaceFromUser = null,
        rooms = null,
        bathrooms = null,
        bedrooms = null,
        pointsOfInterest = null,
        autoCompleteAddress = null,
        address = null,
        description = null,
        photos = null,
        agent = null,
        lastUpdate = LocalDateTime.of(2024, 1, 1, 0, 0, 0)
    )
    // endregion IN

    // region OUT
    private fun getExpectedAddPropertyViewState(): AddPropertyViewState = AddPropertyViewState(
        propertyTypeEntity = null,
        forSaleSince = null,
        dateOfSale = null,
        isSold = false,
        priceHint = NativeText.Argument(
            R.string.add_property_price_hint,
            NativeText.Resource(AppCurrency.USD.currencySymbol)
        ),
        price = null,
        currencyFormat = getDefaultCurrencyFormat(),
        surfaceLabel = NativeText.Argument(
            R.string.add_property_label_surface,
            NativeText.Resource(SurfaceUnit.FEET.unitSymbol)
        ),
        surface = BigDecimal.ZERO,
        numberOfRooms = BigDecimal.ZERO,
        numberOfBathrooms = BigDecimal.ZERO,
        numberOfBedrooms = BigDecimal.ZERO,
        amenities = emptyList(),
        addressPredictions = emptyList(),
        address = null,
        complementaryAddress = null,
        city = null,
        state = null,
        zipcode = null,
        description = null,
        photos = emptyList(),
        isTypeErrorVisible = false,
        forSaleSinceError = null,
        dateOfSaleError = null,
        priceError = null,
        isSurfaceErrorVisible = false,
        isRoomsErrorVisible = false,
        addressError = null,
        cityError = null,
        stateError = null,
        zipcodeError = null,
        descriptionError = null
    )

    private fun getDefaultCurrencyFormat(): DecimalFormat {
        // Currency is USD in this test
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }

        return DecimalFormat("#,###.##", symbols)
    }
    // endregion OUT
}