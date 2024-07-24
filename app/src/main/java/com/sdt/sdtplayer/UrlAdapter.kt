package com.sdt.sdtplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UrlAdapter(
    private val urlList: MutableList<String>,
    private val showDeleteButton: Boolean,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<UrlAdapter.UrlViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class UrlViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val urlText: TextView = itemView.findViewById(R.id.channel_url)
        val deleteButton: Button = itemView.findViewById(R.id.delete_channel)
        val channelNumber: TextView = itemView.findViewById(R.id.channel_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrlViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false)
        return UrlViewHolder(view)
    }

    override fun onBindViewHolder(holder: UrlViewHolder, position: Int) {
        val url = urlList[position]
        holder.urlText.text = url
        holder.channelNumber.text = (position + 1).toString()

        // Actualiza la apariencia según la posición seleccionada
        holder.itemView.setBackgroundResource(
            if (selectedPosition == position) R.drawable.channel_selected_background
            else R.drawable.channel_background
        )

        holder.deleteButton.visibility = if (showDeleteButton) View.VISIBLE else View.GONE
        holder.deleteButton.text = "X"

        holder.deleteButton.setOnClickListener {
            onDeleteClick(position)
        }

        holder.itemView.setOnClickListener {
            notifyItemChanged(selectedPosition)
            selectedPosition = holder.adapterPosition
            notifyItemChanged(selectedPosition)
        }

        // Hacer que los elementos sean enfocables
        holder.itemView.isFocusable = true
        holder.itemView.isFocusableInTouchMode = true
        holder.deleteButton.isFocusable = true
        holder.deleteButton.isFocusableInTouchMode = true
    }

    override fun getItemCount() = urlList.size

    fun setSelectedPosition(position: Int) {
        notifyItemChanged(selectedPosition)
        selectedPosition = position
        notifyItemChanged(selectedPosition)
    }
}