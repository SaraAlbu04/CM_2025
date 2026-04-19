package com.example.ghostmappers.services


import android.content.Context

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class WeatherService(context: Context) {

    private val client = OkHttpClient()
    private val locationService = LocationService(context)


    suspend fun getCurrentTemperature(): Double = withContext(Dispatchers.IO) {
        val location = locationService.getCurrentLocation()
            ?: throw Exception("Failed to get current location")
        val url =
            "https://api.open-meteo.com/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}&current_weather=true"

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw Exception("Failed to fetch weather data")
        }

        val json = JSONObject(response.body!!.string())
        val currentWeather = json.getJSONObject("current_weather")
        return@withContext currentWeather.getDouble("temperature")
    }

}
