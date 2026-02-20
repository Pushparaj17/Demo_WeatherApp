package com.push.dev.demo_weatherapp.ui.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.push.dev.demo_weatherapp.domain.model.WeatherData
import com.push.dev.demo_weatherapp.domain.model.WeatherError
import com.push.dev.demo_weatherapp.domain.usecase.GetWeatherByCityNameUseCase
import com.push.dev.demo_weatherapp.domain.usecase.GetWeatherByCoordinatesUseCase
import com.push.dev.demo_weatherapp.utils.DataStoreHelper
import com.push.dev.demo_weatherapp.utils.LocationHelper
import com.push.dev.demo_weatherapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for weather screen
 * Manages UI state and handles business logic
 */

@HiltViewModel
class WeatherViewModel @Inject constructor(
    application: Application,
    private val getWeatherByCoordinatesUseCase: GetWeatherByCoordinatesUseCase,
    private val getWeatherByCityNameUseCase: GetWeatherByCityNameUseCase,
    private val getForecastByCoordinatesUseCase: com.push.dev.demo_weatherapp.domain.usecase.GetForecastByCoordinatesUseCase,
    private val getForecastByCityNameUseCase: com.push.dev.demo_weatherapp.domain.usecase.GetForecastByCityNameUseCase,
    private val locationHelper: LocationHelper,
    private val dataStoreHelper: DataStoreHelper
) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()
    
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)
    
    private var hasInitialized = false
    
    /**
     * Initialize on app launch
     * Priority: Saved city > Location (first time only) > Idle
     */
    fun initializeOnLaunch(hasLocationPermission: Boolean) {
        if (hasInitialized) return
        hasInitialized = true
        
        viewModelScope.launch {
            try {
                val lastCity = dataStoreHelper.getLastCitySync()
                
                if (lastCity != null) {
                    // If there's a saved city, load it (don't use location)
                    searchWeatherByCity(lastCity)
                } else {
                    // First time launch - no saved city
                    // Try location if permission granted, otherwise show idle
                    // Don't save location city on first launch
                    if (hasLocationPermission) {
                        fetchWeatherByLocation(saveCity = false)
                    } else {
                        _uiState.value = WeatherUiState.Idle
                    }
                }
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Idle
            }
        }
    }
    
    /**
     * Search weather by city name
     */
    fun searchWeatherByCity(cityName: String) {
        if (cityName.isBlank()) {
            _uiState.value = WeatherUiState.Error(WeatherError.UnknownError("City name cannot be empty"))
            return
        }
        
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            
            // First, try to get coordinates from city name using Geocoder
            val coordinatesResult = locationHelper.getCoordinatesFromCityName(cityName)
            
            when {
                coordinatesResult.isSuccess -> {
                    val (lat, lon) = coordinatesResult.getOrNull() ?: return@launch
                    fetchWeatherByCoordinates(lat, lon, cityName)
                }
                else -> {
                    // Fallback to direct city name API call
                    fetchWeatherByCityName(cityName)
                }
            }
        }
    }
    
    /**
     * Fetch weather using coordinates (preferred method)
     * @param saveCity If true, saves the city name to DataStore. Set to false for location-based fetches on first launch.
     */
    private suspend fun fetchWeatherByCoordinates(
        latitude: Double,
        longitude: Double,
        cityName: String,
        saveCity: Boolean = true
    ) {
        when (val result = getWeatherByCoordinatesUseCase(latitude, longitude)) {
            is Resource.Success -> {
                val weatherData = result.data
                // Fetch forecast for 7 days
                val forecastResult = getForecastByCoordinatesUseCase(latitude, longitude)
                val forecast = when (forecastResult) {
                    is Resource.Success -> {
                        // Ensure we have 7 days, pad if needed
                        val forecastList = forecastResult.data
                        if (forecastList.size >= 7) {
                            forecastList.take(7)
                        } else {
                            // If forecast has fewer than 7 days, pad with current weather
                            val padded = forecastList.toMutableList()
                            while (padded.size < 7) {
                                val lastDay = padded.lastOrNull() ?: weatherData
                                val oneDayMillis = 24L * 60L * 60L * 1000L
                                padded.add(
                                    lastDay.copy(
                                        lastUpdated = lastDay.lastUpdated + (padded.size * oneDayMillis)
                                    )
                                )
                            }
                            padded.take(7)
                        }
                    }
                    else -> {
                        // Fallback to building forecast from current weather
                        buildSevenDayForecast(weatherData)
                    }
                }
                
                // Save city name to DataStore only if saveCity is true
                if (saveCity) {
                    val cityToSave = if (cityName.isNotBlank()) {
                        cityName
                    } else {
                        weatherData.cityName
                    }
                    dataStoreHelper.saveLastCity(cityToSave)
                }
                
                _uiState.value = WeatherUiState.Success(
                    weatherData = weatherData,
                    forecast = forecast,
                    selectedIndex = 0
                )
            }
            is Resource.Error -> {
                _uiState.value = WeatherUiState.Error(result.error)
            }
            is Resource.Loading -> {
                // Already in loading state
            }
        }
    }
    
    /**
     * Fetch weather using city name (fallback method)
     */
    private suspend fun fetchWeatherByCityName(cityName: String) {
        when (val result = getWeatherByCityNameUseCase(cityName)) {
            is Resource.Success -> {
                val weatherData = result.data
                // Try to fetch forecast by city name
                val forecastResult = getForecastByCityNameUseCase(cityName)
                val forecast = when (forecastResult) {
                    is Resource.Success -> {
                        val forecastList = forecastResult.data
                        if (forecastList.size >= 7) {
                            forecastList.take(7)
                        } else {
                            // Pad if needed
                            val padded = forecastList.toMutableList()
                            while (padded.size < 7) {
                                val lastDay = padded.lastOrNull() ?: weatherData
                                val oneDayMillis = 24L * 60L * 60L * 1000L
                                padded.add(
                                    lastDay.copy(
                                        lastUpdated = lastDay.lastUpdated + (padded.size * oneDayMillis)
                                    )
                                )
                            }
                            padded.take(7)
                        }
                    }
                    else -> {
                        // Fallback to building forecast from current weather
                        buildSevenDayForecast(weatherData)
                    }
                }
                
                // Save city name to DataStore
                dataStoreHelper.saveLastCity(cityName)
                _uiState.value = WeatherUiState.Success(
                    weatherData = weatherData,
                    forecast = forecast,
                    selectedIndex = 0
                )
            }
            is Resource.Error -> {
                _uiState.value = WeatherUiState.Error(result.error)
            }
            is Resource.Loading -> {
                // Already in loading state
            }
        }
    }
    
    /**
     * Fetch weather using device location
     * @param saveCity If true, saves the location city to DataStore. Set to false for first launch.
     */
    fun fetchWeatherByLocation(saveCity: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            
            try {
                // Use callback-based approach for location
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    viewModelScope.launch {
                        if (location != null) {
                            fetchWeatherByCoordinates(
                                location.latitude,
                                location.longitude,
                                "", // Will be resolved from coordinates
                                saveCity = saveCity
                            )
                        } else {
                            _uiState.value = WeatherUiState.Error(
                                WeatherError.LocationError("Unable to get current location")
                            )
                        }
                    }
                }.addOnFailureListener { exception ->
                    viewModelScope.launch {
                        _uiState.value = WeatherUiState.Error(
                            WeatherError.LocationError("Location error: ${exception.message}")
                        )
                    }
                }
            } catch (e: SecurityException) {
                _uiState.value = WeatherUiState.Error(
                    WeatherError.LocationError("Location permission denied")
                )
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(
                    WeatherError.LocationError("Location error: ${e.message}")
                )
            }
        }
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        if (_uiState.value is WeatherUiState.Error) {
            _uiState.value = WeatherUiState.Idle
        }
    }

    /**
     * Handle selection of a specific forecast day (by index 0..6)
     */
    fun selectForecastDay(index: Int) {
        val current = _uiState.value
        if (current is WeatherUiState.Success && index in current.forecast.indices) {
            val selectedWeather = current.forecast[index]
            _uiState.value = current.copy(
                weatherData = selectedWeather,
                selectedIndex = index
            )
        }
    }

    /**
     * Build a simple 7-day list based on the current day's weather.
     * Used as fallback when forecast API is unavailable.
     */
    private fun buildSevenDayForecast(base: WeatherData): List<WeatherData> {
        val oneDayMillis = 24L * 60L * 60L * 1000L
        return (0..6).map { offset ->
            base.copy(
                lastUpdated = base.lastUpdated + offset * oneDayMillis
            )
        }
    }
}

/**
 * Sealed class representing UI states
 */
sealed class WeatherUiState {
    object Idle : WeatherUiState()
    object Loading : WeatherUiState()
    data class Success(
        val weatherData: WeatherData,
        val forecast: List<WeatherData> = emptyList(),
        val selectedIndex: Int = 0
    ) : WeatherUiState()
    data class Error(val error: WeatherError) : WeatherUiState()
}


