package com.example.weatherappproject.Home

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.weatherappproject.R
import com.example.weatherappproject.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private var isUserSearch = false

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        fusedLocation = LocationServices.getFusedLocationProviderClient(this)


        tts = TextToSpeech(this, this, "com.google.android.tts")

        createNotificationChannel()

        getCurrentLocationWeather()

        setupSearch()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.ENGLISH)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                ttsReady = true
            }
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String?): Boolean {
                if (!text.isNullOrBlank()) {
                    isUserSearch = true
                    fetchWeatherData(text.trim())
                }
                return true
            }
            override fun onQueryTextChange(newText: String?) = true
        })
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationWeather() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocation.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                fetchWeatherByLocation(location.latitude, location.longitude)
            } else {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchWeatherByLocation(lat: Double, lon: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)

        val response = retrofit.getWeatherByLocation(
            lat, lon,
            "6ae8774ff62e8b9ded3e26e589bbe7c6",
            "metric"
        )

        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, res: Response<WeatherApp>) {
                if (res.isSuccessful && res.body() != null) {
                    val city = res.body()!!.name
                    fetchWeatherData(city)
                }
            }
            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {}
        })
    }

    private fun fetchWeatherData(city: String) {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(
            city,
            "6ae8774ff62e8b9ded3e26e589bbe7c6",
            "metric"
        )

        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, res: Response<WeatherApp>) {

                if (res.isSuccessful && res.body() != null) {

                    val body = res.body()!!
                    val temp = body.main.temp.toString()
                    val condition = body.weather.firstOrNull()?.main ?: "Clear"

                    binding.temp.text = "$temp °C"
                    binding.cityName.text = city
                    binding.weather.text = condition
                    binding.humidity.text = "${body.main.humidity}%"
                    binding.windSpeed.text = "${body.wind.speed} m/s"
                    binding.maxTemp.text = "Max: ${body.main.temp_max}°C"
                    binding.minTemp.text = "Min: ${body.main.temp_min}°C"
                    binding.sea.text = "${body.main.pressure} hPa"

                    binding.sunRise.text = time(body.sys.sunrise.toLong())
                    binding.sunset.text = time(body.sys.sunset.toLong())
                    binding.day.text = dayName(System.currentTimeMillis())
                    binding.date.text = date()

                    changeBackground(condition)

                    showWeatherNotification(body.main.temp, condition)

                    if (isUserSearch) {
                        uploadToFirebase(city, temp)
                        isUserSearch = false
                    }

                } else {
                    Toast.makeText(this@MainActivity, "City not found!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uploadToFirebase(city: String, temp: String) {
        val db = FirebaseDatabase.getInstance().getReference("WeatherData")
        val day = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
        val data = mapOf(
            "search_title" to city,
            "temperature" to "$temp°C",
            "day_name" to day
        )
        db.push().setValue(data)
    }

    private fun changeBackground(condition: String) {
        when (condition) {
            "Clear", "Sunny" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Clouds", "Mist", "Fog", "Haze" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Rain", "Drizzle", "Showers" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                "weather_channel",
                "Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun showWeatherNotification(temp: Double, condition: String) {
        val title: String
        val message: String
        val voice: String

        when {
            temp <= 15 -> {
                title = "❄️ Cold Weather"
                message = "It's cold outside!"
                voice = "The weather is cold today, wear warm clothes."
            }
            temp >= 30 -> {
                title = "☀️ Hot Weather"
                message = "It's too hot! Drink water."
                voice = "The weather is hot today, drink plenty of water."
            }
            else -> {
                title = "🌤 Pleasant Day"
                message = "The weather is good today."
                voice = "The weather is pleasant today."
            }
        }

        val notification = NotificationCompat.Builder(this, "weather_channel")
            .setSmallIcon(R.drawable.sun)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), notification)
        }

        Handler(mainLooper).postDelayed({
            speakOut(voice)
        }, 2000)
    }

    private fun speakOut(text: String) {
        if (ttsReady) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun date(): String =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

    private fun time(t: Long): String =
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(t * 1000))

    private fun dayName(t: Long): String =
        SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(t))

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
