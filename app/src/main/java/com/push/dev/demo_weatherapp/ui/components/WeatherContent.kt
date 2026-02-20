package com.push.dev.demo_weatherapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        modifier = modifier
            .testTag("weather_card")
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("city_name")
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Last updated time
            Text(
                text = formatTimestamp(weatherData.lastUpdated),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("last_updated")
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Weather icon with a subtle background
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(70.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = WeatherIconHelper.getIconUrl(weatherData.iconCode),
                    contentDescription = weatherData.description,
                    modifier = Modifier
                        .size(100.dp)
                        .testTag("weather_icon")
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Temperature
            Text(
                text = "${weatherData.temperature.toInt()}°",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-2).sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("temperature")
            )
            
            // Description
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = weatherData.description.replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("description")
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Weather details in a grid-like row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WeatherDetailItem(
                    icon = Icons.Default.WaterDrop,
                    label = "Humidity",
                    value = "${weatherData.humidity}%",
                    testTag = "humidity"
                )
                
                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                WeatherDetailItem(
                    icon = Icons.Default.Air,
                    label = "Wind",
                    value = "${weatherData.windSpeed.toInt()} mph",
                    testTag = "wind_speed"
                )
            }
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
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
