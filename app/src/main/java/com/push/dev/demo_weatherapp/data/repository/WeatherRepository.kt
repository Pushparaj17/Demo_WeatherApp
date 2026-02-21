package com.push.dev.demo_weatherapp.data.repository

import com.push.dev.demo_weatherapp.data.api.OpenWeatherApiService
import com.push.dev.demo_weatherapp.data.model.ForecastResponse
import com.push.dev.demo_weatherapp.data.model.WeatherResponse
import com.push.dev.demo_weatherapp.data.model.ForecastItem
import com.push.dev.demo_weatherapp.domain.model.ForecastData
import com.push.dev.demo_weatherapp.domain.model.WeatherData
import com.push.dev.demo_weatherapp.domain.model.WeatherError
import com.push.dev.demo_weatherapp.utils.Resource
import kotlinx.coroutines.rx3.await
import java.util.Calendar
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
     * Fetch forecast by coordinates (daily + hourly until midnight today)
     */
    suspend fun getForecastByCoordinates(
        latitude: Double,
        longitude: Double
    ): Resource<ForecastData> {
        return try {
            val response = apiService.getForecastByCoordinates(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey
            ).await()
            
            if (response.cod == "200" && response.list != null && response.city != null) {
                Resource.Success(response.toForecastData())
            } else {
                Resource.Error(WeatherError.UnknownError("Invalid forecast response from API"))
            }
        } catch (e: Exception) {
            Resource.Error(handleException(e))
        }
    }
    
    /**
     * Fetch forecast by city name (daily + hourly until midnight today)
     */
    suspend fun getForecastByCityName(cityName: String): Resource<ForecastData> {
        return try {
            val response = apiService.getForecastByCityName(
                cityName = cityName,
                apiKey = apiKey
            ).await()
            
            if (response.cod == "200" && response.list != null && response.city != null) {
                Resource.Success(response.toForecastData())
            } else {
                Resource.Error(WeatherError.CityNotFound("Forecast not found for city: $cityName"))
            }
        } catch (e: Exception) {
            Resource.Error(handleException(e))
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
            lastUpdated = (dateTime ?: System.currentTimeMillis() / 1000) * 1000,
            latitude = coordinates.latitude ?: 0.0,
            longitude = coordinates.longitude ?: 0.0
        )
    }
    
    /**
     * Convert forecast response to ForecastData (daily + hourly until midnight today)
     */
    private fun ForecastResponse.toForecastData(): ForecastData {
        val cityName = city?.name ?: "Unknown"
        val latitude = city?.coordinates?.latitude ?: 0.0
        val longitude = city?.coordinates?.longitude ?: 0.0
        val forecastItems = list ?: return ForecastData(emptyList(), emptyList())

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val midnightTonight = calendar.timeInMillis

        // Hourly: from now until 12 AM (midnight)
        val hourlyToday = forecastItems
            .filter { item ->
                val itemTime = (item.dateTime ?: 0L) * 1000L
                itemTime >= now && itemTime < midnightTonight
            }
            .map { it.toWeatherData(cityName, latitude, longitude) }

        // Daily: group by day, one item per day (around noon)
        val dailyForecast = mutableListOf<WeatherData>()
        val oneDayMs = 24L * 60 * 60 * 1000
        for (dayOffset in 0..6) {
            val targetDayStart = todayStart + (dayOffset * oneDayMs)
            val targetDayEnd = targetDayStart + oneDayMs
            val dayItems = forecastItems.filter { item ->
                val itemTime = (item.dateTime ?: 0L) * 1000L
                itemTime >= targetDayStart && itemTime < targetDayEnd
            }
            if (dayItems.isNotEmpty()) {
                val noonTime = targetDayStart + (12L * 60 * 60 * 1000)
                val closestItem = dayItems.minByOrNull { item ->
                    kotlin.math.abs((item.dateTime ?: 0L) * 1000L - noonTime)
                } ?: dayItems.first()
                dailyForecast.add(closestItem.toWeatherData(cityName, latitude, longitude))
            }
        }

        return ForecastData(daily = dailyForecast, hourlyToday = hourlyToday)
    }

    private fun ForecastItem.toWeatherData(
        cityName: String,
        latitude: Double,
        longitude: Double
    ): WeatherData {
        val weather = weather?.firstOrNull()
        val main = main ?: throw IllegalStateException("Main data is null")
        return WeatherData(
            cityName = cityName,
            temperature = main.temp ?: 0.0,
            description = weather?.description ?: "Unknown",
            humidity = main.humidity ?: 0,
            windSpeed = wind?.speed ?: 0.0,
            iconCode = weather?.icon ?: "01d",
            lastUpdated = (dateTime ?: System.currentTimeMillis() / 1000) * 1000,
            latitude = latitude,
            longitude = longitude
        )
    }
}

