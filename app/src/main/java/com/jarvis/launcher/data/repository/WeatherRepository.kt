package com.jarvis.launcher.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class WeatherData(
    val tempC: Int,
    val tempF: Int,
    val condition: String,
    val location: String
)

@Singleton
class WeatherRepository @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private var cached: WeatherData? = null
    private var lastFetchTime = 0L

    suspend fun getWeather(): WeatherData? = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (cached != null && now - lastFetchTime < CACHE_DURATION_MS) {
            return@withContext cached
        }

        try {
            val request = Request.Builder()
                .url("https://wttr.in/?format=j1")
                .addHeader("User-Agent", "JarvisLauncher/1.0")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext cached

            val json = JSONObject(body)
            val current = json.getJSONArray("current_condition").getJSONObject(0)
            val nearest = json.getJSONArray("nearest_area").getJSONObject(0)

            val tempC = current.getString("temp_C").toIntOrNull() ?: 0
            val tempF = current.getString("temp_F").toIntOrNull() ?: 0
            val condition = current.getJSONArray("weatherDesc")
                .getJSONObject(0).getString("value")
            val city = nearest.getJSONArray("areaName")
                .getJSONObject(0).getString("value")

            val data = WeatherData(tempC, tempF, condition.uppercase(), city.uppercase())
            cached = data
            lastFetchTime = now
            data
        } catch (e: Exception) {
            cached
        }
    }

    companion object {
        private const val CACHE_DURATION_MS = 15 * 60 * 1000L
    }
}
