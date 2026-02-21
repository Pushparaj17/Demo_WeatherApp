package com.push.dev.demo_weatherapp.utils

/**
 * Wrapper class for handling API responses
 * Represents loading, success, and error states
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val error: com.push.dev.demo_weatherapp.domain.model.WeatherError) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}



