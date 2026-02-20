package com.push.dev.demo_weatherapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.push.dev.demo_weatherapp.domain.model.WeatherData
import com.push.dev.demo_weatherapp.utils.WeatherIconHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Component to display weather information
 */
@Composable
fun WeatherContent(
    weatherData: WeatherData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("weather_card"),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // City name
            Text(
                text = weatherData.cityName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("city_name")
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Weather icon
            AsyncImage(
                model = WeatherIconHelper.getIconUrl(weatherData.iconCode),
                contentDescription = weatherData.description,
                modifier = Modifier
                    .size(120.dp)
                    .testTag("weather_icon")
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Temperature
            Text(
                text = "${weatherData.temperature.toInt()}°F",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("temperature")
            )
            
            // Description
            Text(
                text = weatherData.description.replaceFirstChar { 
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("description")
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Weather details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetailItem(
                    icon = Icons.Default.WaterDrop,
                    label = "Humidity",
                    value = "${weatherData.humidity}%",
                    testTag = "humidity"
                )
                
                WeatherDetailItem(
                    icon = Icons.Default.Air,
                    label = "Wind Speed",
                    value = "${weatherData.windSpeed.toInt()} mph",
                    testTag = "wind_speed"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Last updated time
            Text(
                text = "Last updated: ${formatTimestamp(weatherData.lastUpdated)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("last_updated")
            )
        }
    }
}

@Composable
private fun WeatherDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
