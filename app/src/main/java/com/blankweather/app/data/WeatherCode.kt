package com.blankweather.app.data

enum class WeatherKind {
    CLEAR, MAINLY_CLEAR, CLOUDY, FOG, DRIZZLE, RAIN, SNOW, SHOWERS, THUNDERSTORM
}

object WeatherCode {

    fun describe(code: Int): String = when (code) {
        0 -> "Clear"
        1 -> "Mainly Clear"
        2 -> "Partly Cloudy"
        3 -> "Overcast"
        45, 48 -> "Fog"
        51 -> "Light Drizzle"
        53 -> "Drizzle"
        55 -> "Dense Drizzle"
        56, 57 -> "Freezing Drizzle"
        61 -> "Light Rain"
        63 -> "Rain"
        65 -> "Heavy Rain"
        66, 67 -> "Freezing Rain"
        71 -> "Light Snow"
        73 -> "Snow"
        75 -> "Heavy Snow"
        77 -> "Snow Grains"
        80 -> "Light Showers"
        81 -> "Showers"
        82 -> "Violent Showers"
        85 -> "Light Snow Showers"
        86 -> "Snow Showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm w/ Hail"
        else -> "—"
    }

    fun kind(code: Int): WeatherKind = when (code) {
        0 -> WeatherKind.CLEAR
        1 -> WeatherKind.MAINLY_CLEAR
        2, 3 -> WeatherKind.CLOUDY
        45, 48 -> WeatherKind.FOG
        51, 53, 55, 56, 57 -> WeatherKind.DRIZZLE
        61, 63, 65, 66, 67 -> WeatherKind.RAIN
        71, 73, 75, 77, 85, 86 -> WeatherKind.SNOW
        80, 81, 82 -> WeatherKind.SHOWERS
        95, 96, 99 -> WeatherKind.THUNDERSTORM
        else -> WeatherKind.CLOUDY
    }
}
