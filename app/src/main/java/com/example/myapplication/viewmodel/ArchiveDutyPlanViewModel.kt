package com.example.myapplication.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.myapplication.data.model.DutyPlanResponse

class ArchiveDutyPlanViewModel() : BaseDutyPlanViewModel() {

    fun loadDutyPlanData(plan: DutyPlanResponse) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _emptyStateMessage.value = null

                when {
                    plan.assignedUsers.isEmpty() -> {
                        _emptyStateMessage.value = "Пользователи не добавлены"
                        _tableData.value = null
                    }
                    plan.duties.isEmpty() || plan.dutyPlaces.isEmpty() -> {
                        _tableData.value = createEmptyTable(plan.assignedUsers, plan.year, plan.month)
                    }
                    else -> {
                        _tableData.value = buildDutyTable(plan.assignedUsers, plan.dutyPlaces, plan.duties, plan.year, plan.month)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
                _emptyStateMessage.value = "Ошибка загрузки данных"

            } finally {
                _loading.value = false
            }
        }
    }
}

