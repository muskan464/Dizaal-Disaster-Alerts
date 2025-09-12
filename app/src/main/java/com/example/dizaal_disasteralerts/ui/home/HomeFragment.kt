package com.example.dizaal_disasteralerts.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dizaal_disasteralerts.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AlertAdapter
    private val allItems = listOf(
        AlertItem("Flood Warning", "Riverside County", "Active until 6 PM", Severity.FLOODS),
        AlertItem("Earthquake Alert", "Magnitude 6.5 • Issued 10 min ago", "—", Severity.EARTHQUAKES),
        AlertItem("Cyclone Alert", "Category 3 • Coastline", "Approaching", Severity.CYCLONES),
        AlertItem("Heatwave Alert", "Northern States • 45°C+", "Valid for next 3 days", Severity.HEATWAVES),
        AlertItem("Cold Wave Alert", "Northern Highlands • Sub-zero", "Valid until Monday", Severity.COLDWAVES),
        AlertItem("Tsunami Warning", "Pacific Coast • Wave Height 3m+", "Approaching Shoreline", Severity.TSUNAMIS)

    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = AlertAdapter(allItems)
        binding.rvAlerts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlerts.adapter = adapter

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterList(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(query: String?) {
        val q = query?.trim()?.lowercase() ?: ""
        val filtered = allItems.filter {
            it.title.lowercase().contains(q) ||
                    it.subtitle.lowercase().contains(q)
        }
        adapter.submit(filtered)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}