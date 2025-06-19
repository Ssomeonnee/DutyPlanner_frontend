
package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentUserItemBinding

class ListAdapter(
    private val onItemClick: (position: Int, item: String) -> Unit
) : RecyclerView.Adapter<ListAdapter.UserViewHolder>() {

    private var list: List<String> = emptyList()

    inner class UserViewHolder(val binding: FragmentUserItemBinding) : //хранит ссылки на View каждого элемента списка
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { // настраивается обработчик кликов на элементе списка
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(position, list[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder { // создание View для элемента списка и возвращение UserViewHolder, который хранит ссылки на эти View
        val binding = FragmentUserItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) { // нужно обновить View для элемента списка на определенной позиции
        holder.binding.userNameTextView.text = list[position]
    }

    override fun getItemCount() = list.size // узнать количество элементов в списке

    fun submitList(newList: List<String>) { // обновления списка пользователей
        list = newList
        notifyDataSetChanged()
    }
}

