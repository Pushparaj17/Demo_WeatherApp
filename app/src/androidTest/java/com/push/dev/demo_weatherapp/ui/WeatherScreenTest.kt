package com.push.dev.demo_weatherapp.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.push.dev.demo_weatherapp.ui.components.SearchBar
import com.push.dev.demo_weatherapp.ui.components.WeatherContent
import com.push.dev.demo_weatherapp.domain.model.WeatherData
import com.push.dev.demo_weatherapp.domain.model.WeatherError
import com.push.dev.demo_weatherapp.ui.screen.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for Weather Screen components
 */
@RunWith(AndroidJUnit4::class)
class WeatherScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun searchBar_displaysCorrectly() {
        composeTestRule.setContent {
            SearchBar(
                query = "",
                onQueryChange = {},
                onSearch = {},
                onLocationClick = {}
            )
        }
        
        composeTestRule.onNodeWithTag("search_input").assertExists()
        composeTestRule.onNodeWithTag("search_button").assertExists()
        composeTestRule.onNodeWithTag("location_button").assertExists()
    }
    
    @Test
    fun searchBar_entersTextCorrectly() {
        var query = ""
        composeTestRule.setContent {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = {},
                onLocationClick = {}
            )
        }
        
        composeTestRule.onNodeWithTag("search_input")
            .performTextInput("New York")
            .assertTextContains("New York")
    }
    
    @Test
    fun weatherContent_displaysWeatherData() {
        val weatherData = WeatherData(
            cityName = "New York",
            temperature = 72.5,
            description = "clear sky",
            humidity = 65,
            windSpeed = 5.5,
            iconCode = "01d",
            lastUpdated = System.currentTimeMillis(),
            latitude = 40.7128,
            longitude = -74.0060
        )
        
        composeTestRule.setContent {
            WeatherContent(weatherData = weatherData)
        }
        
        composeTestRule.onNodeWithTag("weather_card").assertExists()
        composeTestRule.onNodeWithTag("city_name").assertExists()
        composeTestRule.onNodeWithTag("temperature").assertExists()
        composeTestRule.onNodeWithTag("description").assertExists()
        composeTestRule.onNodeWithTag("humidity").assertExists()
        composeTestRule.onNodeWithTag("wind_speed").assertExists()
    }
    
    @Test
    fun idleContent_displaysCorrectly() {
        composeTestRule.setContent {
            IdleContent()
        }
        
        composeTestRule.onNodeWithTag("idle_content").assertExists()
    }
    
    @Test
    fun loadingContent_displaysCorrectly() {
        composeTestRule.setContent {
            LoadingContent()
        }
        
        composeTestRule.onNodeWithTag("loading_content").assertExists()
    }
    
    @Test
    fun errorContent_displaysError() {
        val error = WeatherError.NetworkError("Network error")
        
        composeTestRule.setContent {
            ErrorContent(
                error = error,
                onRetry = {},
                onDismiss = {}
            )
        }
        
        composeTestRule.onNodeWithTag("error_content").assertExists()
    }
}

