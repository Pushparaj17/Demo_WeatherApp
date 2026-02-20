package com.push.dev.demo_weatherapp.utils;

import android.util.Log;

/**
 * Java interoperability component
 * Demonstrates Java-Kotlin interoperability
 * This class can be called from both Java and Kotlin code
 */
public class JavaInteropHelper {
    private static final String TAG = "JavaInteropHelper";
    
    /**
     * Validates if a city name is valid (non-empty and contains only letters and spaces)
     * This method can be called from both Java and Kotlin
     */
    public static boolean isValidCityName(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return false;
        }
        
        // Check if city name contains only letters, spaces, and common city name characters
        String trimmed = cityName.trim();
        return trimmed.matches("^[a-zA-Z\\s\\-']+$") && trimmed.length() >= 2;
    }
    
    /**
     * Formats temperature for display
     * Can be called from both Java and Kotlin
     */
    public static String formatTemperature(double temperature) {
        int tempInt = (int) Math.round(temperature);
        return tempInt + "°F";
    }
    
    /**
     * Logs a debug message
     * Demonstrates Java calling Android APIs
     */
    public static void logDebug(String message) {
        Log.d(TAG, message);
    }
    
    /**
     * Logs an error message
     */
    public static void logError(String message, Throwable throwable) {
        Log.e(TAG, message, throwable);
    }
}


