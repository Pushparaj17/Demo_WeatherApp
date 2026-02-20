package com.push.dev.demo_weatherapp.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_prefs")

/**
 * Helper class for managing DataStore preferences
 * Handles persistence of last searched city
 */
@Singleton
class DataStoreHelper @Inject constructor(
    private val context: Context
) {
    companion object {
        private val LAST_CITY_KEY = stringPreferencesKey("last_searched_city")
    }
    
    /**
     * Save last searched city name
     */
    suspend fun saveLastCity(cityName: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_CITY_KEY] = cityName
        }
    }
    
    /**
     * Get last searched city name as Flow
     */
    fun getLastCity(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[LAST_CITY_KEY]
        }
    }
    
    /**
     * Get last searched city name synchronously (for initial load)
     */
    suspend fun getLastCitySync(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[LAST_CITY_KEY]
        }.first()
    }
}

