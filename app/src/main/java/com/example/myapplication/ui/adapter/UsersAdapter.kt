package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.UserItemMainActivityBinding

class UsersAdapter(
    private var users: List<String>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    private var selectedRow = -1

    fun updateData(newUsers: List<String>) {
        users = newUsers
        notifyDataSetChanged()
    }

    fun setSelectedRow(position: Int) {

        if (users.isEmpty() || users[0].isEmpty()) {
            return
        }

        val prev = selectedRow
        selectedRow = position
        if (prev != -1) notifyItemChanged(prev)
        if (selectedRow != -1) notifyItemChanged(selectedRow)
    }

    inner class UserViewHolder(
        private val binding: UserItemMainActivityBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: String, position: Int) {
            binding.textTextView.text = user

            binding.root.setOnClickListener {
                onItemClick(position)
            }

            binding.root.background = if (position == selectedRow) {
                ContextCompat.getDrawable(binding.root.context, R.drawable.user_selected_cell_bg)
            } else {
                ContextCompat.getDrawable(binding.root.context, R.drawable.user_default_cell_bg)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = UserItemMainActivityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position], position)
    }

    override fun getItemCount() = users.size

}
