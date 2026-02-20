package com.push.dev.demo_weatherapp.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.push.dev.demo_weatherapp.domain.model.WeatherError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Helper class for geocoding operations
 * Converts city names to coordinates using Android Geocoder
 */
class LocationHelper(private val context: Context) {
    
    /**
     * Convert city name to coordinates
     * Prefers US locations
     */
    suspend fun getCoordinatesFromCityName(cityName: String): Result<Pair<Double, Double>> {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.US)
                val addresses: List<Address>? = geocoder.getFromLocationName(cityName, 1)
                
                if (addresses.isNullOrEmpty()) {
                    Result.failure(Exception("City not found: $cityName"))
                } else {
                    val address = addresses.first()
                    val latitude = address.latitude
                    val longitude = address.longitude
                    
                    if (latitude != 0.0 && longitude != 0.0) {
                        Result.success(Pair(latitude, longitude))
                    } else {
                        Result.failure(Exception("Invalid coordinates for city: $cityName"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get city name from coordinates (reverse geocoding)
     */
    suspend fun getCityNameFromCoordinates(
        latitude: Double,
        longitude: Double
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.US)
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                
                if (addresses.isNullOrEmpty()) {
                    Result.failure(Exception("Location not found"))
                } else {
                    val address = addresses.first()
                    val cityName = address.locality ?: address.adminArea ?: "Unknown"
                    Result.success(cityName)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}


