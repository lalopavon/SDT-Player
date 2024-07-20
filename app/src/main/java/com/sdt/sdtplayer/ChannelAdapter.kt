package com.sdt.sdtplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChannelAdapter(
    private val channelList: List<String>,
    private val onChannelClick: (String) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val channelText: TextView = itemView.findViewById(R.id.channel_url)

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(selectedPosition)
                    selectedPosition = adapterPosition
                    notifyItemChanged(selectedPosition)
                    onChannelClick(channelList[adapterPosition])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.channelText.text = channelList[position]
        if (selectedPosition == position) {
            holder.itemView.setBackgroundResource(R.drawable.channel_selected_background)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.channel_background)
        }
    }

    override fun getItemCount() = channelList.size

    fun setSelectedPosition(position: Int) {
        notifyItemChanged(selectedPosition)
        selectedPosition = position
        notifyItemChanged(selectedPosition)
    }
}