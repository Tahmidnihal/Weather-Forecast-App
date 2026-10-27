package com.example.weatherappproject.History

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherappproject.Home.MainActivity
import com.example.weatherappproject.R
import com.example.weatherappproject.databinding.ActivityHistoryPageBinding
import com.google.firebase.database.*

class HistoryPage : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryPageBinding
    private lateinit var database: DatabaseReference
    private lateinit var weatherList: ArrayList<WeatherModel>
    private var loginFace = false
    private lateinit var sharedPreferences: SharedPreferences



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        val btnContinue = findViewById<ImageView>(R.id.back)
        val logout=findViewById<CardView>(R.id.logout);

       btnContinue.setOnClickListener {
         onBackPressed()
        }
        logout.setOnClickListener{
            val editor = sharedPreferences.edit()
            editor.putString("profileKey", null)
            editor.putBoolean("profilePermission",loginFace)
            editor.apply()
            val intent = Intent(this@HistoryPage,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        weatherList = ArrayList()
        database = FirebaseDatabase.getInstance().getReference("WeatherData")

        binding.recycler.layoutManager = LinearLayoutManager(this)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                weatherList.clear()
                for (data in snapshot.children) {
                    val model = data.getValue(WeatherModel::class.java)
                    model?.let { weatherList.add(it) }
                }
                binding.recycler.adapter = WeatherHistoryAdapter(weatherList.reversed())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistoryPage, "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
