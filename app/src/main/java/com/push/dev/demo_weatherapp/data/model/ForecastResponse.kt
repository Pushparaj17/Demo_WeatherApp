package com.push.dev.demo_weatherapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data model representing the forecast response from OpenWeatherMap API
 */
data class ForecastResponse(
    @SerializedName("cod")
    val cod: String?,
    
    @SerializedName("message")
    val message: Int?,
    
    @SerializedName("cnt")
    val count: Int?,
    
    @SerializedName("list")
    val list: List<ForecastItem>?,
    
    @SerializedName("city")
    val city: ForecastCity?
)

data class ForecastItem(
    @SerializedName("dt")
    val dateTime: Long?,
    
    @SerializedName("main")
    val main: Main?,
    
    @SerializedName("weather")
    val weather: List<Weather>?,
    
    @SerializedName("clouds")
    val clouds: Clouds?,
    
    @SerializedName("wind")
    val wind: Wind?,
    
    @SerializedName("visibility")
    val visibility: Int?,
    
    @SerializedName("pop")
    val pop: Double?,
    
    @SerializedName("dt_txt")
    val dateText: String?
)

data class ForecastCity(
    @SerializedName("id")
    val id: Int?,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("coord")
    val coordinates: Coordinates?,
    
    @SerializedName("country")
    val country: String?,
    
    @SerializedName("population")
    val population: Int?,
    
    @SerializedName("timezone")
    val timezone: Int?,
    
    @SerializedName("sunrise")
    val sunrise: Long?,
    
    @SerializedName("sunset")
    val sunset: Long?
)

