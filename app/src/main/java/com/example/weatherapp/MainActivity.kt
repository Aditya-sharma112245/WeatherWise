package com.example.weatherapp

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView

import androidx.appcompat.widget.SearchView;

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.*

import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var temp: TextView
    private lateinit var description: TextView
    private lateinit var humidityText: TextView
    private lateinit var windSpeedText: TextView
    private lateinit var cityname: TextView
    private lateinit var time: TextView
    private lateinit var image: ImageView
    private lateinit var search: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        temp = findViewById(R.id.temp)
        description = findViewById(R.id.description)
        humidityText = findViewById(R.id.humidityText)
        windSpeedText = findViewById(R.id.windSpeedText)
        cityname = findViewById(R.id.cityname)
        time = findViewById(R.id.timeText)
        image = findViewById(R.id.imageView)
        search = findViewById(R.id.search)

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    getData(query)
                } else {
                    getData("London")
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        // Fetch weather data for default location (London)
        getData("London")
    }

    private fun getData(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = getWeatherData(query)
                withContext(Dispatchers.Main) {
                    parseWeatherData(response)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching weather data: ${e.message}")
            }
        }
    }

    private suspend fun getWeatherData(query: String): JSONObject {
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$query&appid=0cb7be620c01bbf0337dec7517d43cce"
        return withContext(Dispatchers.IO) {
            val response = URL(url).readText()
            JSONObject(response)
        }
    }

    private fun parseWeatherData(response: JSONObject) {
        val mainObject = response.getJSONObject("main")
        val temperatureKelvin = mainObject.getDouble("temp")
        val temperatureCelsius = temperatureKelvin - 273.15

        val weatherArray = response.getJSONArray("weather")
        val weatherObject = weatherArray.getJSONObject(0)
        val weatherDescription = weatherObject.getString("description")

        val cityName = response.getString("name")

        val windObject = response.getJSONObject("wind")
        val windSpeed = windObject.getDouble("speed")

        val sysObject = response.getJSONObject("sys")
        val sunriseTimestamp = sysObject.getLong("sunrise") * 1000
        val sunsetTimestamp = sysObject.getLong("sunset") * 1000

        val weatherIcon = weatherObject.getString("icon")
        val weatherIconUrl = "https://openweathermap.org/img/wn/$weatherIcon.png"

        val sunriseTime = getTimeString(sunriseTimestamp)
        val sunsetTime = getTimeString(sunsetTimestamp)

        temp.text = "${temperatureCelsius.toInt()}°C"
        description.text = weatherDescription
        humidityText.text = "Humidity: ${mainObject.getInt("humidity")}%"
        windSpeedText.text = "Wind Speed: $windSpeed m/s"
        cityname.text = cityName
        time.text = "Sunrise: $sunriseTime\nSunset: $sunsetTime"

        Glide.with(this@MainActivity)
            .load(weatherIconUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e(TAG, "Glide Error: ${e?.message}")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .override(715, 713)
            .centerCrop()
            .into(image)
    }

    private fun getTimeString(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val sdf = java.text.SimpleDateFormat("hh:mm a")
        return sdf.format(date)
    }
}