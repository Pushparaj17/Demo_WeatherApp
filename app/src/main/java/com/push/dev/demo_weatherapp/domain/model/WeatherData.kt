package com.push.dev.demo_weatherapp.domain.model

/**
 * Domain model representing weather data
 * This is the clean domain model used throughout the app
 */
data class WeatherData(
    val cityName: String,
    val temperature: Double,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val iconCode: String,
    val lastUpdated: Long,
    val latitude: Double,
    val longitude: Double
)

/**
 * Sealed class representing different types of weather errors
 */
sealed class WeatherError(message: String) {
    data class NetworkError(val message: String) : WeatherError(message)
    data class CityNotFound(val message: String) : WeatherError(message)
    data class ApiKeyError(val message: String) : WeatherError(message)
    data class LocationError(val message: String) : WeatherError(message)
    data class UnknownError(val message: String) : WeatherError(message)
}


