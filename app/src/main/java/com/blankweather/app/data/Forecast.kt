package com.blankweather.app.data

/**
 * Unified domain model the UI consumes. Provider-specific data structures
 * are converted into this in the data layer so the UI doesn't care whether
 * the source is NWS, Open-Meteo, MET Norway, etc. Temperatures are always
 * Celsius; conversion to Fahrenheit happens at display time.
 */
data class Forecast(
    val current: CurrentWeather,
    val hourly: List<HourlyEntry>,
    val daily: List<DailyEntry>,
)

data class CurrentWeather(
    val temperatureC: Double,
    val feelsLikeC: Double,
    val time: String,
    val kind: WeatherKind,
    val description: String,
)

data class HourlyEntry(
    val time: String,
    val temperatureC: Double,
    val probability: Int,
    val kind: WeatherKind,
)

data class DailyEntry(
    val date: String,
    val minC: Double,
    val maxC: Double,
    val kind: WeatherKind,
)
