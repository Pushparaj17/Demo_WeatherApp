package com.push.dev.demo_weatherapp.domain.model

/**
 * Forecast data containing both daily and hourly (today until midnight) forecasts.
 */
data class ForecastData(
    val daily: List<WeatherData>,
    val hourlyToday: List<WeatherData>
)
