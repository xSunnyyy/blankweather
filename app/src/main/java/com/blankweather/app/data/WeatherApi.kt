package com.blankweather.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class WeatherApi {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getForecast(
        latitude: Double,
        longitude: Double,
        unit: TempUnit,
    ): ForecastResponse {
        return client.get("https://api.open-meteo.com/v1/forecast") {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("current", "temperature_2m,weather_code")
            parameter(
                "hourly",
                "temperature_2m,precipitation_probability,weather_code"
            )
            parameter(
                "daily",
                "weather_code,temperature_2m_max,temperature_2m_min"
            )
            parameter("temperature_unit", unit.apiValue)
            parameter("timezone", "auto")
            parameter("forecast_days", 8)
        }.body()
    }
}
