package com.example.design

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlacementAdapter(private val placementDataList: List<PlacementData>) :
    RecyclerView.Adapter<PlacementAdapter.PlacementViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(placementData: PlacementData)
    }
    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: PlacementActivity) {
        this.listener = listener
    }

    inner class PlacementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jobRoleTextView: TextView = itemView.findViewById(R.id.jobRoleTextView)
        val companyNameTextView: TextView = itemView.findViewById(R.id.companyNameTextView)
        val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        val postedDateTextView: TextView = itemView.findViewById(R.id.postedDateTextView)

        init {
            // Set click listener inside ViewHolder constructor
            itemView.setOnClickListener {
                listener?.onItemClick(placementDataList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)
        return PlacementViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlacementViewHolder, position: Int) {
        val placementData = placementDataList[position]
        holder.jobRoleTextView.text = placementData.jobRole
        holder.companyNameTextView.text = placementData.companyName
        holder.locationTextView.text = placementData.location
        holder.postedDateTextView.text = placementData.postedDate

    }

    override fun getItemCount(): Int {
        return placementDataList.size
    }
}
