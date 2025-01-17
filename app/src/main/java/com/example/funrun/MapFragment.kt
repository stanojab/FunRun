package com.example.funrun

import android.content.pm.PackageManager
import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.runlibrary.Run
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapFragment : Fragment(R.layout.fragment_map),  OnMapReadyCallback, SensorEventListener {

    private lateinit var googleMap: GoogleMap
    private lateinit var startButton: Button
    private lateinit var tvPace: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvDistance: TextView

    private var isRunning = false
    private var startTime = System.currentTimeMillis()
    private var totalSteps = 0
    private var initialStepCount = 0
    private var isInitialStepSet = false

    private var timer: CountDownTimer? = null

    private val locationClient by lazy { LocationServices.getFusedLocationProviderClient(requireContext()) }
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                enableLocationAndMoveCamera()
            }
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
            if (isRunning) {
                stopRun()
            } else {
                startRun()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        checkAndRequestLocationPermission()
    }

    private fun checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableLocationAndMoveCamera()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun enableLocationAndMoveCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
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
        startButton.text = "Stop Run"
        startTime = System.currentTimeMillis()

        isInitialStepSet = false
        totalSteps = 0

        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)

        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val elapsedTime = System.currentTimeMillis() - startTime
                val seconds = (elapsedTime / 1000) % 60
                val minutes = (elapsedTime / (1000 * 60)) % 60

                val durationText = String.format("%02d:%02d", minutes, seconds)
                tvDuration.text = "Duration: $durationText"

                updatePace()
            }

            override fun onFinish() {}
        }.start()
    }

    private fun stopRun() {
        isRunning = false
        startButton.text = "Start Run"
        timer?.cancel()

        sensorManager.unregisterListener(this)

        val elapsedTime = System.currentTimeMillis() - startTime
        val pace = if (elapsedTime > 0) (totalSteps / 1000.0) / (elapsedTime / 1000.0 / 3600.0) else 0.0

        val run = Run(
            pace = pace,
            duration = elapsedTime,
            distance = totalSteps / 1.0,
            timestamp = System.currentTimeMillis()
        )

        (requireActivity().application as MyApplication).addRun(run)
    }

    private fun updatePace() {
        val elapsedTime = System.currentTimeMillis() - startTime
        val pace = if (elapsedTime > 0) (totalSteps / 1000.0) / (elapsedTime / 1000.0 / 3600.0) else 0.0
        tvPace.text = "Pace: ${"%.2f".format(pace)} km/h"
        tvDistance.text = "Distance: ${"%.2f".format(totalSteps / 1000.0)} km"
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
}


