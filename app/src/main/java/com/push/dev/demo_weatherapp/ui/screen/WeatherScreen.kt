package com.push.dev.demo_weatherapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.push.dev.demo_weatherapp.R
import com.push.dev.demo_weatherapp.domain.model.WeatherError
import com.push.dev.demo_weatherapp.ui.components.SearchBar
import com.push.dev.demo_weatherapp.ui.components.WeatherContent
import com.push.dev.demo_weatherapp.ui.viewmodel.WeatherUiState
import com.push.dev.demo_weatherapp.ui.viewmodel.WeatherViewModel

/**
 * Main weather screen composable
 */
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel(),
    onRequestLocationPermission: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    
    val colorTop = MaterialTheme.colorScheme.primaryContainer
    val colorBottom = MaterialTheme.colorScheme.background
    val backgroundBrush = remember(colorTop, colorBottom) {
        androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                colorTop.copy(alpha = 0.4f),
                colorBottom
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App title at the top
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        // Search bar centered horizontally
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = {
                    if (searchQuery.isNotBlank()) {
                        viewModel.searchWeatherByCity(searchQuery.trim())
                    }
                },
                onLocationClick = {
                    onRequestLocationPermission()
                },
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Weather content based on state
        when (val state = uiState) {
            is WeatherUiState.Idle -> {
                IdleContent()
            }
            is WeatherUiState.Loading -> {
                LoadingContent()
            }
            is WeatherUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    key(state.weatherData.lastUpdated, state.selectedIndex, state.selectedHourlyIndex) {
                        WeatherContent(
                            weatherData = state.weatherData,
                            showTimeOnly = state.showHourlyForecast,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (state.forecast.isNotEmpty() || state.hourlyToday.isNotEmpty()) {
                        ForecastViewToggle(
                            showHourlyForecast = state.showHourlyForecast,
                            hasHourlyData = state.hourlyToday.isNotEmpty(),
                            onToggle = { viewModel.setShowHourlyForecast(it) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    when {
                        state.showHourlyForecast && state.hourlyToday.isNotEmpty() -> {
                            HourlyForecastSelector(
                                hours = state.hourlyToday,
                                selectedIndex = state.selectedHourlyIndex,
                                onHourSelected = { viewModel.selectHourlySlot(it) }
                            )
                        }
                        state.forecast.isNotEmpty() -> {
                            ForecastDaySelector(
                                days = state.forecast,
                                selectedIndex = state.selectedIndex,
                                onDaySelected = { index -> viewModel.selectForecastDay(index) }
                            )
                        }
                    }
                }
            }
            is WeatherUiState.Error -> {
                ErrorContent(
                    error = state.error,
                    onRetry = {
                        if (searchQuery.isNotBlank()) {
                            viewModel.searchWeatherByCity(searchQuery.trim())
                        }
                    },
                    onDismiss = { viewModel.clearError() }
                )
            }
        }
    }
}

@Composable
private fun ForecastViewToggle(
    showHourlyForecast: Boolean,
    hasHourlyData: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = !showHourlyForecast,
            onClick = { onToggle(false) },
            label = { Text(stringResource(R.string.forecast_next_7_days)) },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = showHourlyForecast,
            onClick = { if (hasHourlyData) onToggle(true) },
            enabled = hasHourlyData,
            label = { Text(stringResource(R.string.forecast_rest_of_today)) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HourlyForecastSelector(
    hours: List<com.push.dev.demo_weatherapp.domain.model.WeatherData>,
    selectedIndex: Int,
    onHourSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.forecast_today_until_midnight),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            hours.forEachIndexed { index, hourWeather ->
                val isSelected = index == selectedIndex
                FilledTonalButton(
                    onClick = { onHourSelected(index) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.defaultMinSize(minWidth = 72.dp)
                ) {
                    val timeText = remember(hourWeather.lastUpdated) {
                        val sdf = java.text.SimpleDateFormat("h a", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(hourWeather.lastUpdated))
                    }
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "${hourWeather.temperature.toInt()}°",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastDaySelector(
    days: List<com.push.dev.demo_weatherapp.domain.model.WeatherData>,
    selectedIndex: Int,
    onDaySelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.forecast_next_7_days),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.forEachIndexed { index, dayWeather ->
                val isSelected = index == selectedIndex
                FilledTonalButton(
                    onClick = { onDaySelected(index) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .defaultMinSize(minWidth = 72.dp)
                ) {
                    val dateText = remember(dayWeather.lastUpdated) {
                        val sdf = java.text.SimpleDateFormat("EEE\ndd", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(dayWeather.lastUpdated))
                    }
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun IdleContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("idle_content"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.idle_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.idle_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("loading_content"),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    error: WeatherError,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("error_content"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.error_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val errorMessage = when (error) {
                is WeatherError.NetworkError -> error.message
                is WeatherError.CityNotFound -> error.message
                is WeatherError.ApiKeyError -> error.message
                is WeatherError.LocationError -> error.message
                is WeatherError.UnknownError -> error.message
            }
            
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dismiss))
                }
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}
