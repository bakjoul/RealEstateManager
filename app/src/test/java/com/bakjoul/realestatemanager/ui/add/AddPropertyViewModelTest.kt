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
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.photos.DeletePhotoUseCase
import com.bakjoul.realestatemanager.domain.photos.GetPhotosForPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.property.AddPropertyUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.DeletePropertyDraftUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.GetPropertyDraftByIdUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.UpdatePropertyDraftUseCase
import com.bakjoul.realestatemanager.domain.property.model.PropertyPoiEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyTypeEntity
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormAddress
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormEntity
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.Event
import com.bakjoul.realestatemanager.ui.utils.NativeText
import com.bakjoul.realestatemanager.utils.TestCoroutineRule
import com.bakjoul.realestatemanager.utils.ViewModelTestUtils.Companion.getCurrencyFormat
import com.bakjoul.realestatemanager.utils.observe
import com.bakjoul.realestatemanager.utils.observeForTesting
import com.google.common.truth.Truth
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
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
    private val clock: Clock = Clock.fixed(
        ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant(),
        ZoneOffset.UTC
    )
    private val updatePropertyDraftUseCase: UpdatePropertyDraftUseCase = mockk()
    private val addPropertyUseCase: AddPropertyUseCase = mockk()

    private lateinit var viewModel: AddPropertyViewModel

    @Before
    fun setUp() {
        every { savedStateHandle.get<Long>("draftId") } returns DEFAULT_DRAFT_ID
        every { savedStateHandle.get<Boolean>("isNewDraft") } returns true
        every { getCurrentCurrencyUseCase.invoke() } returns flowOf(AppCurrency.USD)
        every { getCurrentSurfaceUnitUseCase.invoke() } returns flowOf(SurfaceUnit.FEET)
        coEvery { getEuroRateUseCase.invoke() } returns CurrencyRateWrapper.Success(
            CurrencyRateEntity(
                currency = AppCurrency.EUR,
                rate = DEFAULT_EURO_RATE,
                updateDate = LocalDate.of(2023, 12, 28))
        )
        coEvery { getPropertyDraftByIdUseCase.invoke(DEFAULT_DRAFT_ID) } returns getDefaultPropertyFormEntity()
        //every { getCurrentNavigationUseCase.invoke() } returns flowOf()
        //coEvery { getAddressPredictionsUseCase.invoke(any()) } returns AutocompleteWrapper.NoResults
        //coEvery { getAddressDetailsUseCase.invoke(any()) } returns GeocodingWrapper.NoResults
        coEvery { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) } returns flowOf(emptyList())
        //coJustRun { deletePhotoUseCase.invoke(any()) }
        //coJustRun { navigateUseCase.invoke(any()) }
        //coJustRun { deletePropertyDraftUseCase.invoke(any()) }
        coEvery { updatePropertyDraftUseCase.invoke(DEFAULT_DRAFT_ID, any()) } returns 1
        //coEvery { addPropertyUseCase.invoke(any()) } returns 1L
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
            clock = clock,
            updatePropertyDraftUseCase = updatePropertyDraftUseCase,
            addPropertyUseCase = addPropertyUseCase
        )
    }

    @Test
    fun `nominal case - usd and feet`() = testCoroutineRule.runTest {
        // Given
        initViewModel()

        // When
        viewModel.viewStateLiveData.observeForTesting(this) {
            advanceUntilIdle()

            // Then
            assertThat(it.value).isEqualTo(getDefaultViewState())

            verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
            verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
            verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
            verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
            coVerify(exactly = 1) { getEuroRateUseCase.invoke()  }
            coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
            coVerify(exactly = 1) {
                updatePropertyDraftUseCase.invoke(
                    DEFAULT_DRAFT_ID,
                    getDefaultPropertyFormEntity()
                )
            }
            confirmVerified(
                getCurrentCurrencyUseCase,
                getCurrentSurfaceUnitUseCase,
                savedStateHandle,
                getEuroRateUseCase,
                getPhotosForPropertyIdUseCase,
                updatePropertyDraftUseCase
            )
        }
    }

    @Test
    fun `nominal case - euros and meters`() = testCoroutineRule.runTest {
        // Given
        every { getCurrentCurrencyUseCase.invoke() } returns flowOf(AppCurrency.EUR)
        every { getCurrentSurfaceUnitUseCase.invoke() } returns flowOf(SurfaceUnit.METERS)
        initViewModel()

        // When
        viewModel.viewStateLiveData.observeForTesting(this) {
            advanceUntilIdle()

            // Then
            assertThat(it.value).isEqualTo(
                getDefaultViewState(
                    isCurrencyEuro = true,
                    isSurfaceUnitMeters = true
                )
            )

            verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
            verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
            verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
            verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
            coVerify(exactly = 1) { getEuroRateUseCase.invoke()  }
            coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
            coVerify(exactly = 1) {
                updatePropertyDraftUseCase.invoke(
                    DEFAULT_DRAFT_ID,
                    getDefaultPropertyFormEntity()
                )
            }
            confirmVerified(
                savedStateHandle,
                getCurrentCurrencyUseCase,
                getCurrentSurfaceUnitUseCase,
                getEuroRateUseCase,
                getPhotosForPropertyIdUseCase,
                updatePropertyDraftUseCase
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

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        confirmVerified(savedStateHandle)
    }

    @Test
    fun `isNewDraft null`() = testCoroutineRule.runTest {
        // Given
        every { savedStateHandle.get<Boolean>("isNewDraft") } returns null

        // When
        assertThrows(IllegalArgumentException::class.java) {
            initViewModel()
        }.also { exception ->
            // Then
            assert(exception.message == "No information about new draft passed as parameter !")
        }

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Long>("isNewDraft") }
        confirmVerified(savedStateHandle)
    }

    @Test
    fun `view state sources null should emit view state null`() = testCoroutineRule.runTest {
        // Given
        every { getCurrentCurrencyUseCase.invoke() } returns emptyFlow()
        every { getCurrentSurfaceUnitUseCase.invoke() } returns emptyFlow()
        coEvery { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) } returns emptyFlow()

        initViewModel()

        // When
        viewModel.viewStateLiveData.observeForTesting(this) {

            // Then
            assertThat(it.value).isEqualTo(null)

            verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
            verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
            verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
            verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
            coVerify(exactly = 1) { getEuroRateUseCase.invoke()  }
            coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
            confirmVerified(
                savedStateHandle,
                getCurrentCurrencyUseCase,
                getCurrentSurfaceUnitUseCase,
                getEuroRateUseCase,
                getPhotosForPropertyIdUseCase
            )
        }
    }

    @Test
    fun `current address input shorter than 5 should emit empty address predictions list`() = testCoroutineRule.runTest {
        // Given
        initViewModel()
        viewModel.viewStateLiveData.observeForTesting(this) {}

        // When
        viewModel.onAddressChanged("test")

        // Then
        viewModel.viewStateLiveData.observeForTesting(this) {
            assertThat(it.value?.addressPredictions).isEqualTo(emptyList())

            verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
            verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
            verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
            verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
            coVerify(exactly = 1) { getEuroRateUseCase.invoke()  }
            coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
            confirmVerified(
                getCurrentCurrencyUseCase,
                getCurrentSurfaceUnitUseCase,
                savedStateHandle,
                getEuroRateUseCase,
                getPhotosForPropertyIdUseCase
            )
        }
    }

    @Test
    fun `current address input greater than or equal to 5 should emit address predictions list`() = testCoroutineRule.runTest {
        // Given
        coEvery { getAddressPredictionsUseCase.invoke("testing") } returns AutocompleteWrapper.Success(
            listOf(
                PredictionEntity(
                    address = "test address",
                    placeId = "test placeId",
                )
            )
        )
        initViewModel()
        viewModel.viewStateLiveData.observeForTesting(this) {}

        // When
        viewModel.onAddressChanged("testing")
        advanceTimeBy(SAVE_DELAY)

        // Then
        viewModel.viewStateLiveData.observeForTesting(this) {
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

        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        coVerify(exactly = 1) { getEuroRateUseCase.invoke()  }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        coVerify(exactly = 1) { getAddressPredictionsUseCase.invoke("testing") }
        confirmVerified(
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            savedStateHandle,
            getEuroRateUseCase,
            getPhotosForPropertyIdUseCase,
            getAddressPredictionsUseCase
        )
    }

    @Test
    fun `loading existing draft should emit view state with draft data`() = testCoroutineRule.runTest {
        // Given
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

        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        coVerify(exactly = 1) { getEuroRateUseCase.invoke()  }
        coVerify(exactly = 1) { getPropertyDraftByIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        confirmVerified(
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            savedStateHandle,
            getEuroRateUseCase,
            getPropertyDraftByIdUseCase,
            getPhotosForPropertyIdUseCase
        )
    }

    @Test
    fun `validating form with empty fields should emit view state with errors`() = testCoroutineRule.runTest {
        // Given
        initViewModel()
        viewModel.viewStateLiveData.observeForTesting(this) {}

        // When
        viewModel.onDoneButtonClicked()

        // Then
        viewModel.viewStateLiveData.observeForTesting(this) {
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

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        coVerify(exactly = 1) { getEuroRateUseCase.invoke()  }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        confirmVerified(
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            savedStateHandle,
            getEuroRateUseCase,
            getPhotosForPropertyIdUseCase
        )
    }

    @Test
    fun `saving draft with euros and feet should convert them into usd and meters`() = testCoroutineRule.runTest {
        // Given
        every { getCurrentCurrencyUseCase.invoke() } returns flowOf(AppCurrency.EUR)
        every { getCurrentSurfaceUnitUseCase.invoke() } returns flowOf(SurfaceUnit.FEET)
        initViewModel()

        // When
        viewModel.viewStateLiveData.observeForTesting(this) {
            viewModel.onPriceChanged(BigDecimal(10000))
            runCurrent()
            viewModel.onSurfaceChanged(BigDecimal(100))
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) {
                updatePropertyDraftUseCase.invoke(
                    DEFAULT_DRAFT_ID,
                    getExpectedPropertyForm(
                        referencePrice = BigDecimal(11109),
                        priceFromUser = BigDecimal(10000),
                        referenceSurface = BigDecimal(31),
                        surfaceFromUser = BigDecimal(100)
                    )
                )
            }

            verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
            verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
            verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
            verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
            coVerify(exactly = 1) { getEuroRateUseCase.invoke()  }
            coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
            confirmVerified(
                updatePropertyDraftUseCase,
                getCurrentCurrencyUseCase,
                getCurrentSurfaceUnitUseCase,
                savedStateHandle,
                getEuroRateUseCase,
                getPhotosForPropertyIdUseCase
            )
        }
    }

    @Test
    fun `saving draft with dollars and meters should not convert them`() = testCoroutineRule.runTest {
        // Given
        every { getCurrentCurrencyUseCase.invoke() } returns flowOf(AppCurrency.USD)
        every { getCurrentSurfaceUnitUseCase.invoke() } returns flowOf(SurfaceUnit.METERS)
        initViewModel()

        // When
        viewModel.viewStateLiveData.observeForTesting(this) {
            viewModel.onPriceChanged(BigDecimal(10000))
            runCurrent()
            viewModel.onSurfaceChanged(BigDecimal(100))
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) {
                updatePropertyDraftUseCase.invoke(
                    DEFAULT_DRAFT_ID,
                    getExpectedPropertyForm(
                        referencePrice = BigDecimal(10000),
                        priceFromUser = BigDecimal(10000),
                        referenceSurface = BigDecimal(100),
                        surfaceFromUser = BigDecimal(100)
                    )
                )
            }

            verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
            verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
            verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
            verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
            coVerify(exactly = 1) { getEuroRateUseCase.invoke()  }
            coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
            confirmVerified(
                updatePropertyDraftUseCase,
                savedStateHandle,
                getCurrentCurrencyUseCase,
                getCurrentSurfaceUnitUseCase,
                getEuroRateUseCase,
                getPhotosForPropertyIdUseCase
            )
        }
    }

    @Test
    fun `on camera button clicked and camera permission granted, view action live data should expose open camera action`() = testCoroutineRule.runTest {
        // Given
        initViewModel()
        coJustRun { navigateUseCase.invoke(To.Camera(DEFAULT_DRAFT_ID)) }
        every { getCurrentNavigationUseCase.invoke() } returns flowOf(To.Camera(DEFAULT_DRAFT_ID))
        viewModel.viewStateLiveData.observeForTesting(this) {}

        // When
        viewModel.onCameraPermissionGranted()

        // Then
        viewModel.viewActionLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(Event(AddPropertyViewAction.OpenCamera(DEFAULT_DRAFT_ID)))
        }

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        verify(exactly = 1) { getCurrentNavigationUseCase.invoke() }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        verify(exactly = 1) { navigateUseCase.invoke(To.Camera(DEFAULT_DRAFT_ID)) }
        confirmVerified(
            savedStateHandle,
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            getCurrentNavigationUseCase,
            getPhotosForPropertyIdUseCase,
            navigateUseCase
        )
    }

    @Test
    fun `on camera permission denied and on snack bar button clicked, view action live data should expose open settings action`() = testCoroutineRule.runTest {
        // Given
        initViewModel()
        coJustRun { navigateUseCase.invoke(To.Settings) }
        every { getCurrentNavigationUseCase.invoke() } returns flowOf(To.Settings)
        viewModel.viewStateLiveData.observeForTesting(this) {}

        // When
        viewModel.onChangeSettingsClicked()

        // Then
        viewModel.viewActionLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(Event(AddPropertyViewAction.OpenSettings))
        }

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        verify(exactly = 1) { getCurrentNavigationUseCase.invoke() }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        verify(exactly = 1) { navigateUseCase.invoke(To.Settings) }
        confirmVerified(
            savedStateHandle,
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            getCurrentNavigationUseCase,
            getPhotosForPropertyIdUseCase,
            navigateUseCase
        )
    }

    @Test
    fun `on close dialog clicked, if new draft and form is empty, draft should be deleted and view action live data should expose toast`() = testCoroutineRule.runTest {
        // Given
        initViewModel()
        coJustRun { deletePropertyDraftUseCase.invoke(any()) }
        coJustRun { navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.toast_draft_discarded))) }
        coJustRun { navigateUseCase.invoke(To.CloseAddProperty) }
        every { getCurrentNavigationUseCase.invoke() } returns flowOf(
            To.Toast(NativeText.Resource(R.string.toast_draft_discarded)),
            To.CloseAddProperty
        )
        viewModel.viewStateLiveData.observeForTesting(this) {}
        val actionLiveData = viewModel.viewActionLiveData.observe(this)

        // When
        viewModel.closeDialog()
        runCurrent()

        // Then
        Truth.assertThat(actionLiveData.values).containsExactly(
            Event(AddPropertyViewAction.ShowToast(NativeText.Resource(R.string.toast_draft_discarded))),
            Event(AddPropertyViewAction.CloseDialog)
        ).inOrder()

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        verify(exactly = 1) { getCurrentNavigationUseCase.invoke() }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        coVerify(exactly = 1) { deletePropertyDraftUseCase.invoke(DEFAULT_DRAFT_ID) }
        verify(exactly = 1) { navigateUseCase.invoke(To.Toast(NativeText.Resource(R.string.toast_draft_discarded))) }
        verify(exactly = 1) { navigateUseCase.invoke(To.CloseAddProperty) }
        confirmVerified(
            savedStateHandle,
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            getCurrentNavigationUseCase,
            getPhotosForPropertyIdUseCase,
            deletePropertyDraftUseCase,
            navigateUseCase
        )
    }

    @Test
    fun `on close dialog clicked, if existing draft, view action live data should expose save draft dialog action`() = testCoroutineRule.runTest {
        // Given
        every { savedStateHandle.get<Boolean>("isNewDraft") } returns false
        initViewModel()
        coJustRun { navigateUseCase.invoke(To.SaveDraftDialog) }
        every { getCurrentNavigationUseCase.invoke() } returns flowOf(To.SaveDraftDialog)
        viewModel.viewStateLiveData.observeForTesting(this) {}

        // When
        viewModel.closeDialog()

        // Then
        viewModel.viewActionLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(Event(AddPropertyViewAction.SaveDraftDialog))
        }

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        verify(exactly = 1) { getCurrentNavigationUseCase.invoke() }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        verify(exactly = 1) { navigateUseCase.invoke(To.SaveDraftDialog) }
        confirmVerified(
            savedStateHandle,
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            getCurrentNavigationUseCase,
            getPhotosForPropertyIdUseCase,
            navigateUseCase
        )
    }

    // region other closeDialog() tests
    @Test
    fun `on close dialog clicked, if new draft and type is not null, view action live data should expose save draft dialog action`() = testCoroutineRule.runTest {
        // Given
        initViewModel()
        coJustRun { navigateUseCase.invoke(To.SaveDraftDialog) }
        every { getCurrentNavigationUseCase.invoke() } returns flowOf(To.SaveDraftDialog)
        viewModel.viewStateLiveData.observeForTesting(this) {}

        // When
        viewModel.onPropertyTypeChanged(PropertyTypeEntity.FLAT.radioButtonId)
        viewModel.closeDialog()

        // Then
        viewModel.viewActionLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(Event(AddPropertyViewAction.SaveDraftDialog))
        }

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        verify(exactly = 1) { getCurrentNavigationUseCase.invoke() }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        verify(exactly = 1) { navigateUseCase.invoke(To.SaveDraftDialog) }
        confirmVerified(
            savedStateHandle,
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            getCurrentNavigationUseCase,
            getPhotosForPropertyIdUseCase,
            navigateUseCase
        )
    }

    @Test
    fun `on close dialog clicked, if new draft and forSaleSince is not null, view action live data should expose save draft dialog action`() = testCoroutineRule.runTest {
        // Given
        initViewModel()
        coJustRun { navigateUseCase.invoke(To.SaveDraftDialog) }
        every { getCurrentNavigationUseCase.invoke() } returns flowOf(To.SaveDraftDialog)
        viewModel.viewStateLiveData.observeForTesting(this) {}

        // When
        viewModel.onForSaleSinceDateChanged(1706140800000)  // 25/01/2024
        viewModel.closeDialog()

        // Then
        viewModel.viewActionLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(Event(AddPropertyViewAction.SaveDraftDialog))
        }

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        verify(exactly = 1) { getCurrentNavigationUseCase.invoke() }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        verify(exactly = 1) { navigateUseCase.invoke(To.SaveDraftDialog) }
        confirmVerified(
            savedStateHandle,
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            getCurrentNavigationUseCase,
            getPhotosForPropertyIdUseCase,
            navigateUseCase
        )
    }

    @Test
    fun `on close dialog clicked, if new draft and date of sale is not null, view action live data should expose save draft dialog action`() = testCoroutineRule.runTest {
        // Given
        initViewModel()
        coJustRun { navigateUseCase.invoke(To.SaveDraftDialog) }
        every { getCurrentNavigationUseCase.invoke() } returns flowOf(To.SaveDraftDialog)
        viewModel.viewStateLiveData.observeForTesting(this) {}

        // When
        viewModel.onSaleStatusChanged(true)
        runCurrent()
        viewModel.onSoldOnDateChanged(1708819200000)  // 25/02/2024

        viewModel.closeDialog()

        // Then
        viewModel.viewActionLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(Event(AddPropertyViewAction.SaveDraftDialog))
        }

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        verify(exactly = 1) { getCurrentNavigationUseCase.invoke() }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        verify(exactly = 1) { navigateUseCase.invoke(To.SaveDraftDialog) }
        confirmVerified(
            savedStateHandle,
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            getCurrentNavigationUseCase,
            getPhotosForPropertyIdUseCase,
            navigateUseCase
        )
    }

    @Test
    fun `on close dialog clicked, if new draft and price is not null, view action live data should expose save draft dialog action`() = testCoroutineRule.runTest {
        // Given
        initViewModel()
        coJustRun { navigateUseCase.invoke(To.SaveDraftDialog) }
        every { getCurrentNavigationUseCase.invoke() } returns flowOf(To.SaveDraftDialog)
        viewModel.viewStateLiveData.observeForTesting(this) {}

        // When
        viewModel.onPriceChanged(BigDecimal(10000))
        viewModel.closeDialog()

        // Then
        viewModel.viewActionLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(Event(AddPropertyViewAction.SaveDraftDialog))
        }

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        verify(exactly = 1) { getCurrentNavigationUseCase.invoke() }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        verify(exactly = 1) { navigateUseCase.invoke(To.SaveDraftDialog) }
        confirmVerified(
            savedStateHandle,
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            getCurrentNavigationUseCase,
            getPhotosForPropertyIdUseCase,
            navigateUseCase
        )
    }

    @Test
    fun `on close dialog clicked, if new draft and surface is not null, view action live data should expose save draft dialog action`() = testCoroutineRule.runTest {
        // Given
        initViewModel()
        coJustRun { navigateUseCase.invoke(To.SaveDraftDialog) }
        every { getCurrentNavigationUseCase.invoke() } returns flowOf(To.SaveDraftDialog)
        viewModel.viewStateLiveData.observeForTesting(this) {}

        // When
        viewModel.onSurfaceChanged(BigDecimal(100))
        viewModel.closeDialog()

        // Then
        viewModel.viewActionLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(Event(AddPropertyViewAction.SaveDraftDialog))
        }

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        verify(exactly = 1) { getCurrentNavigationUseCase.invoke() }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        verify(exactly = 1) { navigateUseCase.invoke(To.SaveDraftDialog) }
        confirmVerified(
            savedStateHandle,
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            getCurrentNavigationUseCase,
            getPhotosForPropertyIdUseCase,
            navigateUseCase
        )
    }

    @Test
    fun `on close dialog clicked, if new draft and number of rooms is not null, view action live data should expose save draft dialog action`() = testCoroutineRule.runTest {
        // Given
        initViewModel()
        coJustRun { navigateUseCase.invoke(To.SaveDraftDialog) }
        every { getCurrentNavigationUseCase.invoke() } returns flowOf(To.SaveDraftDialog)
        viewModel.viewStateLiveData.observeForTesting(this) {}

        // When
        viewModel.onRoomsCountChanged(BigDecimal(7))
        viewModel.closeDialog()

        // Then
        viewModel.viewActionLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(Event(AddPropertyViewAction.SaveDraftDialog))
        }

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        verify(exactly = 1) { getCurrentNavigationUseCase.invoke() }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        verify(exactly = 1) { navigateUseCase.invoke(To.SaveDraftDialog) }
        confirmVerified(
            savedStateHandle,
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            getCurrentNavigationUseCase,
            getPhotosForPropertyIdUseCase,
            navigateUseCase
        )
    }

    @Test
    fun `on close dialog clicked, if new draft and number of bathrooms is not null, view action live data should expose save draft dialog action`() = testCoroutineRule.runTest {
        // Given
        initViewModel()
        coJustRun { navigateUseCase.invoke(To.SaveDraftDialog) }
        every { getCurrentNavigationUseCase.invoke() } returns flowOf(To.SaveDraftDialog)
        viewModel.viewStateLiveData.observeForTesting(this) {}

        // When
        viewModel.onBathroomsCountChanged(BigDecimal(2))
        viewModel.closeDialog()

        // Then
        viewModel.viewActionLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(Event(AddPropertyViewAction.SaveDraftDialog))
        }

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        verify(exactly = 1) { getCurrentNavigationUseCase.invoke() }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        verify(exactly = 1) { navigateUseCase.invoke(To.SaveDraftDialog) }
        confirmVerified(
            savedStateHandle,
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            getCurrentNavigationUseCase,
            getPhotosForPropertyIdUseCase,
            navigateUseCase
        )
    }

    @Test
    fun `on close dialog clicked, if new draft and number of bedrooms is not null, view action live data should expose save draft dialog action`() = testCoroutineRule.runTest {
        // Given
        initViewModel()
        coJustRun { navigateUseCase.invoke(To.SaveDraftDialog) }
        every { getCurrentNavigationUseCase.invoke() } returns flowOf(To.SaveDraftDialog)
        viewModel.viewStateLiveData.observeForTesting(this) {}

        // When
        viewModel.onBedroomsCountChanged(BigDecimal(2))
        viewModel.closeDialog()

        // Then
        viewModel.viewActionLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(Event(AddPropertyViewAction.SaveDraftDialog))
        }

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        verify(exactly = 1) { getCurrentNavigationUseCase.invoke() }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        verify(exactly = 1) { navigateUseCase.invoke(To.SaveDraftDialog) }
        confirmVerified(
            savedStateHandle,
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            getCurrentNavigationUseCase,
            getPhotosForPropertyIdUseCase,
            navigateUseCase
        )
    }

    @Test
    fun `on close dialog clicked, if new draft and amenities list is not empty, view action live data should expose save draft dialog action`() = testCoroutineRule.runTest {
        // Given
        initViewModel()
        coJustRun { navigateUseCase.invoke(To.SaveDraftDialog) }
        every { getCurrentNavigationUseCase.invoke() } returns flowOf(To.SaveDraftDialog)
        viewModel.viewStateLiveData.observeForTesting(this) {}

        // When
        viewModel.onChipCheckedChanged(PropertyPoiEntity.PARK.poiResId, true)
        viewModel.closeDialog()

        // Then
        viewModel.viewActionLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(Event(AddPropertyViewAction.SaveDraftDialog))
        }

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        verify(exactly = 1) { getCurrentNavigationUseCase.invoke() }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        verify(exactly = 1) { navigateUseCase.invoke(To.SaveDraftDialog) }
        confirmVerified(
            savedStateHandle,
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            getCurrentNavigationUseCase,
            getPhotosForPropertyIdUseCase,
            navigateUseCase
        )
    }

    @Test
    fun `on close dialog clicked, if new draft and description is not null, view action live data should expose save draft dialog action`() = testCoroutineRule.runTest {
        // Given
        initViewModel()
        coJustRun { navigateUseCase.invoke(To.SaveDraftDialog) }
        every { getCurrentNavigationUseCase.invoke() } returns flowOf(To.SaveDraftDialog)
        viewModel.viewStateLiveData.observeForTesting(this) {}

        // When
        viewModel.onDescriptionChanged("test")
        viewModel.closeDialog()

        // Then
        viewModel.viewActionLiveData.observeForTesting(this) {
            assertThat(it.value).isEqualTo(Event(AddPropertyViewAction.SaveDraftDialog))
        }

        verify(exactly = 1) { savedStateHandle.get<Long>("draftId") }
        verify(exactly = 1) { savedStateHandle.get<Boolean>("isNewDraft") }
        verify(exactly = 1) { getCurrentCurrencyUseCase.invoke() }
        verify(exactly = 1) { getCurrentSurfaceUnitUseCase.invoke() }
        verify(exactly = 1) { getCurrentNavigationUseCase.invoke() }
        coVerify(exactly = 1) { getPhotosForPropertyIdUseCase.invoke(DEFAULT_DRAFT_ID) }
        verify(exactly = 1) { navigateUseCase.invoke(To.SaveDraftDialog) }
        confirmVerified(
            savedStateHandle,
            getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase,
            getCurrentNavigationUseCase,
            getPhotosForPropertyIdUseCase,
            navigateUseCase
        )
    }
    // endregion closeDialog() tests

    // region IN
    private fun getDefaultPropertyFormEntity() = PropertyFormEntity(
        id = DEFAULT_DRAFT_ID,
        type = null,
        isSold = false,
        forSaleSince = null,
        dateOfSale = null,
        referencePrice = null,
        priceFromUser = null,
        referenceSurface = null,
        surfaceFromUser = null,
        rooms = null,
        bathrooms = null,
        bedrooms = null,
        pointsOfInterest = emptyList(),
        autoCompleteAddress = PropertyFormAddress(),
        address = PropertyFormAddress(),
        description = null,
        photos = emptyList(),
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

    private fun getExpectedPropertyForm(
        propertyDraftId: Long = DEFAULT_DRAFT_ID,
        type: PropertyTypeEntity? = null,
        isSold: Boolean? = false,
        forSaleSince: LocalDate? = null,
        dateOfSale: LocalDate? = null,
        referencePrice: BigDecimal? = null,
        priceFromUser: BigDecimal? = null,
        referenceSurface: BigDecimal? = null,
        surfaceFromUser: BigDecimal? = null,
        rooms: BigDecimal? = null,
        bathrooms: BigDecimal? = null,
        bedrooms: BigDecimal? = null,
        pointsOfInterest: List<PropertyPoiEntity>? = emptyList(),
        autoCompleteAddress: PropertyFormAddress? = PropertyFormAddress(),
        address: PropertyFormAddress? = PropertyFormAddress(),
        description: String? = null,
        photos: List<PhotoEntity>? = emptyList(),
        agent: String? = null,
        lastUpdate: LocalDateTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0)
    ) = PropertyFormEntity(
        id = propertyDraftId,
        type = type,
        isSold = isSold,
        forSaleSince = forSaleSince,
        dateOfSale = dateOfSale,
        referencePrice = referencePrice,
        priceFromUser = priceFromUser,
        referenceSurface = referenceSurface,
        surfaceFromUser = surfaceFromUser,
        rooms = rooms,
        bathrooms = bathrooms,
        bedrooms = bedrooms,
        pointsOfInterest = pointsOfInterest,
        autoCompleteAddress = autoCompleteAddress,
        address = address,
        description = description,
        photos = photos,
        agent = agent,
        lastUpdate = lastUpdate
    )
    // endregion OUT
}