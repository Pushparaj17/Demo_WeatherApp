package com.push.dev.demo_weatherapp.domain.usecase

import com.push.dev.demo_weatherapp.data.repository.WeatherRepository
import com.push.dev.demo_weatherapp.domain.model.WeatherData
import com.push.dev.demo_weatherapp.utils.Resource
import javax.inject.Inject

/**
 * Use case for fetching weather by city name
 * Follows single responsibility principle
 */
class GetWeatherByCityNameUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(cityName: String): Resource<WeatherData> {
        return repository.getWeatherByCityName(cityName)
    }
}

