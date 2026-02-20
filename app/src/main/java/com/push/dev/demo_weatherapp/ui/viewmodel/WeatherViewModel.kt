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
    private val locationHelper: LocationHelper,
    private val dataStoreHelper: DataStoreHelper
) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()
    
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)
    
    init {
        loadLastSearchedCity()
    }
    
    /**
     * Load last searched city on app launch
     */
    private fun loadLastSearchedCity() {
        viewModelScope.launch {
            try {
                val lastCity = dataStoreHelper.getLastCitySync()
                if (lastCity != null) {
                    searchWeatherByCity(lastCity)
                } else {
                    _uiState.value = WeatherUiState.Idle
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
     */
    private suspend fun fetchWeatherByCoordinates(
        latitude: Double,
        longitude: Double,
        cityName: String
    ) {
        when (val result = getWeatherByCoordinatesUseCase(latitude, longitude)) {
            is Resource.Success -> {
                val weatherData = result.data
                // Save city name to DataStore
                dataStoreHelper.saveLastCity(cityName)
                _uiState.value = WeatherUiState.Success(weatherData)
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
                // Save city name to DataStore
                dataStoreHelper.saveLastCity(cityName)
                _uiState.value = WeatherUiState.Success(weatherData)
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
     */
    fun fetchWeatherByLocation() {
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
                                "" // Will be resolved from coordinates
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
}

/**
 * Sealed class representing UI states
 */
sealed class WeatherUiState {
    object Idle : WeatherUiState()
    object Loading : WeatherUiState()
    data class Success(val weatherData: WeatherData) : WeatherUiState()
    data class Error(val error: WeatherError) : WeatherUiState()
}


