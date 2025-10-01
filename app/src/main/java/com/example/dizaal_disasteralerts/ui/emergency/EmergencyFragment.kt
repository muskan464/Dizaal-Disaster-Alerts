package com.example.dizaal_disasteralerts.ui.emergency

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.dizaal_disasteralerts.R

class EmergencyFragment : Fragment() {

    private val disasters = listOf(
        "Earthquakes", "Floods", "Storms",
        "Fires", "Cyclones", "Heatwaves",
        "Coldwaves", "Tsunamis"
    )

    private val safetyTipsMap = mapOf(
        "Earthquakes" to listOf(
            "Drop to your hands and knees to prevent being knocked over.",
            "Cover your head and neck under sturdy furniture (table, desk) to protect from falling debris.",
            "Hold on until the shaking stops and remain indoors—stay away from windows and exterior walls.",
            "After shaking stops, check for injuries and hazards like gas leaks or damaged wiring.",
            "If outdoors, move to an open area away from buildings, trees, and power lines."
        ),
        "Floods" to listOf(
            "Move to higher ground immediately if flooding is occurring in your area.",
            "Avoid walking, swimming, or driving through floodwaters—just 6 inches of moving water can knock you down.",
            "Disconnect electrical appliances and avoid contact with electrical equipment in water.",
            "Listen to official alerts and evacuation orders from local authorities.",
            "Keep emergency supplies ready: clean drinking water, food, flashlight, medicines, and important documents."
        ),
        "Storms" to listOf(
            "Stay indoors and away from windows, skylights, and glass doors during strong winds or lightning.",
            "Unplug electrical appliances to protect them from power surges caused by lightning.",
            "Secure loose outdoor objects (furniture, tools, decorations) to prevent them from becoming projectiles.",
            "Avoid using phones or plumbing systems during lightning strikes.",
            "Keep a battery-powered radio to stay updated on weather conditions and emergency instructions."
        ),
        "Fires" to listOf(
            "If you see fire indoors, stay low to avoid smoke inhalation—crawl if necessary.",
            "Use the back of your hand to check if doors are hot before opening them.",
            "If clothing catches fire: STOP, DROP, and ROLL to extinguish flames.",
            "Have a family evacuation plan with at least two escape routes from each room.",
            "Do not re-enter a burning building; call emergency services immediately once outside."
        ),
        "Cyclones" to listOf(
            "Stay indoors in the strongest part of the building, away from windows and glass doors.",
            "Stock up on drinking water, dry food, first-aid kits, and flashlights.",
            "Disconnect electrical appliances and secure gas cylinders.",
            "Avoid coastal areas, rivers, and low-lying regions; evacuate if ordered by authorities.",
            "After the cyclone passes, be cautious of damaged power lines, flooded areas, and weak structures."
        ),
        "Heatwaves" to listOf(
            "Drink plenty of water even if you don’t feel thirsty—avoid alcohol and caffeinated drinks.",
            "Stay indoors during the hottest hours (10 a.m. – 4 p.m.) and use fans or air-conditioning.",
            "Wear loose, light-colored clothing and a hat or umbrella when outdoors.",
            "Check regularly on elderly, children, and people with chronic illnesses as they are more vulnerable.",
            "Never leave children or pets in parked vehicles during hot weather."
        ),
        "Coldwaves" to listOf(
            "Stay indoors and keep rooms heated—block drafts from windows and doors.",
            "Wear several layers of loose, warm clothing and cover head, hands, and feet.",
            "Keep emergency supplies: blankets, food, hot water, and medicines.",
            "Be alert for signs of hypothermia (shivering, slurred speech, confusion) and frostbite.",
            "Avoid driving unless absolutely necessary—roads may be icy and dangerous."
        ),
        "Tsunamis" to listOf(
            "If you feel strong earthquake tremors near coastal areas, move to higher ground immediately.",
            "Follow official tsunami evacuation routes and do not wait for visual confirmation of waves.",
            "Stay away from beaches, rivers, and estuaries until authorities declare it safe.",
            "Carry emergency kits with food, water, first-aid supplies, and important documents.",
            "Do not return to coastal areas until authorities confirm the danger is over—tsunamis often come in multiple waves."
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_emergency, container, false)

        // Emergency Buttons
        v.findViewById<View>(R.id.btnPolice).setOnClickListener { dial("100") }
        v.findViewById<View>(R.id.btnAmbulance).setOnClickListener { dial("102") }
        v.findViewById<View>(R.id.btnFire).setOnClickListener { dial("101") }

        val layoutDisasters = v.findViewById<LinearLayout>(R.id.layoutDisasters)
        val layoutSafetyTips = v.findViewById<LinearLayout>(R.id.layoutSafetyTips)

        // Dynamically create disaster buttons
        disasters.forEach { disaster ->
            val chip = TextView(requireContext()).apply {
                text = disaster
                setPadding(24, 16, 24, 16)
                setBackgroundResource(R.drawable.bg_chip)
                setTextColor(resources.getColor(R.color.text_primary, null))
                setOnClickListener {
                    showSafetyTips(disaster, layoutSafetyTips)
                }
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = 16 }
            layoutDisasters.addView(chip, params)
        }

        // Show default tips (first disaster)
        showSafetyTips(disasters.first(), layoutSafetyTips)

        return v
    }

    private fun dial(number: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)
    }

    private fun showSafetyTips(disaster: String, container: LinearLayout) {
        container.removeAllViews()
        val tips = safetyTipsMap[disaster] ?: listOf("No tips available")
        tips.forEach { tip ->
            val tv = TextView(requireContext()).apply {
                text = "• $tip"
                setTextColor(resources.getColor(R.color.text_primary, null))
                textSize = 16f
                setPadding(8, 8, 8, 8)
            }
            container.addView(tv)
        }
    }
}
