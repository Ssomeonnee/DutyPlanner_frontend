package com.example.myapplication.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.model.DutyPlanResponse
import com.example.myapplication.ui.adapter.DatesAdapter
import com.example.myapplication.ui.adapter.DutyPlanAdapter
import com.example.myapplication.ui.adapter.UsersAdapter
import com.example.myapplication.viewmodel.ArchiveDutyPlanViewModel

class ArchiveDutyPlanFragment : BaseDutyPlanFragment<ArchiveDutyPlanViewModel>() {
    override val viewModel: ArchiveDutyPlanViewModel by viewModels()

    protected lateinit var usersAdapter: UsersAdapter
    protected lateinit var dutyPlanAdapter: DutyPlanAdapter
    protected lateinit var datesAdapter: DatesAdapter

    private var plan: DutyPlanResponse? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Восстанавливаем данные из аргументов или сохраненного состояния
        plan = arguments?.getParcelable("plan") ?: savedInstanceState?.getParcelable("saved_plan")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        plan?.let {
            viewModel.loadDutyPlanData(it)
        } ?: run {
            viewModel._emptyStateMessage.value = "Не удалось загрузить данные графика"
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        plan?.let {
            outState.putParcelable("saved_plan", it)
        }
    }

    override fun initAdapters() {
        usersAdapter = UsersAdapter(emptyList()) { position ->
            viewModel.toggleRowSelection(position)
        }

        dutyPlanAdapter = DutyPlanAdapter(emptyArray()) { position ->
            viewModel.toggleRowSelection(position)
        }

        datesAdapter = DatesAdapter(emptyList())
    }

    override fun setupRecyclerViews() {
        binding.datesRecyclerView.apply {
            adapter = datesAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }

        binding.usersRecyclerView.apply {
            adapter = usersAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.dutyPlanRecyclerView.apply {
            adapter = dutyPlanAdapter
            layoutManager = GridLayoutManager(requireContext(), 30)
        }
    }

    override fun updateTableViews(
        users: List<String>,
        dates: List<String>,
        table: Array<Array<String>>
    ) {
        usersAdapter.updateData(users)
        datesAdapter.updateData(dates)
        dutyPlanAdapter.updateData(table)
        (binding.dutyPlanRecyclerView.layoutManager as? GridLayoutManager)?.spanCount = dates.size
    }
}
