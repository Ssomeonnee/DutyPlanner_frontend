package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.DutyPlanResponse
import com.example.myapplication.databinding.FragmentUserItemBinding

class ArchiveListAdapter(
    private val onItemClick: (position: Int, plan: DutyPlanResponse) -> Unit
) : ListAdapter<DutyPlanResponse, ArchiveListAdapter.ArchiveViewHolder>(ArchiveDiffCallback()) {

    inner class ArchiveViewHolder(private val binding: FragmentUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: DutyPlanResponse) {
            binding.userNameTextView.text = plan.getTitle()
            binding.root.setOnClickListener {
                onItemClick(adapterPosition, plan)
            }
            binding.itemIcon.setImageResource(R.drawable.cloud)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveViewHolder {
        val binding = FragmentUserItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArchiveViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArchiveViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ArchiveDiffCallback : DiffUtil.ItemCallback<DutyPlanResponse>() {
        override fun areItemsTheSame(oldItem: DutyPlanResponse, newItem: DutyPlanResponse): Boolean {
            return oldItem.id == newItem.id // Сравниваем по ID
        }

        override fun areContentsTheSame(oldItem: DutyPlanResponse, newItem: DutyPlanResponse): Boolean {
            return oldItem == newItem // Сравниваем все поля
        }
    }


}