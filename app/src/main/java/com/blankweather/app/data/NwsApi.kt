package com.blankweather.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.OffsetDateTime

class NwsApi {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            })
        }
        defaultRequest {
            header(HttpHeaders.UserAgent, USER_AGENT)
            header(HttpHeaders.Accept, "application/geo+json")
        }
    }

    suspend fun loadForecast(latitude: Double, longitude: Double): Forecast {
        val points = points(latitude, longitude)
            ?: throw OutsideCoverageException()
        val props = points.properties

        val hourlyDto = hourly(props.forecastHourly)
        val dailyDto = daily(props.forecast)
        val current = currentObservation(props.observationStations)

        return Forecast(
            current = current ?: deriveCurrentFromHourly(hourlyDto),
            hourly = hourlyDto.toHourlyEntries(),
            daily = dailyDto.toDailyEntries(),
        )
    }

    private suspend fun points(lat: Double, lng: Double): PointsResponse? {
        val response: HttpResponse = client.get("$BASE/points/$lat,$lng")
        return when (response.status) {
            HttpStatusCode.OK -> response.body()
            HttpStatusCode.NotFound -> null
            else -> throw IllegalStateException("NWS points failed: ${response.status}")
        }
    }

    private suspend fun hourly(url: String): HourlyForecast {
        val sanitized = appendSi(url)
        return client.get(sanitized).body()
    }

    private suspend fun daily(url: String): DailyForecast {
        val sanitized = appendSi(url)
        return client.get(sanitized).body()
    }

    private suspend fun currentObservation(stationsUrl: String): CurrentWeather? = try {
        val stations: StationsResponse = client.get(stationsUrl).body()
        val stationId = stations.features.firstOrNull()
            ?.properties?.stationIdentifier ?: return null
        val obs: ObservationResponse = client.get(
            "$BASE/stations/$stationId/observations/latest"
        ).body()
        val temp = obs.properties.temperature.value
        if (temp == null) null else CurrentWeather(
            temperatureC = temp,
            time = obs.properties.timestamp,
            kind = NwsIcon.kindFromIconUrl(obs.properties.icon),
            description = obs.properties.textDescription
                ?.takeIf { it.isNotBlank() } ?: "—",
        )
    } catch (_: Throwable) {
        null
    }

    private fun deriveCurrentFromHourly(forecast: HourlyForecast): CurrentWeather {
        val first = forecast.properties.periods.firstOrNull()
            ?: error("NWS returned no hourly periods")
        return CurrentWeather(
            temperatureC = first.temperature.toCelsius(first.temperatureUnit),
            time = first.startTime,
            kind = NwsIcon.kindFromIconUrl(first.icon),
            description = first.shortForecast ?: "—",
        )
    }

    private fun appendSi(url: String): String {
        val builder = URLBuilder(url)
        builder.parameters["units"] = "si"
        return builder.buildString()
    }

    companion object {
        const val BASE = "https://api.weather.gov"
        const val USER_AGENT = "BlankWeather (https://github.com/xSunnyyy/blankweather)"
    }
}

class OutsideCoverageException :
    IllegalStateException("This location is outside NWS coverage (US only).")

private fun Double.toCelsius(unit: String?): Double =
    when (unit?.uppercase()) {
        "F" -> (this - 32) * 5.0 / 9.0
        else -> this
    }

private fun HourlyForecast.toHourlyEntries(): List<HourlyEntry> =
    properties.periods.take(48).map { p ->
        HourlyEntry(
            time = p.startTime,
            temperatureC = p.temperature.toCelsius(p.temperatureUnit),
            probability = p.probabilityOfPrecipitation?.value ?: 0,
            kind = NwsIcon.kindFromIconUrl(p.icon),
        )
    }

private fun DailyForecast.toDailyEntries(): List<DailyEntry> {
    val byDate = linkedMapOf<LocalDate, DailyAccumulator>()
    for (period in properties.periods) {
        val date = LocalDate.from(OffsetDateTime.parse(period.startTime))
        val acc = byDate.getOrPut(date) { DailyAccumulator() }
        val tempC = period.temperature.toCelsius(period.temperatureUnit)
        if (period.isDaytime) {
            acc.maxC = tempC
            acc.kind = NwsIcon.kindFromIconUrl(period.icon)
        } else {
            acc.minC = tempC
            if (acc.kind == null) acc.kind = NwsIcon.kindFromIconUrl(period.icon)
        }
    }
    return byDate.entries.take(7).map { (date, acc) ->
        DailyEntry(
            date = date.toString(),
            minC = acc.minC ?: acc.maxC ?: 0.0,
            maxC = acc.maxC ?: acc.minC ?: 0.0,
            kind = acc.kind ?: WeatherKind.CLOUDY,
        )
    }
}

private class DailyAccumulator(
    var minC: Double? = null,
    var maxC: Double? = null,
    var kind: WeatherKind? = null,
)

// ---------- DTOs ----------

@Serializable
private data class PointsResponse(val properties: PointsProperties)

@Serializable
private data class PointsProperties(
    val forecast: String,
    val forecastHourly: String,
    val observationStations: String,
    val relativeLocation: RelativeLocation? = null,
)

@Serializable
private data class RelativeLocation(val properties: RelativeLocationProperties)

@Serializable
private data class RelativeLocationProperties(
    val city: String,
    val state: String,
)

@Serializable
private data class HourlyForecast(val properties: HourlyProperties)

@Serializable
private data class HourlyProperties(val periods: List<HourlyPeriod>)

@Serializable
private data class HourlyPeriod(
    val number: Int,
    val startTime: String,
    val endTime: String,
    val temperature: Double,
    @SerialName("temperatureUnit") val temperatureUnit: String? = null,
    val probabilityOfPrecipitation: ValueWrapper? = null,
    val shortForecast: String? = null,
    val icon: String? = null,
)

@Serializable
private data class DailyForecast(val properties: DailyProperties)

@Serializable
private data class DailyProperties(val periods: List<DailyPeriod>)

@Serializable
private data class DailyPeriod(
    val number: Int,
    val name: String? = null,
    val startTime: String,
    val endTime: String,
    val isDaytime: Boolean,
    val temperature: Double,
    @SerialName("temperatureUnit") val temperatureUnit: String? = null,
    val shortForecast: String? = null,
    val icon: String? = null,
)

@Serializable
private data class ValueWrapper(val value: Int? = null)

@Serializable
private data class StationsResponse(val features: List<StationFeature> = emptyList())

@Serializable
private data class StationFeature(val properties: StationProperties)

@Serializable
private data class StationProperties(val stationIdentifier: String)

@Serializable
private data class ObservationResponse(val properties: ObservationProperties)

@Serializable
private data class ObservationProperties(
    val timestamp: String,
    val temperature: NullableDouble,
    val textDescription: String? = null,
    val icon: String? = null,
)

@Serializable
private data class NullableDouble(
    val value: Double? = null,
    val unitCode: String? = null,
)
