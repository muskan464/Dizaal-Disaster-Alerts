package com.example.dizaal_disasteralerts.ui.home

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dizaal_disasteralerts.databinding.FragmentHomeBinding
import com.example.dizaal_disasteralerts.ui.details.AlertDetailsFragment
import com.example.dizaal_disasteralerts.viewmodel.FloodViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AlertAdapter
    private val floodViewModel: FloodViewModel by activityViewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private val LOCATION_PERMISSION_REQUEST_CODE = 201
    private val LOCATION_SETTINGS_REQUEST_CODE = 202

    /** 🔹 Static demo items for other disaster types */
    private val staticItems = listOf(
        AlertItem("Earthquake Alert", "Magnitude 6.5 • Issued 10 min ago", "—", Severity.EARTHQUAKES),
        AlertItem("Cyclone Alert", "Category 3 • Coastline", "Approaching", Severity.CYCLONES),
        AlertItem("Heatwave Alert", "Northern States • 45°C+", "Valid for next 3 days", Severity.HEATWAVES),
        AlertItem("Cold Wave Alert", "Northern Highlands • Sub-zero", "Valid until Monday", Severity.COLDWAVES),
        AlertItem("Tsunami Warning", "Pacific Coast • Wave Height 3m+", "Approaching Shoreline", Severity.TSUNAMIS)
    )

    private var currentList: List<AlertItem> = listOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 2000
            fastestInterval = 1000
            numUpdates = 1
        }

        setupRecyclerView()
        setupSearch()
        observeFloodData()

        // Fetch live flood data based on current location
        checkLocationSettings()

        // Handle details screen navigation
        childFragmentManager.addOnBackStackChangedListener {
            val showingDetails = childFragmentManager.backStackEntryCount > 0
            binding.homeContentLayout.visibility = if (showingDetails) View.GONE else View.VISIBLE
            binding.detailsContainer.visibility = if (showingDetails) View.VISIBLE else View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = AlertAdapter(emptyList()) { item -> openAlertDetails(item) }
        binding.rvAlerts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlerts.adapter = adapter

        currentList = staticItems
        adapter.submit(currentList)
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.apply { filterList(query) }
            override fun onQueryTextChange(newText: String?) = true.apply { filterList(newText) }
        })
    }

    private fun observeFloodData() {
        floodViewModel.floodAlerts.observe(viewLifecycleOwner) { alerts ->
            currentList = if (!alerts.isNullOrEmpty()) {
                alerts + staticItems
            } else {
                staticItems
            }
            adapter.submit(currentList)
        }

        floodViewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let {
                Toast.makeText(requireContext(), "Flood API error: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** 🔹 Step 1: Check if Location is enabled, else show popup */
    private fun checkLocationSettings() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { getDeviceLocation() }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(requireActivity(), LOCATION_SETTINGS_REQUEST_CODE)
                } catch (e: IntentSender.SendIntentException) {
                    Toast.makeText(requireContext(), "Unable to open location settings", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Location settings are inadequate", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 🔹 Step 2: Get device location */
    private fun getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
            if (loc != null) {
                floodViewModel.fetchFloodData(loc.latitude, loc.longitude)
            } else {
                requestNewLocationData()
            }
        }.addOnFailureListener { requestNewLocationData() }
    }

    private fun requestNewLocationData() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    floodViewModel.fetchFloodData(it.latitude, it.longitude)
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }, Looper.getMainLooper())
    }

    private fun filterList(query: String?) {
        val q = query?.trim()?.lowercase() ?: ""
        val filtered = currentList.filter {
            it.title.lowercase().contains(q) ||
                    it.subtitle.lowercase().contains(q) ||
                    it.time.lowercase().contains(q) ||
                    (it.payload?.lowercase()?.contains(q) ?: false)
        }
        adapter.submit(filtered)
    }

    private fun openAlertDetails(item: AlertItem) {
        binding.detailsContainer.visibility = View.VISIBLE
        binding.homeContentLayout.visibility = View.GONE

        childFragmentManager.beginTransaction()
            .replace(binding.detailsContainer.id, AlertDetailsFragment.newInstance(item))
            .addToBackStack(null)
            .commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationSettings()
        } else {
            Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show()
            // fallback: Delhi if no permission
            floodViewModel.fetchFloodData(28.6139, 77.2090)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
