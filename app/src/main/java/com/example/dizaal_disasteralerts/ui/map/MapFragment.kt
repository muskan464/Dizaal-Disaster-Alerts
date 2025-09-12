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
import com.example.dizaal_disasteralerts.ui.map.disasters.FloodManager
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
    private lateinit var viewModel: MapViewModel

    private val LOCATION_PERMISSION_REQUEST_CODE = 101
    private val LOCATION_SETTINGS_REQUEST_CODE = 102

    // Fused location provider
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Callback for fresh location updates
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            handleLocation(location)
            fusedLocationClient.removeLocationUpdates(this) // stop updates after one-shot
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentMapBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        viewModel = ViewModelProvider(this)[MapViewModel::class.java]

        observeFloodData()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchLocation(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    private fun observeFloodData() {
        viewModel.floodData.observe(viewLifecycleOwner) { data ->
            data?.let {
                val lat = it.latitude ?: return@let
                val lon = it.longitude ?: return@let
                val discharge = it.daily?.riverDischarge?.firstOrNull() ?: 0.0
                val maxDischarge = it.daily?.riverDischargeMax?.firstOrNull() ?: discharge
                floodManager.showFlood(lat, lon, discharge, maxDischarge)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.uiSettings?.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
        }
        floodManager = FloodManager(map!!)
        enableMyLocation()

        val mapView = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).view
        mapView?.findViewById<View>(Integer.parseInt("2"))?.let { locationButton ->
            val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)

            layoutParams.setMargins(40, 0, 0, 200)
            locationButton.layoutParams = layoutParams

        }
    }

    private fun enableMyLocation() {
        context?.let { ctx ->
            if (ActivityCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                map?.isMyLocationEnabled = true
                getDeviceLocation()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        handleLocation(location)
                    } else {
                        requestNewLocationData()
                    }
                }
                .addOnFailureListener { requestNewLocationData() }
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
                        LOCATION_SETTINGS_REQUEST_CODE
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Toast.makeText(requireContext(), "Unable to open location settings", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Location settings are inadequate", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestNewLocationData() {
        try {
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 1000
                fastestInterval = 500
                numUpdates = 1
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Location permission missing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleLocation(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f))
        viewModel.fetchFloodData(location.latitude, location.longitude)
    }

    private fun searchLocation(locationName: String) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(locationName, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)

                map?.clear()
                map?.addMarker(MarkerOptions().position(latLng).title(locationName))
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))

                viewModel.fetchFloodData(address.latitude, address.longitude)
            } else {
                Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_SETTINGS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // User enabled location → fetch location again
                getDeviceLocation()
            } else {
                Toast.makeText(requireContext(), "Location is required for full functionality", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    map?.isMyLocationEnabled = true
                    getDeviceLocation()
                }
            } else {
                Toast.makeText(requireContext(), "Location permission is required to show your current location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) { }
        floodManager.clearFloodPolygons()
        _binding = null
    }
}
