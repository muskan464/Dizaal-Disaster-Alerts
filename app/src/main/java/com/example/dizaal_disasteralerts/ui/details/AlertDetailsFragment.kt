package com.example.dizaal_disasteralerts.ui.details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.dizaal_disasteralerts.R
import com.example.dizaal_disasteralerts.ui.home.AlertItem
import com.example.dizaal_disasteralerts.util.getCityNameFromCoordinates
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class AlertDetailsFragment : Fragment() {

    private var alertItem: AlertItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { alertItem = it.getParcelable(ARG_ALERT) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alert_details, container, false)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.title_home)
        toolbar.setNavigationIcon(R.drawable.outline_arrow_left_alt_24)
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        alertItem?.let { item ->
            val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
            val tvSeverity = view.findViewById<TextView>(R.id.tvSeverity)
            val tvRiskType = view.findViewById<TextView>(R.id.tvRiskType)
            val tvLocation = view.findViewById<TextView>(R.id.tvLocation)
            val tvIssuedTime = view.findViewById<TextView>(R.id.tvIssuedTime)
            val tvExpectedTime = view.findViewById<TextView>(R.id.tvExpectedTime)
            val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
            val tvAuthority = view.findViewById<TextView>(R.id.tvAuthority)
            val tvInstructions = view.findViewById<TextView>(R.id.tvInstructions)
            val tvShelterInfo = view.findViewById<TextView>(R.id.tvShelterInfo)

            tvTitle.text = item.title ?: "Disaster Alert"
            tvSeverity.text = "Severity: ${item.severity.name}"
            tvRiskType.text = "Risk: ${item.subtitle ?: "N/A"}"

            val riskLower = (item.subtitle ?: "").lowercase()
            val riskColor = when {
                "severe" in riskLower || "extreme" in riskLower -> android.R.color.holo_red_dark
                "high" in riskLower -> android.R.color.holo_red_light
                "moderate" in riskLower -> android.R.color.holo_orange_dark
                "low" in riskLower -> android.R.color.holo_green_dark
                "minimal" in riskLower -> android.R.color.holo_green_light
                else -> android.R.color.black
            }
            tvRiskType.setTextColor(resources.getColor(riskColor, requireActivity().theme))

            tvLocation.text = if (item.latitude != null && item.longitude != null) {
                "Location: ${getCityNameFromCoordinates(requireContext(), item.latitude, item.longitude)}"
            } else {
                "Location: ${item.location ?: "Unknown"}"
            }

            tvIssuedTime.text = "Issued: ${item.issuedTime ?: "N/A"}"
            tvExpectedTime.text = "Expected: ${item.expectedTime ?: "N/A"}"
            tvDescription.text = item.description ?: "No description available."
            tvAuthority.text = item.authority ?: "Unknown authority"
            tvInstructions.text = item.instructions ?: "No advisory available."
            tvShelterInfo.text = item.shelterInfo ?: "Not specified"

            view.findViewById<MaterialButton>(R.id.btnCopyInfo).setOnClickListener {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val allText = buildString {
                    appendLine(item.title)
                    appendLine(tvSeverity.text)
                    appendLine(tvRiskType.text)
                    appendLine(tvLocation.text)
                    appendLine(tvIssuedTime.text)
                    appendLine(tvExpectedTime.text)
                    appendLine("Description: ${item.description}")
                    appendLine("Authority: ${item.authority}")
                    appendLine("Instructions: ${item.instructions}")
                    appendLine("Shelter: ${item.shelterInfo}")
                }
                val clip = ClipData.newPlainText("Disaster Info", allText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "Disaster info copied", Toast.LENGTH_SHORT).show()
            }

            // Share
            view.findViewById<MaterialButton>(R.id.btnShare).setOnClickListener {
                val shareText = buildString {
                    appendLine(item.title)
                    appendLine(tvSeverity.text)
                    appendLine(tvRiskType.text)
                    appendLine(tvLocation.text)
                    appendLine(tvIssuedTime.text)
                    appendLine(tvExpectedTime.text)
                    appendLine("Description: ${item.description}")
                    appendLine("Authority: ${item.authority}")
                    appendLine("Instructions: ${item.instructions}")
                    appendLine("Shelter: ${item.shelterInfo}")
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                startActivity(Intent.createChooser(shareIntent, "Share Disaster Info"))
            }

            // Map
            view.findViewById<MaterialButton>(R.id.btnOpenMap).setOnClickListener {
                if (item.latitude != null && item.longitude != null) {
                    val mapIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("geo:${item.latitude},${item.longitude}?q=${item.latitude},${item.longitude}(${item.title})")
                    )
                    startActivity(mapIntent)
                } else {
                    Toast.makeText(requireContext(), "No location available", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }

    companion object {
        private const val ARG_ALERT = "arg_alert"

        @JvmStatic
        fun newInstance(alert: AlertItem) =
            AlertDetailsFragment().apply {
                arguments = Bundle().apply { putParcelable(ARG_ALERT, alert) }
            }
    }
}
