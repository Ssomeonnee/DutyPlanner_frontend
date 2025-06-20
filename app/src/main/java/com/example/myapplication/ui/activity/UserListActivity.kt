package com.example.myapplication.ui.activity

import android.os.Bundle
import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.model.User
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.di.UserListViewModelFactory
import com.example.myapplication.ui.adapter.UserListAdapter
import com.example.myapplication.ui.fragment.UsersInfoFragment
import com.example.myapplication.utils.ToastUtil
import com.example.myapplication.ui.viewmodel.UserListViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class UserListActivity : ListActivity() {

    private lateinit var listAdapter: UserListAdapter

    private val viewModel: UserListViewModel by viewModels {
        UserListViewModelFactory(
            UserRepository(this)
        )
    }

    override fun setupHandlers() {
        // Инициализируем адаптер
        listAdapter = UserListAdapter(
            onItemClick = { position, user ->
                UsersInfoFragment().apply {
                    arguments = Bundle().apply {
                        //putInt("user_id", user.id)
                        putParcelable("user", user)
                        putString("mode", "viewMode")
                        //putBoolean("viewMode", true)
                    }
                }.show(supportFragmentManager, "UsersInfoDialog")
            }
        )

        // Настраиваем RecyclerView
        binding.itemRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@UserListActivity)
            adapter = listAdapter // Устанавливаем адаптер
        }

        binding.iconAddImageView.setOnClickListener {
            UsersInfoFragment().apply {
                arguments = Bundle().apply {
                    putString("mode", "createMode")
                    //putBoolean("createMode", true)
                }
            }.show(supportFragmentManager, "UsersInfoDialog")
        }
    }

    override fun getData() {
        binding.listTitleTextView.text = "Сотрудники"

        lifecycleScope.launch {
            viewModel.error.collectLatest { errorMessage ->
                errorMessage?.let {
                    // Показываем ошибку пользователю (можно использовать Toast, Snackbar или TextView)
                    ToastUtil.showCustomToast(this@UserListActivity, it)
                    // Или можно отображать в emptyStateTextView
                    showEmptyState("Ошибка загрузки")
                }
            }
        }

        lifecycleScope.launch {
            viewModel.users.collectLatest { users ->
                listAdapter.submitList(users)
                // Показываем или скрываем TextView в зависимости от наличия данных
                updateEmptyState(users)
            }
        }

        lifecycleScope.launch {
            viewModel.loading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                // Если идет загрузка, скрываем сообщение о пустом списке
                if (isLoading) {
                    hideEmptyState()
                } else {
                    // После завершения загрузки проверяем список
                    viewModel.users.value.let { users ->
                        if (users.isEmpty()) {
                            showEmptyState("Список сотрудников пуст")
                        }
                    }
                }
            }
        }

        supportFragmentManager.setFragmentResultListener("user_updated", this) { _, _ ->
            viewModel.refresh()
        }
    }

    private fun showEmptyState(message: String) {
        binding.emptyStateTextView.visibility = View.VISIBLE
        binding.emptyStateTextView.text = message
    }

    private fun hideEmptyState() {
        binding.emptyStateTextView.visibility = View.GONE
    }

    private fun updateEmptyState(users: List<User>) {
        if (users.isEmpty()) {
            showEmptyState("Список сотрудников пуст")
        } else {
            hideEmptyState()
        }
    }

    override fun setupNav() {
        binding.bottomNav.selectedItemId = R.id.nav_users

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_users -> true
                R.id.nav_dutyplans -> {
                    navigateToMain()
                    true
                }
                R.id.nav_places -> {
                    navigateToPlaces()
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateToMain() {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }.also {
            startActivity(it)
            finish()
        }
    }

    private fun navigateToPlaces() {
        Intent(this, PlaceListActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }.also {
            startActivity(it)
            finish()
        }
    }

    override fun onNewIntent(intent: Intent?) {
      //  super.onNewIntent(intent)
        binding.bottomNav.selectedItemId = R.id.nav_users
    }

}


