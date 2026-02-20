package com.push.dev.demo_weatherapp.data.api

import com.push.dev.demo_weatherapp.data.model.ForecastResponse
import com.push.dev.demo_weatherapp.data.model.WeatherResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API service for OpenWeatherMap API
 * Uses RxJava Single for reactive stream demonstration
 */
interface OpenWeatherApiService {
    
    /**
     * Fetch weather by coordinates (preferred method)
     */
    @GET("weather")
    fun getWeatherByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): Single<WeatherResponse>
    
    /**
     * Fetch weather by city name (fallback method)
     */
    @GET("weather")
    fun getWeatherByCityName(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): Single<WeatherResponse>
    
    /**
     * Fetch 5-day forecast by coordinates
     */
    @GET("forecast")
    fun getForecastByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): Single<ForecastResponse>
    
    /**
     * Fetch 5-day forecast by city name
     */
    @GET("forecast")
    fun getForecastByCityName(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): Single<ForecastResponse>
}

