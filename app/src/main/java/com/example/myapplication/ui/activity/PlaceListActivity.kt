package com.example.myapplication.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.data.model.DutyPlace
import com.example.myapplication.data.repository.DutyPlaceRepository
import com.example.myapplication.di.PlaceListViewModelFactory
import com.example.myapplication.ui.adapter.PlaceListAdapter
import com.example.myapplication.ui.fragment.PlacesInfoFragment
import com.example.myapplication.utils.ToastUtil
import com.example.myapplication.viewmodel.PlaceListViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

class PlaceListActivity : ListActivity() {

    private lateinit var listAdapter: PlaceListAdapter

    private val viewModel: PlaceListViewModel by viewModels {
        PlaceListViewModelFactory(
            DutyPlaceRepository(this)
        )
    }

    override fun setupHandlers() {
        // Инициализируем адаптер
        listAdapter = PlaceListAdapter(
            onItemClick = { position, place ->
                PlacesInfoFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("place", place)
                        putString("mode", "viewMode")
                    }
                }.show(supportFragmentManager, "PLacesInfoDialog")
            }
        )

        // Настраиваем RecyclerView
        binding.itemRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlaceListActivity)
            adapter = listAdapter // Устанавливаем адаптер
        }

        binding.iconAddImageView.setOnClickListener {
            PlacesInfoFragment().apply {
                arguments = Bundle().apply {
                    putString("mode", "createMode")
                }
            }.show(supportFragmentManager, "PlacesInfoDialog")
        }
    }

    override fun getData() {
        binding.listTitleTextView.text = "Места дежурств"

        lifecycleScope.launch {
            viewModel.error.collectLatest { errorMessage ->
                errorMessage?.let {
                    // Показываем ошибку пользователю (можно использовать Toast, Snackbar или TextView)
                    ToastUtil.showCustomToast(this@PlaceListActivity, it)
                    // Или можно отображать в emptyStateTextView
                    showEmptyState("Ошибка загрузки")
                }
            }
        }

        lifecycleScope.launch {
            viewModel.places.collectLatest { places ->
                listAdapter.submitList(places)
                // Показываем или скрываем TextView в зависимости от наличия данных
                updateEmptyState(places)
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
                    viewModel.places.value.let { places ->
                        if (places.isEmpty()) {
                            showEmptyState("Список мест дежурств пуст")
                        }
                    }
                }
            }
        }

        supportFragmentManager.setFragmentResultListener("place_updated", this) { _, _ ->
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

    private fun updateEmptyState(places: List<DutyPlace>) {
        if (places.isEmpty()) {
            showEmptyState("Список мест дежурств пуст")
        } else {
            hideEmptyState()
        }
    }

    override fun setupNav() {
        binding.bottomNav.selectedItemId = R.id.nav_places

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_users -> {
                    navigateToUsers()
                    true
                }
                R.id.nav_dutyplans -> {
                    navigateToMain()
                    true
                }
                R.id.nav_places -> true
                else -> false
            }
        }
    }

    private fun navigateToUsers() {
        Intent(this, UserListActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }.also {
            startActivity(it)
            finish()
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

    override fun onNewIntent(intent: Intent?) {
        //super.onNewIntent(intent)
        binding.bottomNav.selectedItemId = R.id.nav_places
    }

}

