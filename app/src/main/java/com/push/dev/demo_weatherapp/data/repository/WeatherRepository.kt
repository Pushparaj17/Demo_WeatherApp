package com.push.dev.demo_weatherapp.data.repository

import com.push.dev.demo_weatherapp.data.api.OpenWeatherApiService
import com.push.dev.demo_weatherapp.data.model.WeatherResponse
import com.push.dev.demo_weatherapp.domain.model.WeatherData
import com.push.dev.demo_weatherapp.domain.model.WeatherError
import com.push.dev.demo_weatherapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.rx3.await
import javax.inject.Inject

/**
 * Repository implementation for weather data
 * Handles API calls and data transformation
 */
class WeatherRepository @Inject constructor(
    private val apiService: OpenWeatherApiService,
    private val apiKey: String
) {
    
    /**
     * Fetch weather by coordinates
     */
    suspend fun getWeatherByCoordinates(
        latitude: Double,
        longitude: Double
    ): Resource<WeatherData> {
        return try {
            val response = apiService.getWeatherByCoordinates(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey
            ).await()
            
            if (response.cod == 200 && response.weather != null && response.main != null) {
                Resource.Success(response.toDomainModel())
            } else {
                Resource.Error(WeatherError.UnknownError("Invalid response from API"))
            }
        } catch (e: Exception) {
            Resource.Error(handleException(e))
        }
    }
    
    /**
     * Fetch weather by city name (fallback)
     */
    suspend fun getWeatherByCityName(cityName: String): Resource<WeatherData> {
        return try {
            val response = apiService.getWeatherByCityName(
                cityName = cityName,
                apiKey = apiKey
            ).await()
            
            if (response.cod == 200 && response.weather != null && response.main != null) {
                Resource.Success(response.toDomainModel())
            } else {
                Resource.Error(WeatherError.CityNotFound("City not found: $cityName"))
            }
        } catch (e: Exception) {
            Resource.Error(handleException(e))
        }
    }
    
    /**
     * Handle exceptions and convert to appropriate error types
     */
    private fun handleException(e: Exception): WeatherError {
        return when {
            e.message?.contains("404") == true -> WeatherError.CityNotFound("City not found")
            e.message?.contains("401") == true -> WeatherError.ApiKeyError("Invalid API key")
            e.message?.contains("timeout", ignoreCase = true) == true -> 
                WeatherError.NetworkError("Request timeout")
            e.message?.contains("Unable to resolve host", ignoreCase = true) == true -> 
                WeatherError.NetworkError("No internet connection")
            else -> WeatherError.UnknownError(e.message ?: "Unknown error occurred")
        }
    }
    
    /**
     * Extension function to convert API response to domain model
     */
    private fun WeatherResponse.toDomainModel(): WeatherData {
        val weather = weather?.firstOrNull()
        val main = main ?: throw IllegalStateException("Main data is null")
        val coordinates = coordinates ?: throw IllegalStateException("Coordinates are null")
        
        return WeatherData(
            cityName = name ?: "Unknown",
            temperature = main.temp ?: 0.0,
            description = weather?.description ?: "Unknown",
            humidity = main.humidity ?: 0,
            windSpeed = wind?.speed ?: 0.0,
            iconCode = weather?.icon ?: "01d",
            lastUpdated = System.currentTimeMillis(),
            latitude = coordinates.latitude ?: 0.0,
            longitude = coordinates.longitude ?: 0.0
        )
    }
}

