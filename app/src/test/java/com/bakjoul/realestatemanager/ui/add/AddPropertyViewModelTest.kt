package com.bakjoul.realestatemanager.ui.add

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateWrapper
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.PhotoListItemViewState
import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.SelectType
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
import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.property.AddPropertyUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.DeletePropertyDraftUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.GetPropertyDraftByIdUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.UpdatePropertyDraftUseCase
import com.bakjoul.realestatemanager.domain.property.model.PropertyPoiEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyTypeEntity
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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

class AddPropertyViewModelTest {

    companion object {
        private const val DEFAULT_DRAFT_ID = 1L
        private const val DEFAULT_EURO_RATE = 1.1109
        private val SAVE_DELAY = 3.seconds
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
    fun `nominal case - usd and feet`() = testCoroutineRule.runTest {
        // Given
        every { savedStateHandle.get<Long>("draftId") } returns DEFAULT_DRAFT_ID
        every { savedStateHandle.get<Boolean>("isNewDraft") } returns true
        initViewModel()

        // When
        viewModel.viewStateLiveData.observeForTesting(this) {

            // Then
            assertThat(it.value).isEqualTo(getDefaultViewState())
        }
    }

    @Test
    fun `nominal case - euros and meters`() = testCoroutineRule.runTest {
        // Given
        every { getCurrentCurrencyUseCase.invoke() } returns flowOf(AppCurrency.EUR)
        every { getCurrentSurfaceUnitUseCase.invoke() } returns flowOf(SurfaceUnit.METERS)
        every { savedStateHandle.get<Long>("draftId") } returns DEFAULT_DRAFT_ID
        every { savedStateHandle.get<Boolean>("isNewDraft") } returns true
        initViewModel()

        // When
        viewModel.viewStateLiveData.observeForTesting(this) {

            // Then
            assertThat(it.value).isEqualTo(
                getDefaultViewState(
                    isCurrencyEuro = true,
                    isSurfaceUnitMeters = true
                )
            )
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
        viewModel.viewStateLiveData.observeForTesting(this) {
            viewModel.onAddressChanged("test")

            // Then
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

        // Then
        viewModel.viewStateLiveData.observeForTesting(this) {
            // When
            viewModel.onAddressChanged("testing")
            advanceTimeBy(SAVE_DELAY)
            runCurrent()

            // Then
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

    @Test
    fun `property form update within save delay should cancel latest draft save job`() = testCoroutineRule.runTest {
        // Given
        every { savedStateHandle.get<Long>("draftId") } returns DEFAULT_DRAFT_ID
        every { savedStateHandle.get<Boolean>("isNewDraft") } returns true
        initViewModel()

        // When
        viewModel.viewStateLiveData.observeForTesting(this) {
            advanceUntilIdle()  // Lets the first save job completes

            // Form update starts a new save job
            viewModel.onPropertyTypeChanged(PropertyTypeEntity.FLAT.radioButtonId)
            // Advances times but not enough to cancel the latest save job
            advanceTimeBy(1000)
            // Another form update cancels latest save job and starts a new one
            viewModel.onPropertyTypeChanged(PropertyTypeEntity.HOUSE.radioButtonId)

            // TODO Comment vérifier ?
        }
    }

    @Test
    fun `loading existing draft should emit view state with draft data`() = testCoroutineRule.runTest {
        // Given
        every { savedStateHandle.get<Long>("draftId") } returns DEFAULT_DRAFT_ID
        every { savedStateHandle.get<Boolean>("isNewDraft") } returns false
        coEvery { getPropertyDraftByIdUseCase.invoke(DEFAULT_DRAFT_ID) } returns getExistingDraftPropertyFormEntity()
        coEvery { getPhotosForPropertyIdUseCase.invoke(any()) } returns flowOf(
            listOf(
                PhotoEntity(
                    id = 0L,
                    propertyId = 1L,
                    url = "test url",
                    description = "test description"
                )
            )
        )
        initViewModel()

        // When
        viewModel.viewStateLiveData.observeForTesting(this) {

            // Then
            assertThat(it.value).isEqualTo(getExistingDraftExpectedViewState())
        }
    }

    @Test
    fun `validating form with empty fields should emit view state with errors`() = testCoroutineRule.runTest {
        // Given
        every { savedStateHandle.get<Long>("draftId") } returns DEFAULT_DRAFT_ID
        every { savedStateHandle.get<Boolean>("isNewDraft") } returns true
        initViewModel()

        // When
        viewModel.viewStateLiveData.observeForTesting(this) {
            viewModel.onDoneButtonClicked()
            advanceUntilIdle()

            // Then
            assertThat(it.value?.isTypeErrorVisible).isEqualTo(true)
            assertThat(it.value?.forSaleSinceError).isEqualTo(NativeText.Resource(R.string.add_property_error_date_required))
            assertThat(it.value?.priceError).isEqualTo(NativeText.Resource(R.string.add_property_error_price_required))
            assertThat(it.value?.isSurfaceErrorVisible).isEqualTo(true)
            assertThat(it.value?.isRoomsErrorVisible).isEqualTo(true)
            assertThat(it.value?.addressError).isEqualTo(NativeText.Resource(R.string.add_property_error_address_required))
            assertThat(it.value?.cityError).isEqualTo(NativeText.Simple(" "))
            assertThat(it.value?.stateError).isEqualTo(NativeText.Simple(" "))
            assertThat(it.value?.zipcodeError).isEqualTo(NativeText.Simple(" "))
            assertThat(it.value?.descriptionError).isEqualTo(NativeText.Resource(R.string.add_property_error_description_required))
        }
    }

    @Test
    fun `saving draft with euros and feet should convert them into usd and meters`() = testCoroutineRule.runTest {
        // Given
        every { getCurrentCurrencyUseCase.invoke() } returns flowOf(AppCurrency.EUR)
        every { getCurrentSurfaceUnitUseCase.invoke() } returns flowOf(SurfaceUnit.METERS)
        every { savedStateHandle.get<Long>("draftId") } returns DEFAULT_DRAFT_ID
        every { savedStateHandle.get<Boolean>("isNewDraft") } returns true
        initViewModel()

        // When
        viewModel.viewStateLiveData.observeForTesting(this) {
            viewModel.onPriceChanged(BigDecimal(10000))
            advanceUntilIdle()

            // Then
            // TODO Comment vérifier ?
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

    private fun getExistingDraftPropertyFormEntity() = PropertyFormEntity(
        id = DEFAULT_DRAFT_ID,
        type = PropertyTypeEntity.FLAT,
        isSold = true,
        forSaleSince = LocalDate.of(2020, 1, 1),
        dateOfSale = LocalDate.of(2020, 2, 1),
        referencePrice = BigDecimal(10000),
        priceFromUser = null,
        referenceSurface = BigDecimal(100),
        surfaceFromUser = null,
        rooms = BigDecimal(7),
        bathrooms = BigDecimal(2),
        bedrooms = BigDecimal(2),
        pointsOfInterest = listOf(PropertyPoiEntity.PARK),
        autoCompleteAddress = null,
        address = null,
        description = "test description",
        photos = listOf(
            PhotoEntity(
                id = 0L,
                propertyId = 1L,
                url = "test url",
                description = "test description"
            )
        ),
        agent = null,
        lastUpdate = LocalDateTime.of(2024, 1, 1, 0, 0, 0)
    )
    // endregion IN

    // region OUT
    private fun getDefaultViewState(
        isCurrencyEuro: Boolean = false,
        isSurfaceUnitMeters: Boolean = false
    ): AddPropertyViewState = AddPropertyViewState(
        propertyTypeEntity = null,
        forSaleSince = null,
        dateOfSale = null,
        isSold = false,
        priceHint = NativeText.Argument(
            R.string.add_property_price_hint,
            NativeText.Resource(
                if (isCurrencyEuro) {
                    AppCurrency.EUR.currencySymbol
                } else {
                    AppCurrency.USD.currencySymbol
                }
            )
        ),
        price = null,
        currencyFormat = getCurrencyFormat(isCurrencyEuro),
        surfaceLabel = NativeText.Argument(
            R.string.add_property_label_surface,
            NativeText.Resource(
                if (isSurfaceUnitMeters) {
                    SurfaceUnit.METERS.unitSymbol
                } else {
                    SurfaceUnit.FEET.unitSymbol
                }
            )
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

    private fun getCurrencyFormat(isCurrencyEuro: Boolean = false): DecimalFormat {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = if (isCurrencyEuro) {
                ' '
            } else {
                ','
            }
            decimalSeparator = if (isCurrencyEuro) {
                ','
            } else {
                '.'
            }
        }

        return DecimalFormat("#,###.##", symbols)
    }

    private fun getExistingDraftExpectedViewState(): AddPropertyViewState = AddPropertyViewState(
        propertyTypeEntity = PropertyTypeEntity.FLAT,
        forSaleSince = NativeText.Date(R.string.date_format, LocalDate.of(2020, 1, 1)),
        dateOfSale = NativeText.Date(R.string.date_format, LocalDate.of(2020, 2, 1)),
        isSold = true,
        priceHint = NativeText.Argument(
            R.string.add_property_price_hint,
            NativeText.Resource(AppCurrency.USD.currencySymbol)
        ),
        price = "10000",
        currencyFormat = getCurrencyFormat(),
        surfaceLabel = NativeText.Argument(
            R.string.add_property_label_surface,
            NativeText.Resource(SurfaceUnit.FEET.unitSymbol)
        ),
        surface = BigDecimal(329),
        numberOfRooms = BigDecimal(7),
        numberOfBathrooms = BigDecimal(2),
        numberOfBedrooms = BigDecimal(2),
        amenities = listOf(PropertyPoiEntity.PARK),
        addressPredictions = emptyList(),
        address = null,
        complementaryAddress = null,
        city = null,
        state = null,
        zipcode = null,
        description = "test description",
        photos = listOf(
            PhotoListItemViewState(
                id = 0,
                url = "test url",
                description = "test description",
                selectType = SelectType.NOT_SELECTABLE,
                onPhotoClicked = EquatableCallback {},
                onDeletePhotoClicked = EquatableCallback {}
            )
        ),
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

    private fun getDefaultViewStateWithErrors() = AddPropertyViewState(
        propertyTypeEntity = null,
        forSaleSince = null,
        dateOfSale = null,
        isSold = false,
        priceHint = NativeText.Argument(
            R.string.add_property_price_hint,
            NativeText.Resource(AppCurrency.USD.currencySymbol)
        ),
        price = null,
        currencyFormat = getCurrencyFormat(),
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
        isTypeErrorVisible = true,
        forSaleSinceError = NativeText.Resource(R.string.add_property_error_date_required),
        dateOfSaleError = null,
        priceError = NativeText.Resource(R.string.add_property_error_price_required),
        isSurfaceErrorVisible = true,
        isRoomsErrorVisible = true,
        addressError = NativeText.Resource(R.string.add_property_error_address_required),
        cityError = NativeText.Simple(" "),
        stateError = NativeText.Simple(" "),
        zipcodeError = NativeText.Simple(" "),
        descriptionError = NativeText.Resource(R.string.add_property_error_description_required)
    )
    // endregion OUT
}