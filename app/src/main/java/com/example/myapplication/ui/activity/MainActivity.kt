package com.example.myapplication.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.repository.DutyPlaceRepository
import com.example.myapplication.data.repository.DutyPlanRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.di.CurrentDutyPlanViewModelFactory
import com.example.myapplication.ui.fragment.CurrentDutyPlanFragment
import com.example.myapplication.ui.fragment.DeleteDialogFragment
import com.example.myapplication.viewmodel.CurrentDutyPlanViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: CurrentDutyPlanViewModel by viewModels {
        CurrentDutyPlanViewModelFactory(
            DutyPlanRepository(this),
            UserRepository(this),
            DutyPlaceRepository(this)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Проверка авторизации
        if (!isUserLoggedIn()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        setupUIForCurrentUserRole()
        setupObservers()
        setupHandlers()

        // Загрузка фрагмента
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentСont, CurrentDutyPlanFragment())
                .commit()
        }

    }

    private fun setupUIForCurrentUserRole() {
        val sharedPref = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val isAdmin = sharedPref.getBoolean("is_admin", false)

        if (!isAdmin) {
            // Скрываем/отключаем элементы для обычных сотрудников
            binding.bottomNav.visibility = View.GONE
            binding.dutyplansImageView.visibility = View.GONE
            binding.editImageView.visibility = View.GONE

            // Можно также добавить:
            binding.editImageView.isEnabled = false
            binding.dutyplansImageView.isEnabled = false
        }
        else {
            setupAdminObservers()
            setupAdminHandlers()
            setupNavigation()
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPref = getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPref.getString("token", null) != null
    }

    fun setupObservers() {
        // Подписка на изменения
        lifecycleScope.launch {
            viewModel.title.collect { title ->
                binding.monthTextView.text = title
            }
        }

    }

    fun setupAdminObservers() {
        // Подписка на изменения
        lifecycleScope.launch {
            viewModel.isEditMode.collectLatest { isEditMode ->
                updateUIForEditMode(isEditMode)
            }
        }

    }

    fun setupHandlers() {
        binding.logoutImageView.setOnClickListener {
            showLogoutDialog()
        }
    }

    fun setupAdminHandlers() {

        binding.editImageView.setOnClickListener {
            if (viewModel.places.value.isNotEmpty() && viewModel.users.value.isNotEmpty()) {
                viewModel.toggleEditMode()
                // Можно также уведомить фрагмент о начале редактирования
                (supportFragmentManager.findFragmentById(R.id.fragmentСont) as? CurrentDutyPlanFragment)?.startEditing()
            }
            else {
                Toast.makeText(this,
                    if (viewModel.places.value.isEmpty()) "Места дежурств не добавлены"
                    else "Сотрудники не добавлены",
                    Toast.LENGTH_SHORT).show()
            }
        }

        binding.submitImageView.setOnClickListener {
            if (isValidInput()) {
                it.isEnabled = false
                it.alpha = 0.5f

                try {
                    // Получаем фрагмент и вызываем сохранение
                    (supportFragmentManager.findFragmentById(R.id.fragmentСont) as? CurrentDutyPlanFragment)?.let { fragment ->
                        fragment.confirmEditing()

                        // Ждём, пока выключится режим редактирования
                        // Подписываемся на изменения состояния сохранения
                        lifecycleScope.launch {
                            viewModel.isSaving.collect { isSaving ->
                                if (!isSaving) {
                                    // Восстанавливаем кнопку только если сохранение завершено (успешно или с ошибкой)
                                    it.isEnabled = true
                                    it.alpha = 1f
                                }
                            }
                        }
                    }
                }
                catch (e: Exception) {
                    // В случае ошибки всё равно разблокируем кнопку
                    it.isEnabled = true
                    it.alpha = 1f
                    Toast.makeText(
                        this@MainActivity,
                        "Ошибка сохранения: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else {
                Toast.makeText(this, "Имеются недопустимые значения", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLogoutDialog() {
        val dialog = DeleteDialogFragment.newInstance(
            title = "Выход из учетной записи",
            message = "Вы уверены, что хотите выйти из своей учетной записи?",
            deleteName = "Выйти",
            onDeleteConfirmed = { performLogout() }
        )
        dialog.show(supportFragmentManager, "DeleteDialog")
    }

    private fun performLogout() {
        // Очищаем данные авторизации
        getSharedPreferences("auth", Context.MODE_PRIVATE).edit().clear().apply()

        // Переходим на экран авторизации
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }


    private fun isValidInput(): Boolean {
        // Проверяем, есть ли недопустимые значения в таблице
        val tableData = viewModel.tableData.value?.third ?: return false
        val validCodes = viewModel.places.value.mapNotNull { it.shortName }

        for (row in tableData) {
            for (value in row) {
                if (value.isNotEmpty() && !validCodes.contains(value)) {
                    return false
                }
            }
        }
        return true
    }

    private fun updateUIForEditMode(isEditMode: Boolean) {
        if (isEditMode) {
            // Включение режима редактирования
            binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_black))
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent_black)
            binding.monthTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.submitImageView.visibility = View.VISIBLE
            binding.editImageView.visibility = View.GONE
            binding.dutyplansImageView.isEnabled = false
            binding.logoutImageView.isEnabled = false

            // Здесь можно добавить другие изменения UI для режима редактирования
        } else {
            // Выключение режима редактирования
            binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.lightest_gray))
            window.statusBarColor = ContextCompat.getColor(this, R.color.lightest_gray)
            binding.monthTextView.setTextColor(ContextCompat.getColor(this, R.color.darkest_gray))
            binding.submitImageView.visibility = View.GONE
            binding.editImageView.visibility = View.VISIBLE
            binding.dutyplansImageView.isEnabled = true
            binding.logoutImageView.isEnabled = true

            // Здесь можно добавить отмену других изменений UI
        }
    }

    private fun setupNavigation() {
        binding.bottomNav.selectedItemId = R.id.nav_dutyplans

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_users -> {
                    navigateToUsers()
                    true
                }
                R.id.nav_dutyplans -> true
                R.id.nav_places -> {
                    navigateToPlaces()
                    true
                }
                else -> false
            }
        }

        binding.dutyplansImageView.setOnClickListener {
            startActivity(Intent(this, ArchiveActivity::class.java))
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

    private fun navigateToPlaces() {
        Intent(this, PlaceListActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }.also {
            startActivity(it)
            finish()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        binding.bottomNav.selectedItemId = R.id.nav_dutyplans
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle("view_model_state", viewModel.saveState())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getBundle("view_model_state")?.let {
            viewModel.restoreState(it)
        }
    }
}