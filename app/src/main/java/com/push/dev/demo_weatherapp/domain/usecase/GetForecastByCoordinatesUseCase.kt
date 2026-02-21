package com.push.dev.demo_weatherapp.domain.usecase

import com.push.dev.demo_weatherapp.data.repository.WeatherRepository
import com.push.dev.demo_weatherapp.domain.model.ForecastData
import com.push.dev.demo_weatherapp.utils.Resource
import javax.inject.Inject

/**
 * Use case for fetching forecast by coordinates (daily + hourly until midnight)
 */
class GetForecastByCoordinatesUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double
    ): Resource<ForecastData> {
        return repository.getForecastByCoordinates(latitude, longitude)
    }
}



