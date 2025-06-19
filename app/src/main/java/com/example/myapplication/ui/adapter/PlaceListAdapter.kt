package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.DutyPlace
import com.example.myapplication.databinding.FragmentUserItemBinding


class PlaceListAdapter (
    private val onItemClick: (position: Int, place: DutyPlace) -> Unit
) : ListAdapter<DutyPlace, PlaceListAdapter.PlaceViewHolder>(DutyPlaceDiffCallback()) {

    inner class PlaceViewHolder(private val binding: FragmentUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(place: DutyPlace) {
            binding.userNameTextView.text = place.name
            binding.root.setOnClickListener {
                onItemClick(adapterPosition, place)
            }
            binding.itemIcon.setImageResource(R.drawable.home)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = FragmentUserItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DutyPlaceDiffCallback : DiffUtil.ItemCallback<DutyPlace>() {
        override fun areItemsTheSame(oldItem: DutyPlace, newItem: DutyPlace): Boolean {
            return oldItem.id == newItem.id // Сравниваем по ID
        }

        override fun areContentsTheSame(oldItem: DutyPlace, newItem: DutyPlace): Boolean {
            return oldItem == newItem // Сравниваем все поля
        }
    }

}