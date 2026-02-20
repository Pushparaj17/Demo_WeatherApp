package com.push.dev.demo_weatherapp.di

import android.content.Context
import com.push.dev.demo_weatherapp.BuildConfig
import com.push.dev.demo_weatherapp.data.api.OpenWeatherApiService
import com.push.dev.demo_weatherapp.data.repository.WeatherRepository
import com.push.dev.demo_weatherapp.utils.DataStoreHelper
import com.push.dev.demo_weatherapp.utils.LocationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Dagger-Hilt module for dependency injection
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideOpenWeatherApiService(retrofit: Retrofit): OpenWeatherApiService {
        return retrofit.create(OpenWeatherApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideApiKey(): String {
        return BuildConfig.API_KEY
    }
    
    @Provides
    @Singleton
    fun provideWeatherRepository(
        apiService: OpenWeatherApiService,
        apiKey: String
    ): WeatherRepository {
        return WeatherRepository(apiService, apiKey)
    }
    
    @Provides
    @Singleton
    fun provideLocationHelper(
        @ApplicationContext context: Context
    ): LocationHelper {
        return LocationHelper(context)
    }
    
    @Provides
    @Singleton
    fun provideDataStoreHelper(
        @ApplicationContext context: Context
    ): DataStoreHelper {
        return DataStoreHelper(context)
    }
}

