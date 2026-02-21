package com.push.dev.demo_weatherapp.data.repository

import com.push.dev.demo_weatherapp.data.api.OpenWeatherApiService
import com.push.dev.demo_weatherapp.data.model.*
import com.push.dev.demo_weatherapp.domain.model.WeatherError
import com.push.dev.demo_weatherapp.utils.Resource
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for WeatherRepository
 */
class WeatherRepositoryTest {
    
    private lateinit var apiService: OpenWeatherApiService
    private lateinit var repository: WeatherRepository
    private val apiKey = "test_api_key"
    
    @Before
    fun setup() {
        apiService = mock()
        repository = WeatherRepository(apiService, apiKey)
    }
    
    @Test
    fun `getWeatherByCoordinates returns success when API call succeeds`() = runTest {
        // Given
        val mockResponse = createMockWeatherResponse()
        whenever(apiService.getWeatherByCoordinates(0.0, 0.0, apiKey))
            .thenReturn(Single.just(mockResponse))
        
        // When
        val result = repository.getWeatherByCoordinates(0.0, 0.0)
        
        // Then
        assertTrue(result is Resource.Success)
        val successResult = result as Resource.Success
        assertEquals("New York", successResult.data.cityName)
        assertEquals(72.5, successResult.data.temperature, 0.1)
    }
    
    @Test
    fun `getWeatherByCoordinates returns error when API call fails`() = runTest {
        // Given
        whenever(apiService.getWeatherByCoordinates(0.0, 0.0, apiKey))
            .thenReturn(Single.error(Exception("Network error")))
        
        // When
        val result = repository.getWeatherByCoordinates(0.0, 0.0)
        
        // Then
        assertTrue(result is Resource.Error)
        val errorResult = result as Resource.Error
        assertTrue(errorResult.error is WeatherError.NetworkError)
    }
    
    @Test
    fun `getWeatherByCityName returns success when API call succeeds`() = runTest {
        // Given
        val mockResponse = createMockWeatherResponse()
        whenever(apiService.getWeatherByCityName("New York", apiKey))
            .thenReturn(Single.just(mockResponse))
        
        // When
        val result = repository.getWeatherByCityName("New York")
        
        // Then
        assertTrue(result is Resource.Success)
        val successResult = result as Resource.Success
        assertEquals("New York", successResult.data.cityName)
    }
    
    @Test
    fun `getWeatherByCityName returns error when city not found`() = runTest {
        // Given
        val errorResponse = WeatherResponse(
            cod = 404,
            coordinates = null,
            weather = null,
            base = null,
            main = null,
            visibility = null,
            wind = null,
            clouds = null,
            dateTime = null,
            sys = null,
            timezone = null,
            id = null,
            name = null
        )
        whenever(apiService.getWeatherByCityName("InvalidCity", apiKey))
            .thenReturn(Single.just(errorResponse))
        
        // When
        val result = repository.getWeatherByCityName("InvalidCity")
        
        // Then
        assertTrue(result is Resource.Error)
        val errorResult = result as Resource.Error
        assertTrue(errorResult.error is WeatherError.CityNotFound)
    }
    
    private fun createMockWeatherResponse(): WeatherResponse {
        return WeatherResponse(
            coordinates = Coordinates(latitude = 40.7128, longitude = -74.0060),
            weather = listOf(
                Weather(
                    id = 800,
                    main = "Clear",
                    description = "clear sky",
                    icon = "01d"
                )
            ),
            base = "stations",
            main = Main(
                temp = 72.5,
                feelsLike = 70.0,
                tempMin = 68.0,
                tempMax = 75.0,
                pressure = 1013,
                humidity = 65,
                seaLevel = null,
                groundLevel = null
            ),
            visibility = 10000,
            wind = Wind(speed = 5.5, deg = 180, gust = null),
            clouds = Clouds(all = 0),
            dateTime = System.currentTimeMillis() / 1000,
            sys = Sys(
                type = 1,
                id = 4610,
                country = "US",
                sunrise = System.currentTimeMillis() / 1000,
                sunset = System.currentTimeMillis() / 1000
            ),
            timezone = -14400,
            id = 5128581,
            name = "New York",
            cod = 200
        )
    }
}



