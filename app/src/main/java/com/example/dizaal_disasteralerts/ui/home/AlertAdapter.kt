package com.example.dizaal_disasteralerts.ui.home

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dizaal_disasteralerts.R

class AlertAdapter(
    private var data: List<AlertItem>,
    private val onItemClick: (AlertItem) -> Unit
) : RecyclerView.Adapter<AlertAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val container: LinearLayout = v.findViewById(R.id.container)
        val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        val tvSubtitle: TextView = v.findViewById(R.id.tvSubtitle)
        val tvTime: TextView = v.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_alert, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = data[position]
        h.tvTitle.text = item.title
        h.tvSubtitle.text = item.subtitle
        h.tvTime.text = item.time

        h.itemView.setOnClickListener { onItemClick(item) }

        // Set background based on severity
        val bgRes = when (item.severity) {
            Severity.CYCLONES -> R.drawable.cyclones
            Severity.HEATWAVES -> R.drawable.heatwaves
            Severity.COLDWAVES -> R.drawable.cold_waves
            Severity.FLOODS -> R.drawable.floods
            Severity.EARTHQUAKES -> R.drawable.earthquakes
            Severity.TSUNAMIS -> R.drawable.tsunamis
        }

        h.container.post {
            h.container.background = getScaledBitmap(h.container, bgRes)
        }
    }

    override fun getItemCount() = data.size

    fun submit(newData: List<AlertItem>) {
        data = newData
        notifyDataSetChanged()
    }

    private fun getScaledBitmap(view: View, resId: Int): BitmapDrawable {
        val resources = view.resources
        val reqWidth = view.width.takeIf { it > 0 } ?: 800
        val reqHeight = view.height.takeIf { it > 0 } ?: 800

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(resources, resId, options)

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        val bitmap = BitmapFactory.decodeResource(resources, resId, options)
        return BitmapDrawable(resources, bitmap)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
