package com.sparse.launcher

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class WeatherData(val temp: Int, val condition: String, val isFahrenheit: Boolean)

object WeatherRepository {

    private var cachedData: WeatherData? = null
    private var cacheTime = 0L
    private var cachedLat = Double.NaN
    private var cachedLon = Double.NaN

    private const val CACHE_MS = 30 * 60 * 1000L

    fun fetch(lat: Double, lon: Double, useFahrenheit: Boolean): WeatherData {
        val now = System.currentTimeMillis()
        val cached = cachedData
        if (cached != null
            && cached.isFahrenheit == useFahrenheit
            && now - cacheTime < CACHE_MS
            && cachedLat == lat && cachedLon == lon
        ) return cached

        val unit = if (useFahrenheit) "fahrenheit" else "celsius"
        val urlStr = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=$lat&longitude=$lon" +
            "&current=temperature_2m,weather_code" +
            "&temperature_unit=$unit&forecast_days=1"

        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.connectTimeout = 6000
        conn.readTimeout = 6000
        return try {
            val json = conn.inputStream.bufferedReader().readText()
            val current = JSONObject(json).getJSONObject("current")
            val temp = current.getDouble("temperature_2m").toInt()
            val code = current.getInt("weather_code")
            WeatherData(temp, wmoCondition(code), useFahrenheit).also {
                cachedData = it
                cacheTime = now
                cachedLat = lat
                cachedLon = lon
            }
        } finally {
            conn.disconnect()
        }
    }

    fun invalidate() {
        cachedData = null
    }

    private fun wmoCondition(code: Int): String = when (code) {
        0 -> "clear"
        1 -> "mostly clear"
        2 -> "partly cloudy"
        3 -> "overcast"
        45, 48 -> "foggy"
        51, 53, 55 -> "drizzle"
        56, 57 -> "freezing drizzle"
        61, 63, 65 -> "rain"
        66, 67 -> "freezing rain"
        71, 73, 75 -> "snow"
        77 -> "snow grains"
        80, 81, 82 -> "showers"
        85, 86 -> "snow showers"
        95 -> "thunderstorm"
        96, 99 -> "thunderstorm w/ hail"
        else -> "—"
    }
}
