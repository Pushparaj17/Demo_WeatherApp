package com.push.dev.demo_weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.push.dev.demo_weatherapp.ui.screen.WeatherScreen
import com.push.dev.demo_weatherapp.ui.theme.DemoWeatherAppTheme
import com.push.dev.demo_weatherapp.ui.viewmodel.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the weather app
 * Handles location permission requests
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: WeatherViewModel by viewModels()
    
    // Location permission launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            // Permission granted, fetch weather by location
            viewModel.fetchWeatherByLocation()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            DemoWeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherScreen(
                        viewModel = viewModel,
                        onRequestLocationPermission = {
                            requestLocationPermission()
                        }
                    )
                }
            }
        }
        
        // Initialize ViewModel with location permission status
        viewModel.initializeOnLaunch(hasLocationPermission())
    }
    
    /**
     * Request location permission
     */
    private fun requestLocationPermission() {
        if (hasLocationPermission()) {
            viewModel.fetchWeatherByLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    /**
     * Check if location permission is granted
     */
    private fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val coarseLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        return fineLocation || coarseLocation
    }
}