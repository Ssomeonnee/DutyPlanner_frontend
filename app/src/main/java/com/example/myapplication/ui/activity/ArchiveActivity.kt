package com.example.myapplication.ui.activity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.data.repository.DutyPlanRepository
import com.example.myapplication.databinding.ActivityArchiveBinding
import com.example.myapplication.ui.viewmodel.ArchiveViewModel
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.model.DutyPlanResponse
import com.example.myapplication.di.ArchiveListViewModelFactory
import com.example.myapplication.ui.adapter.ArchiveListAdapter
import com.example.myapplication.ui.fragment.ArchiveDutyPlanFragment
import com.example.myapplication.utils.ToastUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

class ArchiveActivity : AppCompatActivity() {

    protected lateinit var binding: ActivityArchiveBinding

    private lateinit var listAdapter: ArchiveListAdapter

    private val viewModel: ArchiveViewModel by viewModels {
        ArchiveListViewModelFactory(
            DutyPlanRepository(this)
        )
    }

    // Флаг для отслеживания текущего состояния
    private var isShowingFragment = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setDependensies()
        setupHandlers()
        //getData()

        // Загружаем данные только если не восстанавливаем состояние
        if (savedInstanceState == null) {
            getData()
        } else {
            // Восстанавливаем состояние фрагмента
            isShowingFragment = savedInstanceState.getBoolean("is_showing_fragment", false)
            if (isShowingFragment) {
                binding.listContainer.visibility = View.GONE
                binding.fragmentContainer.visibility = View.VISIBLE
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("is_showing_fragment", isShowingFragment)
    }

   fun setDependensies() {

    }

    fun setupHandlers() {
        // Инициализируем адаптер
        listAdapter = ArchiveListAdapter(
            onItemClick = { position, plan ->
                showDutyPlanFragment(plan)
                if (plan.dutyPlaces.isNotEmpty()) {
                    val adapter = ArrayAdapter(
                        this,
                        R.layout.spinner_item,
                        plan.dutyPlaces.map { it.getSpinnerName() })
                    binding.filterSpinner.adapter = adapter
                    binding.filterSpinner.visibility = View.VISIBLE
                }
            }
        )

        // Настраиваем RecyclerView
        binding.itemRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ArchiveActivity)
            adapter = listAdapter // Устанавливаем адаптер
        }

        binding.iconBackImageView.setOnClickListener {
            if (isShowingFragment) {
                // Если показываем фрагмент - возвращаемся к списку
                hideDutyPlanFragment()
            } else {
                // Если показываем список - возвращаемся к MainActivity
                finish()
            }
        }
    }

    private fun showDutyPlanFragment(plan: DutyPlanResponse) {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            val fragment = ArchiveDutyPlanFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("plan", plan)
                }
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack("duty_plan_fragment")
                .commit()
        }

        binding.listContainer.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE
        isShowingFragment = true
        binding.listTitleTextView.text = plan.getTitle()
        //binding.filterSpinner.visibility = View.VISIBLE
    }

    private fun hideDutyPlanFragment() {
        supportFragmentManager.popBackStack("duty_plan_fragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        binding.listContainer.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.GONE
        isShowingFragment = false
        binding.listTitleTextView.text = "Архивные графики"
        binding.filterSpinner.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (isShowingFragment) {
            hideDutyPlanFragment()
        } else {
            super.onBackPressed()
        }
    }

    fun getData() {


       lifecycleScope.launch {
            viewModel.error.collectLatest { errorMessage ->
                errorMessage?.let {
                    // Показываем ошибку пользователю (можно использовать Toast, Snackbar или TextView)
                    ToastUtil.showCustomToast(this@ArchiveActivity, it)
                    // Или можно отображать в emptyStateTextView
                    showEmptyState("Ошибка загрузки")
                }
            }
        }

       lifecycleScope.launch {
            viewModel.dutyPlans.collectLatest { dutyPlans ->
                listAdapter.submitList(dutyPlans)
                // Показываем или скрываем TextView в зависимости от наличия данных
                updateEmptyState(dutyPlans)
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
                    viewModel.dutyPlans.value.let { places ->
                        if (places.isEmpty()) {
                            showEmptyState("Список графиков пуст")
                        }
                    }
                }
            }
        }
    }

    private fun showEmptyState(message: String) {
        binding.emptyStateTextView.visibility = View.VISIBLE
        binding.emptyStateTextView.text = message
    }

    private fun hideEmptyState() {
        binding.emptyStateTextView.visibility = View.GONE
    }

    private fun updateEmptyState(plans: List<DutyPlanResponse>) {
        if (plans.isEmpty()) {
            showEmptyState("Список графиков пуст")
        } else {
            hideEmptyState()
        }
    }
}
