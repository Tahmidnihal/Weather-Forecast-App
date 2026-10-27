package com.example.weatherappproject.History

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherappproject.databinding.HistoryviewBinding

class WeatherHistoryAdapter(private val list: List<WeatherModel>) :
    RecyclerView.Adapter<WeatherHistoryAdapter.WeatherViewHolder>() {

    inner class WeatherViewHolder(val binding: HistoryviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val binding = HistoryviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val item = list[position]
        holder.binding.tvSearchTitle.text = item.search_title
        holder.binding.tvTemperature.text = item.temperature+"°C"
        holder.binding.tvDayName.text = item.day_name
    }

    override fun getItemCount(): Int = list.size
}
