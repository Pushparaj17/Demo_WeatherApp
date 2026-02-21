# Weather App

Simple native Android weather app built using Kotlin + Jetpack Compose with MVVM and Clean Architecture.

Uses OpenWeatherMap API to fetch weather by city name or current location.

# Features

Search weather by city name or zip code

Fetch weather using device location

Shows Temperature, Weather description, Humidity, Wind speed, Weather icon

Caches icons using Coil

Saves last searched city (DataStore)

Handles API + network errors

# Tech Stack

Kotlin

Jetpack Compose

MVVM

Dagger-Hilt

Retrofit + OkHttp

Coroutines

RxJava3

DataStore

Coil

Google Play Location

JUnit / Mockito / MockK

# Architecture

Clean Architecture + MVVM.

data      -> API, models, repository

domain    -> business logic (use cases, domain models)

ui        -> compose screens + viewmodels

di        -> hilt modules

utils     -> helpers
 
# Testing

## Run unit tests

./gradlew test

## Run UI tests

./gradlew connectedAndroidTest
