package com.blankweather.app.data

import android.content.Context

class WeatherRepository(context: Context) {
    private val api = WeatherApi()
    private val locationProvider = LocationProvider(context)

    suspend fun load(): WeatherSnapshot {
        val location = locationProvider.currentLocation()
            ?: throw IllegalStateException("Location unavailable. Make sure location is enabled.")
        val city = locationProvider.cityName(location)
        val forecast = api.getForecast(location.latitude, location.longitude)
        return WeatherSnapshot(city = city, forecast = forecast)
    }

    fun hasLocationPermission(): Boolean = locationProvider.hasPermission()
}

data class WeatherSnapshot(
    val city: String?,
    val forecast: ForecastResponse,
)
