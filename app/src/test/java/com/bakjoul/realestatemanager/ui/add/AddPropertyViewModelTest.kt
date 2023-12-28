package com.bakjoul.realestatemanager.ui.add

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateWrapper
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.autocomplete.GetAddressPredictionsUseCase
import com.bakjoul.realestatemanager.domain.autocomplete.model.AutocompleteWrapper
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
import com.bakjoul.realestatemanager.ui.utils.NativeText
import com.bakjoul.realestatemanager.utils.TestCoroutineRule
import com.bakjoul.realestatemanager.utils.observeForTesting
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

class AddPropertyViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase = mockk()
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase = mockk()
    private val application: Application = mockk()
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
        every { getCurrentSurfaceUnitUseCase.invoke() } returns flowOf(SurfaceUnit.METERS)
        every { application.getString(any()) } returns ""
        every { savedStateHandle.get<Long>("draftId") } returns null
        every { savedStateHandle.get<Long>("newDraftId") } returns 1
        coEvery { getEuroRateUseCase.invoke() } returns CurrencyRateWrapper.Success(
            CurrencyRateEntity(
                currency = AppCurrency.EUR,
                rate = 1.1109,
                updateDate = LocalDate.of(2023, 12, 28))
        )
        coEvery { getPropertyDraftByIdUseCase.invoke(any()) } returns PropertyFormEntity(
            id = 1,
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
            lastUpdate = LocalDateTime.of(2021, 1, 1, 0, 0, 0)
        )
        every { getCurrentNavigationUseCase.invoke() } returns flowOf()
        coEvery { getAddressPredictionsUseCase.invoke(any()) } returns AutocompleteWrapper.NoResults
        coEvery { getAddressDetailsUseCase.invoke(any()) } returns GeocodingWrapper.NoResults
        coEvery { getPhotosForPropertyIdUseCase.invoke(any()) } returns flowOf(emptyList())
        coEvery { deletePhotoUseCase.invoke(any()) } returns Unit
        coEvery { navigateUseCase.invoke(any()) } returns Unit
        coEvery { deletePropertyDraftUseCase.invoke(any()) } returns Unit
        coEvery { updatePropertyDraftUseCase.invoke(any(), any()) } returns 1
        coEvery { addPropertyUseCase.invoke(any()) } returns 1L

        viewModel = AddPropertyViewModel(
            getCurrentCurrencyUseCase = getCurrentCurrencyUseCase,
            getCurrentSurfaceUnitUseCase = getCurrentSurfaceUnitUseCase,
            application = application,
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
    fun `initial case`() = testCoroutineRule.runTest {
        // When
        viewModel.viewStateLiveData.observeForTesting(this) {

            // Then
            assertThat(it.value).isEqualTo(getExpectedAddPropertyViewState())
        }
    }

    // region OUT
    private fun getExpectedAddPropertyViewState(): AddPropertyViewState = AddPropertyViewState(
        propertyTypeEntity = null,
        forSaleSince = null,
        dateOfSale = null,
        isSold = false,
        priceHint = NativeText.Simple(""),
        price = null,
        currencyFormat = getDefaultCurrencyFormat(),
        surfaceLabel = NativeText.Simple(""),
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
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }

        return DecimalFormat("#,###.##", symbols)
    }
    // endregion OUT
}