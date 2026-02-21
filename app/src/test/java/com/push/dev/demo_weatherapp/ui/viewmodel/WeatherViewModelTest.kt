package com.push.dev.demo_weatherapp.ui.viewmodel

import android.app.Application
import com.push.dev.demo_weatherapp.domain.model.ForecastData
import com.push.dev.demo_weatherapp.domain.model.WeatherData
import com.push.dev.demo_weatherapp.domain.model.WeatherError
import com.push.dev.demo_weatherapp.domain.usecase.GetForecastByCityNameUseCase
import com.push.dev.demo_weatherapp.domain.usecase.GetForecastByCoordinatesUseCase
import com.push.dev.demo_weatherapp.domain.usecase.GetWeatherByCityNameUseCase
import com.push.dev.demo_weatherapp.domain.usecase.GetWeatherByCoordinatesUseCase
import com.push.dev.demo_weatherapp.utils.DataStoreHelper
import com.push.dev.demo_weatherapp.utils.LocationHelper
import com.push.dev.demo_weatherapp.utils.Resource
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for WeatherViewModel
 */
class WeatherViewModelTest {
    
    private lateinit var application: Application
    private lateinit var getWeatherByCoordinatesUseCase: GetWeatherByCoordinatesUseCase
    private lateinit var getWeatherByCityNameUseCase: GetWeatherByCityNameUseCase
    private lateinit var getForecastByCoordinatesUseCase: GetForecastByCoordinatesUseCase
    private lateinit var getForecastByCityNameUseCase: GetForecastByCityNameUseCase
    private lateinit var locationHelper: LocationHelper
    private lateinit var dataStoreHelper: DataStoreHelper
    private lateinit var viewModel: WeatherViewModel
    
    @Before
    fun setup() {
        application = mockk(relaxed = true)
        getWeatherByCoordinatesUseCase = mockk()
        getWeatherByCityNameUseCase = mockk()
        getForecastByCoordinatesUseCase = mockk()
        getForecastByCityNameUseCase = mockk()
        locationHelper = mockk()
        dataStoreHelper = mockk()
        
        coEvery { dataStoreHelper.getLastCitySync() } returns null
        
        viewModel = WeatherViewModel(
            application,
            getWeatherByCoordinatesUseCase,
            getWeatherByCityNameUseCase,
            getForecastByCoordinatesUseCase,
            getForecastByCityNameUseCase,
            locationHelper,
            dataStoreHelper
        )
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `initial state is Idle when no last city is saved`() = runTest {
        // When
        val state = viewModel.uiState.first()
        
        // Then
        assertTrue(state is WeatherUiState.Idle)
    }
    
    @Test
    fun `searchWeatherByCity sets loading state then success`() = runTest {
        // Given
        val mockWeatherData = createMockWeatherData()
        val cityName = "New York"
        val mockForecast = ForecastData(
            daily = List(7) { mockWeatherData },
            hourlyToday = emptyList()
        )
        
        coEvery { locationHelper.getCoordinatesFromCityName(cityName) } returns 
            Result.success(Pair(40.7128, -74.0060))
        coEvery { getWeatherByCoordinatesUseCase(40.7128, -74.0060) } returns Resource.Success(mockWeatherData)
        coEvery { getForecastByCoordinatesUseCase(40.7128, -74.0060) } returns Resource.Success(mockForecast)
        coEvery { dataStoreHelper.saveLastCity(cityName) } just Runs
        
        // When
        viewModel.searchWeatherByCity(cityName)
        
        kotlinx.coroutines.delay(100)
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Success)
        assertEquals(mockWeatherData.cityName, (state as WeatherUiState.Success).weatherData.cityName)
    }
    
    @Test
    fun `searchWeatherByCity sets error state when city not found`() = runTest {
        // Given
        val cityName = "InvalidCity"
        coEvery { locationHelper.getCoordinatesFromCityName(cityName) } returns 
            Result.failure(Exception("City not found"))
        coEvery { getWeatherByCityNameUseCase(cityName) } returns 
            Resource.Error(WeatherError.CityNotFound("City not found"))
        
        // When
        viewModel.searchWeatherByCity(cityName)
        
        // Wait a bit for state updates
        kotlinx.coroutines.delay(100)
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Error)
    }
    
    @Test
    fun `searchWeatherByCity with empty string sets error state`() = runTest {
        // When
        viewModel.searchWeatherByCity("")
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Error)
    }
    
    @Test
    fun `clearError changes error state to idle`() = runTest {
        // Given - set error state
        val cityName = "InvalidCity"
        coEvery { locationHelper.getCoordinatesFromCityName(cityName) } returns 
            Result.failure(Exception("City not found"))
        coEvery { getWeatherByCityNameUseCase(cityName) } returns 
            Resource.Error(WeatherError.CityNotFound("City not found"))
        
        viewModel.searchWeatherByCity(cityName)
        kotlinx.coroutines.delay(100)
        
        // Verify error state
        assertTrue(viewModel.uiState.value is WeatherUiState.Error)
        
        // When
        viewModel.clearError()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Idle)
    }
    
    private fun createMockWeatherData(): WeatherData {
        return WeatherData(
            cityName = "New York",
            temperature = 72.5,
            description = "clear sky",
            humidity = 65,
            windSpeed = 5.5,
            iconCode = "01d",
            lastUpdated = System.currentTimeMillis(),
            latitude = 40.7128,
            longitude = -74.0060
        )
    }
}



