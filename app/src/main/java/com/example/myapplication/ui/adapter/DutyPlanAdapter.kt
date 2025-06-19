package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.DutyplanItemMainActivityBinding

class DutyPlanAdapter(
    private var data: Array<Array<String>>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<DutyPlanAdapter.CellViewHolder>() {

    private var selectedRow = -1

    fun updateData(newData: Array<Array<String>>) {
        // Проверяем, что newData не пустой
        data = if (newData.isNotEmpty() && newData[0].isNotEmpty()) {
            newData
        } else {
            arrayOf(arrayOf()) // Пустая таблица 1x1
        }
        notifyDataSetChanged()
    }

    fun setSelectedRow(position: Int) {
        val previousSelected = selectedRow
        selectedRow = position

        if (previousSelected != -1) {
            val startPos = previousSelected * data[0].size
            val itemCount = data[0].size
            notifyItemRangeChanged(startPos, itemCount)
        }

        if (selectedRow != -1) {
            val startPos = selectedRow * data[0].size
            val itemCount = data[0].size
            notifyItemRangeChanged(startPos, itemCount)
        }
    }

    inner class CellViewHolder(
        private val binding: DutyplanItemMainActivityBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cellValue: String, rowPosition: Int) {
            binding.textTextView.text = cellValue

            binding.root.setOnClickListener {
                // Только передаем позицию во ViewModel
                onItemClick(if (rowPosition == selectedRow) -1 else rowPosition)
            }

            binding.root.background = if (rowPosition == selectedRow) {
                ContextCompat.getDrawable(binding.root.context, R.drawable.dutyplan_selected_cell_bg)
            } else {
                ContextCompat.getDrawable(binding.root.context, R.drawable.dutyplan_default_cell_bg)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        val binding = DutyplanItemMainActivityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CellViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        if (data.isNotEmpty() && data[0].isNotEmpty()) {
            val row = position / data[0].size
            val col = position % data[0].size
            holder.bind(data[row][col], row)
        }
    }

    override fun getItemCount(): Int {
        return if (data.isEmpty() || data[0].isEmpty()) {
            0
        } else {
            data.size * data[0].size
        }
    }
}