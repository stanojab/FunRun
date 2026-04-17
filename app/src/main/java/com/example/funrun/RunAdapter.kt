package com.example.funrun

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.runlibrary.Run
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RunAdapter(private val runs: List<Run>) : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDistance: TextView = view.findViewById(R.id.distanceTextData)
        val tvDuration: TextView = view.findViewById(R.id.durationTextData)
        val tvPace: TextView = view.findViewById(R.id.paceTextData)
        val tvTimestamp: TextView = view.findViewById(R.id.dateTextData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_run, parent, false)
        return RunViewHolder(view)
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = runs[position]
        // NOTE: No "Distance: " prefix here — the layout labels handle that via separate TextViews
        holder.tvDistance.text = "${"%.2f".format(run.distance)} km"
        holder.tvDuration.text = formatDuration(run.duration)
        holder.tvPace.text = "${"%.2f".format(run.pace)} min/km"
        holder.tvTimestamp.text = formatTimestamp(run.timestamp)
    }

    override fun getItemCount(): Int = runs.size

    private fun formatDuration(duration: Long): String {
        val seconds = (duration / 1000) % 60
        val minutes = (duration / (1000 * 60)) % 60
        val hours = (duration / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
        return format.format(date)
    }
}