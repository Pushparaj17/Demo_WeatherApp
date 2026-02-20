# Weather App - Production-Ready Native Android Application

A production-ready Native Android Weather Application built with modern Android development best practices, featuring clean architecture, MVVM pattern, and comprehensive testing.

## 📱 Features

- **City Search**: Search for weather by US city name
- **Location-Based Weather**: Automatic weather fetch using device location (with permission)
- **Weather Details**: Display temperature, humidity, wind speed, weather description, and icons
- **Image Caching**: Efficient weather icon caching using Coil
- **Persistence**: Auto-load last searched city on app launch using DataStore
- **Error Handling**: Comprehensive error handling for network, location, and API errors
- **Modern UI**: Beautiful Material Design 3 UI built with Jetpack Compose

## 🏗 Architecture

The app follows **Clean Architecture** principles with **MVVM** pattern:

```
app/
├── data/           # Data layer - API, models, repository
│   ├── api/        # Retrofit API service
│   ├── model/      # Data models (API response models)
│   └── repository/ # Repository implementation
├── domain/         # Domain layer - business logic
│   ├── model/     # Domain models
│   └── usecase/   # Use cases
├── ui/             # Presentation layer
│   ├── screen/    # Compose screens
│   ├── components/ # Reusable UI components
│   └── viewmodel/ # ViewModels
├── di/             # Dependency injection (Dagger-Hilt)
└── utils/          # Utility classes and helpers
```

### Architecture Principles

- **Separation of Concerns**: Clear separation between data, domain, and presentation layers
- **Single Responsibility**: Each class has a single, well-defined responsibility
- **Dependency Injection**: Using Dagger-Hilt for dependency management
- **Repository Pattern**: Centralized data access logic
- **Use Cases**: Business logic encapsulated in use cases

## 🛠 Tech Stack

### Core Technologies
- **Language**: Kotlin (Primary) + Java (for interoperability demonstration)
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI**: Jetpack Compose
- **Navigation**: Jetpack Navigation Compose
- **Dependency Injection**: Dagger-Hilt

### Libraries
- **Networking**: Retrofit + OkHttp
- **Reactive Programming**: RxJava 3 (for API calls)
- **Coroutines**: Kotlin Coroutines (for async operations)
- **Image Loading**: Coil (with caching)
- **Persistence**: DataStore Preferences
- **Location**: Google Play Services Location
- **Testing**: JUnit, Mockito, MockK, Compose UI Test

## 🔐 API Configuration

### Setting Up API Key

