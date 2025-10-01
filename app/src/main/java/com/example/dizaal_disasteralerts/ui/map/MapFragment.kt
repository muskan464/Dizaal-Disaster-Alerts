package com.example.dizaal_disasteralerts.ui.map

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.dizaal_disasteralerts.R
import com.example.dizaal_disasteralerts.databinding.FragmentMapBinding
import com.example.dizaal_disasteralerts.ui.map.disasters.EarthquakeManager
import com.example.dizaal_disasteralerts.ui.map.disasters.FloodManager
import com.example.dizaal_disasteralerts.viewmodel.EarthquakeViewModel
import com.example.dizaal_disasteralerts.viewmodel.FloodViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private var map: GoogleMap? = null
    private lateinit var floodManager: FloodManager
    private lateinit var earthquakeManager: EarthquakeManager
    private lateinit var floodViewModel: FloodViewModel
    private lateinit var earthquakeViewModel: EarthquakeViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 101
    private val LOCATION_SETTINGS_REQUEST_CODE = 102

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { handleLocation(it) }
            fusedLocationClient.removeLocationUpdates(this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        floodViewModel = ViewModelProvider(this)[FloodViewModel::class.java]
        earthquakeViewModel = ViewModelProvider(this)[EarthquakeViewModel::class.java]

        setupObservers()
        setupSearchView()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isCompassEnabled = true
        }

        floodManager = FloodManager(map!!)
        earthquakeManager = EarthquakeManager(map!!)

        enableMyLocation()
        repositionLocationButton()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_SETTINGS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) getDeviceLocation()
            else showToast("Location is required for full functionality")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) enableMyLocation()
        else showToast("Location permission is required to show your current location")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try { fusedLocationClient.removeLocationUpdates(locationCallback) } catch (_: Exception) {}
        floodManager.clearFloodPolygons()
        earthquakeManager.clearEarthquakes()
        _binding = null
    }

    private fun setupObservers() {
        // 🔹 Flood data (list-based)
        floodViewModel.floodDataSingle.observe(viewLifecycleOwner) { data ->
            data?.let { floodResponse ->
                val lat = floodResponse.latitude ?: return@let
                val lon = floodResponse.longitude ?: return@let
                val discharge = floodResponse.daily?.riverDischarge?.firstOrNull() ?: 0.0
                val maxDischarge = floodResponse.daily?.riverDischargeMax?.firstOrNull() ?: discharge

                floodManager.clearFloodPolygons()
                floodManager.showFlood(lat, lon, discharge, maxDischarge)
            }
        }





        // 🔹 Earthquake data
        earthquakeViewModel.earthquakeData.observe(viewLifecycleOwner) { data ->
            data?.let { earthquakeManager.showEarthquakes(it) }
        }

        // 🔹 Error handling
        floodViewModel.error.observe(viewLifecycleOwner) { it?.let { showToast(it) } }
        earthquakeViewModel.error.observe(viewLifecycleOwner) { it?.let { showToast(it) } }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchLocation(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?) = false
        })
    }

    private fun repositionLocationButton() {
        val mapView = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).view
        val locationButton = mapView?.findViewById<View>(Integer.parseInt("2"))
        locationButton?.let {
            val params = it.layoutParams as RelativeLayout.LayoutParams
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
            params.setMargins(40, 0, 0, 200)
            it.layoutParams = params
        }
    }

    private fun enableMyLocation() {
        context?.let { ctx ->
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                map?.isMyLocationEnabled = true
                getDeviceLocation()
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        val locationRequest = LocationRequest.create().apply { priority = LocationRequest.PRIORITY_HIGH_ACCURACY }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc -> if (loc != null) handleLocation(loc) else requestNewLocationData() }
                .addOnFailureListener { requestNewLocationData() }
        }

        task.addOnFailureListener { ex ->
            if (ex is ResolvableApiException) {
                try { ex.startResolutionForResult(requireActivity(), LOCATION_SETTINGS_REQUEST_CODE) } catch (_: IntentSender.SendIntentException) { showToast("Unable to open location settings") }
            } else showToast("Location settings are inadequate")
        }
    }

    private fun requestNewLocationData() {
        try {
            val request = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 1000
                fastestInterval = 500
                numUpdates = 1
            }
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        } catch (_: SecurityException) { showToast("Location permission missing") }
    }

    private fun handleLocation(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 6f))

        floodViewModel.fetchFloodData(location.latitude, location.longitude)
        earthquakeViewModel.fetchEarthquakeData(location.latitude, location.longitude)
    }

    private fun searchLocation(query: String) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val results = geocoder.getFromLocationName(query, 1)
            if (!results.isNullOrEmpty()) {
                val address = results[0]
                val latLng = LatLng(address.latitude, address.longitude)

                map?.clear()
                map?.addMarker(MarkerOptions().position(latLng).title(query))
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 6f))

                floodViewModel.fetchFloodData(address.latitude, address.longitude)
                earthquakeViewModel.fetchEarthquakeData(address.latitude, address.longitude)
            } else showToast("Location not found")
        } catch (e: Exception) {
            showToast("Error: ${e.message}")
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
