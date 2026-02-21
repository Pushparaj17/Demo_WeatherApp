package com.push.dev.demo_weatherapp.utils

/**
 * Helper class for weather icon URLs
 * Constructs OpenWeatherMap icon URLs from icon codes
 */
object WeatherIconHelper {
    private const val BASE_ICON_URL = "https://openweathermap.org/img/wn/"
    private const val ICON_SIZE = "@2x" // Use 2x resolution for better quality
    
    /**
     * Get full icon URL from icon code
     * Example: "01d" -> "https://openweathermap.org/img/wn/01d@2x.png"
     */
    fun getIconUrl(iconCode: String): String {
        return "$BASE_ICON_URL$iconCode$ICON_SIZE.png"
    }
}