1. Get your API key from [OpenWeatherMap](https://openweathermap.org/api)

2. Add the API key to `local.properties` file in the project root:
   ```properties
   OPENWEATHER_API_KEY=your_api_key_here
   ```

3. The API key will be automatically injected into `BuildConfig.API_KEY` during build

**Note**: If `local.properties` doesn't exist or doesn't contain the API key, the app will use a dummy key (`DUMMY_API_KEY_123456`) which won't work with the actual API.

### API Endpoints

The app uses OpenWeatherMap API:
- **Base URL**: `https://api.openweathermap.org/data/2.5/`
- **Preferred Method**: Coordinates-based API (`/weather?lat={lat}&lon={lon}`)
- **Fallback Method**: City name API (`/weather?q={city name}`)

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or later
- Android SDK 29 (Android 10) or higher
- Gradle 8.0 or later

### Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd Demo-WeatherApp
   ```

2. Add your OpenWeatherMap API key to `local.properties`:
   ```properties
   OPENWEATHER_API_KEY=your_api_key_here
   ```

3. Sync Gradle files and build the project

4. Run the app on an emulator or physical device

## 📦 Project Structure

```
Demo-WeatherApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/push/dev/demo_weatherapp/
│   │   │   │   ├── data/
│   │   │   │   │   ├── api/              # Retrofit API service
│   │   │   │   │   ├── model/            # API response models
│   │   │   │   │   └── repository/       # Repository implementation
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/            # Domain models
│   │   │   │   │   └── usecase/          # Use cases
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/       # Reusable UI components
│   │   │   │   │   ├── screen/          # Compose screens
│   │   │   │   │   └── viewmodel/       # ViewModels
│   │   │   │   ├── di/                  # Dependency injection modules
│   │   │   │   ├── utils/               # Utility classes
│   │   │   │   ├── MainActivity.kt      # Main activity
│   │   │   │   └── WeatherApplication.kt # Application class
│   │   │   └── res/                      # Resources
│   │   ├── test/                         # Unit tests
│   │   └── androidTest/                  # Instrumented tests
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml                # Version catalog
└── README.md
```

## 🧪 Testing

### Unit Tests

Run unit tests:
```bash
./gradlew test
```

**Test Coverage**:
- `WeatherRepositoryTest`: Tests repository logic and API error handling
- `WeatherViewModelTest`: Tests ViewModel state management and business logic

### UI Tests

Run UI tests:
```bash
./gradlew connectedAndroidTest
```

**UI Test Coverage**:
- Search bar interaction
- Weather content display
- Error state display
- Loading state display

## 🔄 Key Features Implementation

### 1. Location Permission Handling

The app requests location permission on launch and automatically fetches weather if granted. If denied, users can manually search for cities.

**Implementation**: `MainActivity` handles permission requests using `ActivityResultContracts.RequestMultiplePermissions()`

### 2. Geocoding

City names are converted to coordinates using Android's `Geocoder` API before making weather API calls (preferred method).

**Implementation**: `LocationHelper` class handles geocoding operations

### 3. Image Caching

Weather icons are cached efficiently using Coil library, reducing network calls and improving performance.

**Implementation**: `WeatherIconHelper` constructs icon URLs, Coil handles caching automatically

### 4. Data Persistence

Last searched city is saved using DataStore Preferences and automatically loaded on app launch.

**Implementation**: `DataStoreHelper` manages DataStore operations

### 5. Error Handling

Comprehensive error handling for:
- Network errors (no internet, timeout)
- API errors (404, 401, etc.)
- Location errors (permission denied, location unavailable)
- JSON parsing errors

**Implementation**: `Resource` sealed class and `WeatherError` sealed class

## 🎨 UI Components

### SearchBar
- Text input for city search
- Search button
- Location button for current location

### WeatherContent
- City name
- Weather icon (cached)
- Temperature
- Description
- Humidity and wind speed
- Last updated timestamp

### ErrorContent
- Error message display
- Retry button
- Dismiss button

## 🔧 Configuration

### Build Configuration

The app uses `BuildConfig` to store the API key securely:
- API key is read from `local.properties` during build
- Stored in `BuildConfig.API_KEY`
- Never committed to version control

### Network Configuration

- **Base URL**: Configured in `AppModule`
- **Timeout**: 30 seconds for connect, read, and write
- **Logging**: Enabled in debug builds only

## 📝 Code Quality

### Best Practices Implemented

- ✅ Null safety (Kotlin null safety features)
- ✅ Defensive programming (null checks, error handling)
- ✅ Proper error handling (sealed classes for error types)
- ✅ Logging (proper logging for debugging)
- ✅ Comments (important decisions documented)
- ✅ Accessibility (content descriptions, semantic modifiers)
- ✅ Localization (string resources, no hardcoded strings)

### Code Comments

Important architectural decisions and workarounds are documented with comments:
```kotlin
// TODO: Given more time, I would refactor this into a ResultWrapper sealed class.
```

## 🚧 Future Improvements

1. **Offline Support**: Cache weather data for offline access
2. **Multiple Cities**: Save and manage multiple favorite cities
3. **Weather Forecast**: Add 5-day weather forecast
4. **Weather Maps**: Display weather on interactive maps
5. **Notifications**: Weather alerts and notifications
6. **Widgets**: Home screen widgets for quick weather access
7. **Dark Mode**: Enhanced dark theme support
8. **Animations**: Add smooth transitions and animations
9. **Unit Test Coverage**: Increase test coverage to 90%+
10. **CI/CD**: Set up continuous integration and deployment

## 📄 License

This project is a demonstration project for educational purposes.

## 👨‍💻 Development Notes

### Java Interoperability

The app includes a Java component (`JavaInteropHelper.java`) to demonstrate Kotlin-Java interoperability. This component provides utility functions that can be called from both Java and Kotlin code.

### RxJava Usage

RxJava is used for the API service to demonstrate reactive programming. The `OpenWeatherApiService` returns `Single<WeatherResponse>` which is then converted to coroutines using `await()` extension.

### Defensive Programming

- All API responses are checked for null values
- Safe parsing with try-catch blocks
- Proper error messages for users
- Graceful degradation when services are unavailable

## 🤝 Contributing

This is a demonstration project. For production use, consider:
- Adding more comprehensive error handling
- Implementing retry mechanisms
- Adding analytics
- Implementing proper logging framework
- Adding crash reporting (e.g., Firebase Crashlytics)

---

**Built with ❤️ using modern Android development practices**


