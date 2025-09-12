package com.example.dizaal_disasteralerts.ui.emergency

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dizaal_disasteralerts.R

class EmergencyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_emergency, container, false)

        v.findViewById<View>(R.id.btnPolice).setOnClickListener {
            dial("100") // police emergency number
        }

        v.findViewById<View>(R.id.btnAmbulance).setOnClickListener {
            dial("102") // ambulance emergency number
        }

        v.findViewById<View>(R.id.btnFire).setOnClickListener {
            dial("101") // fire emergency number
        }

        return v
    }

    private fun dial(number: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)
    }
}
