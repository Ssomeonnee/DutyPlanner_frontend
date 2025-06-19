package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class DatesAdapter(private var dates: List<String>) : RecyclerView.Adapter<DatesAdapter.DateViewHolder>() {

    class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.date_text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.date_item_main_activity, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        holder.textView.text = dates[position]
    }

    override fun getItemCount() = dates.size

    fun updateData(newDates: List<String>) {
        dates = newDates
        notifyDataSetChanged()
    }
}