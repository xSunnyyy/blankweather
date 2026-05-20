package com.blankweather.app.data

enum class WeatherKind {
    CLEAR, MAINLY_CLEAR, CLOUDY, FOG, DRIZZLE, RAIN, SNOW, SHOWERS, THUNDERSTORM
}

object NwsIcon {
    /**
     * NWS icon URLs look like:
     *   https://api.weather.gov/icons/land/day/skc?size=medium
     *   https://api.weather.gov/icons/land/night/rain,40?size=medium
     *   https://api.weather.gov/icons/land/day/tsra_sct?size=medium
     *
     * We pull out the path segment after day/night, strip the comma and
     * any probability suffix, and map it to a [WeatherKind].
     */
    fun kindFromIconUrl(iconUrl: String?): WeatherKind {
        if (iconUrl.isNullOrBlank()) return WeatherKind.CLOUDY
        val path = iconUrl.substringBefore('?')
        val segments = path.trimEnd('/').split('/')
        val raw = segments.getOrNull(segments.lastIndex) ?: return WeatherKind.CLOUDY
        val code = raw.substringBefore(',').lowercase().removePrefix("wind_")
        return when (code) {
            "skc", "few", "hot", "cold" -> WeatherKind.CLEAR
            "sct" -> WeatherKind.MAINLY_CLEAR
            "bkn", "ovc" -> WeatherKind.CLOUDY
            "fog", "dust", "smoke", "haze" -> WeatherKind.FOG
            "rain_showers", "rain_showers_hi" -> WeatherKind.SHOWERS
            "rain" -> WeatherKind.RAIN
            "fzra", "rain_fzra", "snow_fzra" -> WeatherKind.DRIZZLE
            "snow", "rain_snow", "snow_sleet", "sleet", "blizzard" -> WeatherKind.SNOW
            "tsra", "tsra_sct", "tsra_hi", "tropical_storm", "hurricane", "tornado" ->
                WeatherKind.THUNDERSTORM
            else -> WeatherKind.CLOUDY
        }
    }
}
