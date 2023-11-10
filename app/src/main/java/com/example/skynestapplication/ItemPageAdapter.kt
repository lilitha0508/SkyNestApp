package com.example.skynestapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemPageAdapter(private val title: List<String>, private val desc: List<String>) :
    RecyclerView.Adapter<ItemPageAdapter.Pager2ViewHolder>() {
    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewTitle: TextView = itemView.findViewById(R.id.tvVPHeading)
        val viewDesc: TextView = itemView.findViewById(R.id.tvMessage)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemPageAdapter.Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemPageAdapter.Pager2ViewHolder, position: Int) {
        holder.viewTitle.text = title[position]
        holder.viewDesc.text = desc[position]
    }

    override fun getItemCount(): Int {
        return title.size
    }
}