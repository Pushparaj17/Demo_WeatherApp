package com.push.dev.demo_weatherapp.domain.usecase

import com.push.dev.demo_weatherapp.data.repository.WeatherRepository
import com.push.dev.demo_weatherapp.domain.model.WeatherData
import com.push.dev.demo_weatherapp.utils.Resource
import javax.inject.Inject

/**
 * Use case for fetching weather by coordinates
 * Follows single responsibility principle
 */
class GetWeatherByCoordinatesUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double
    ): Resource<WeatherData> {
        return repository.getWeatherByCoordinates(latitude, longitude)
    }
}



