package com.example.myapplication.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.repository.DutyPlaceRepository
import com.example.myapplication.data.repository.DutyPlanRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.di.CurrentDutyPlanViewModelFactory
import com.example.myapplication.ui.adapter.DatesAdapter
import com.example.myapplication.ui.adapter.EditableDutyPlanAdapter
import com.example.myapplication.ui.adapter.UsersAdapter
import com.example.myapplication.ui.viewmodel.CurrentDutyPlanViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CurrentDutyPlanFragment : BaseDutyPlanFragment<CurrentDutyPlanViewModel>() {

    private lateinit var editableDutyPlanAdapter: EditableDutyPlanAdapter
    protected lateinit var usersAdapter: UsersAdapter
    protected lateinit var datesAdapter: DatesAdapter


    override val viewModel: CurrentDutyPlanViewModel by activityViewModels {
        CurrentDutyPlanViewModelFactory(
            DutyPlanRepository(requireContext()),
            UserRepository(requireContext()),
            DutyPlaceRepository(requireContext())
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            viewModel.loadCurrentPlan()
        }

        // Подписываемся на изменения режима редактирования
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isEditMode.collectLatest { isEditMode ->
                editableDutyPlanAdapter.setEditMode(isEditMode, view)
            }
        }
    }

    override fun observeViewModel() {
        super.observeViewModel()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedRow.collect { selectedRow ->
                usersAdapter.setSelectedRow(selectedRow)
                editableDutyPlanAdapter.setSelectedRow(selectedRow)
            }
        }

       viewLifecycleOwner.lifecycleScope.launch {
            viewModel.places.collectLatest { places ->
                val placeCodes = places.mapNotNull { it.shortName }
                editableDutyPlanAdapter.updateValidPlaceCodes(placeCodes)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tableData.collectLatest { data ->
                if (data != null) {
                    Log.d("Fragment", "Table data updated: ${data.third.contentDeepToString()}")
                    val (users, dates, table) = data
                    binding.dutyPlanRecyclerView.post {
                        editableDutyPlanAdapter.updateData(table)
                        usersAdapter.updateData(users)
                        datesAdapter.updateData(dates)
                        (binding.dutyPlanRecyclerView.layoutManager as? GridLayoutManager)?.spanCount = dates.size
                    }
                }
            }
        }

        fun hasInvalidInput(): Boolean {
            return editableDutyPlanAdapter.hasInvalidInput()
        }

    }

    override fun initAdapters() {

        editableDutyPlanAdapter = EditableDutyPlanAdapter(
            emptyArray(),
            emptyList(),
            { position -> viewModel.toggleRowSelection(position) },
            { row, col, newValue -> viewModel.updateCellValue(row, col, newValue) }
        )

        usersAdapter = UsersAdapter(emptyList()) { position ->
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
            adapter = editableDutyPlanAdapter
            layoutManager = GridLayoutManager(requireContext(), 30)
        }
    }

    override fun updateTableViews(users: List<String>, dates: List<String>, table: Array<Array<String>>) {
        if (!isAdded || isDetached) return

        view?.post {
            val places = viewModel.places.value.mapNotNull { it.shortName }
            editableDutyPlanAdapter.updateData(table)
            usersAdapter.updateData(users)
            datesAdapter.updateData(dates)
            (binding.dutyPlanRecyclerView.layoutManager as? GridLayoutManager)?.spanCount = dates.size
        }
    }

    fun startEditing() {
        editableDutyPlanAdapter.setEditMode(true, view)
        viewModel.setEditMode(true)
    }

    fun confirmEditing() {
        viewModel.saveChanges()
    }

}
