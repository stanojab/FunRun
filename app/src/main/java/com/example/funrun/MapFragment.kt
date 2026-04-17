package com.example.funrun

import android.content.pm.PackageManager
import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.runlibrary.Run
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback, SensorEventListener {

    private lateinit var googleMap: GoogleMap
    private lateinit var startButton: Button
    private lateinit var tvPace: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvDistance: TextView

    private var isRunning = false
    private var startTime = 0L
    private var totalSteps = 0
    private var initialStepCount = 0
    private var isInitialStepSet = false
    private var timer: CountDownTimer? = null

    private val locationClient by lazy { LocationServices.getFusedLocationProviderClient(requireContext()) }
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    // Average stride length in km (0.78 metres per step)
    private val STRIDE_LENGTH_KM = 0.00078

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) enableLocationAndMoveCamera()
        }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startButton = view.findViewById(R.id.startRunButton)
        tvPace = view.findViewById(R.id.tvPace)
        tvDuration = view.findViewById(R.id.tvDuration)
        tvDistance = view.findViewById(R.id.tvDistance)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        startButton.setOnClickListener {
            if (isRunning) stopRun() else startRun()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        checkAndRequestLocationPermission()
    }

    private fun checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableLocationAndMoveCamera()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun enableLocationAndMoveCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            locationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
        }
    }

    private fun startRun() {
        isRunning = true
        startButton.text = "STOP RUN"
        startButton.setBackgroundResource(R.drawable.bg_stop_button)
        startTime = System.currentTimeMillis()
        isInitialStepSet = false
        totalSteps = 0

        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)

        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val elapsed = System.currentTimeMillis() - startTime
                val seconds = (elapsed / 1000) % 60
                val minutes = (elapsed / 60000) % 60
                val hours = elapsed / 3600000
                tvDuration.text = if (hours > 0)
                    "%02d:%02d:%02d".format(hours, minutes, seconds)
                else
                    "%02d:%02d".format(minutes, seconds)
                updateStats()
            }
            override fun onFinish() {}
        }.start()
    }

    private fun stopRun() {
        isRunning = false
        startButton.text = "START RUN"
        startButton.setBackgroundResource(R.drawable.bg_start_button)
        timer?.cancel()
        sensorManager.unregisterListener(this)

        val elapsedMs = System.currentTimeMillis() - startTime
        val distanceKm = totalSteps * STRIDE_LENGTH_KM
        val elapsedHours = elapsedMs / 3600000.0
        // Pace = km / hours = km/h
        val pace = if (elapsedHours > 0) distanceKm / elapsedHours else 0.0

        val run = Run(
            pace = pace,
            duration = elapsedMs,
            distance = distanceKm,
            timestamp = System.currentTimeMillis()
        )
        (requireActivity().application as MyApplication).addRun(run)

        // Reset display
        tvDuration.text = "00:00"
        tvDistance.text = "0.00 km"
        tvPace.text = "0.00"
    }

    private fun updateStats() {
        val elapsedMs = System.currentTimeMillis() - startTime
        val distanceKm = totalSteps * STRIDE_LENGTH_KM
        val elapsedHours = elapsedMs / 3600000.0
        val pace = if (elapsedHours > 0) distanceKm / elapsedHours else 0.0

        tvDistance.text = "%.2f km".format(distanceKm)
        tvPace.text = "%.2f".format(pace)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val stepCount = event.values[0].toInt()
            if (!isInitialStepSet) {
                initialStepCount = stepCount
                isInitialStepSet = true
            }
            totalSteps = stepCount - initialStepCount
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        if (isRunning) sensorManager.unregisterListener(this)
    }
}