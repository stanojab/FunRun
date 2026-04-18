package com.example.funrun

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.runlibrary.Run
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    // ─── UI ───────────────────────────────────────────────────────────────────
    private lateinit var googleMap: GoogleMap
    private lateinit var startButton: Button
    private lateinit var tvPace: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvDistance: TextView

    // ─── State ────────────────────────────────────────────────────────────────
    private var isRunning = false
    private var startTime = 0L
    private var totalDistanceMeters = 0f
    private var lastLocation: Location? = null
    private val routePoints = mutableListOf<LatLng>()

    // ─── Timer ────────────────────────────────────────────────────────────────
    private var timer: CountDownTimer? = null

    // ─── GPS ──────────────────────────────────────────────────────────────────
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        2000L           // update every 2 seconds
    ).apply {
        setMinUpdateIntervalMillis(1000L)       // no faster than 1 s
        setMinUpdateDistanceMeters(2f)          // only if moved ≥ 2 m
        setWaitForAccurateLocation(false)
    }.build()

    // ─── Permissions ──────────────────────────────────────────────────────────
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) enableMyLocation()
        }

    // ─────────────────────────────────────────────────────────────────────────
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startButton = view.findViewById(R.id.startRunButton)
        tvPace     = view.findViewById(R.id.tvPace)
        tvDuration = view.findViewById(R.id.tvDuration)
        tvDistance = view.findViewById(R.id.tvDistance)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Build the location callback once
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { onNewLocation(it) }
            }
        }

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        startButton.setOnClickListener {
            if (isRunning) stopRun() else startRun()
        }
    }

    // ─── Map ready ────────────────────────────────────────────────────────────
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        checkPermissionAndInit()
    }

    private fun checkPermissionAndInit() {
        if (hasLocationPermission()) {
            enableMyLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun hasLocationPermission() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun enableMyLocation() {
        if (!hasLocationPermission()) return
        googleMap.isMyLocationEnabled = true

        // Centre map on last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            loc?.let {
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 16f)
                )
            }
        }
    }

    // ─── Run control ──────────────────────────────────────────────────────────
    private fun startRun() {
        if (!hasLocationPermission()) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        isRunning           = true
        startTime           = System.currentTimeMillis()
        totalDistanceMeters = 0f
        lastLocation        = null
        routePoints.clear()

        startButton.text = "STOP RUN"
        startButton.setBackgroundResource(R.drawable.bg_stop_button)

        startLocationUpdates()
        startTimer()
    }

    private fun stopRun() {
        isRunning = false
        startButton.text = "START RUN"
        startButton.setBackgroundResource(R.drawable.bg_start_button)

        stopLocationUpdates()
        timer?.cancel()

        saveRun()

        // Reset display
        tvDuration.text = "00:00"
        tvDistance.text = "0.00 km"
        tvPace.text     = "0.00"
        Toast.makeText(requireContext(), "Run added!", Toast.LENGTH_SHORT).show()
    }

    // ─── Location updates ─────────────────────────────────────────────────────
    private fun startLocationUpdates() {
        if (!hasLocationPermission()) return
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Called on every GPS fix while running
    private fun onNewLocation(location: Location) {
        // Ignore inaccurate fixes (> 20 m accuracy)
        if (location.accuracy > 20f) return

        val newPoint = LatLng(location.latitude, location.longitude)

        // Accumulate distance
        lastLocation?.let { prev ->
            val delta = prev.distanceTo(location)    // metres
            if (delta > 1f) {                        // ignore GPS jitter < 1 m
                totalDistanceMeters += delta
            }
        }
        lastLocation = location

        // Draw route
        routePoints.add(newPoint)
        redrawRoute()

        // Follow the runner
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPoint, 17f))

        updateStatsDisplay()
    }

    // ─── Route drawing ────────────────────────────────────────────────────────
    private fun redrawRoute() {
        if (routePoints.size < 2) return
        googleMap.addPolyline(
            PolylineOptions()
                .addAll(routePoints)
                .color(Color.parseColor("#B8FF00"))
                .width(14f)
                .geodesic(true)
        )
    }

    // ─── Timer ────────────────────────────────────────────────────────────────
    private fun startTimer() {
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateStatsDisplay()
            }
            override fun onFinish() {}
        }.start()
    }

    // ─── Stats display ────────────────────────────────────────────────────────
    private fun updateStatsDisplay() {
        val elapsedMs  = System.currentTimeMillis() - startTime
        val distanceKm = totalDistanceMeters / 1000.0

        // Duration
        val totalSeconds = elapsedMs / 1000
        val hours   = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        tvDuration.text = if (hours > 0)
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        else
            "%02d:%02d".format(minutes, seconds)

        // Distance
        tvDistance.text = "%.2f km".format(distanceKm)

        // Pace: min/km  (elapsedMinutes / km)
        val elapsedMinutes = elapsedMs / 60000.0
        val pace = if (distanceKm > 0.01) elapsedMinutes / distanceKm else 0.0
        tvPace.text = "%.2f".format(pace)
    }

    // ─── Save run ─────────────────────────────────────────────────────────────
    private fun saveRun() {
        val elapsedMs  = System.currentTimeMillis() - startTime
        val distanceKm = totalDistanceMeters / 1000.0
        val elapsedMinutes = elapsedMs / 60000.0
        val pace = if (distanceKm > 0.01) elapsedMinutes / distanceKm else 0.0

        val run = Run(
            pace      = pace,
            duration  = elapsedMs,
            distance  = distanceKm,
            timestamp = System.currentTimeMillis()
        )
        (requireActivity().application as MyApplication).addRun(run)
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────
    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        if (isRunning) stopLocationUpdates()
    }
}