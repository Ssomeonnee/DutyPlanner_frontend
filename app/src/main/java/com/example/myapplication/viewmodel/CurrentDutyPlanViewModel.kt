package com.example.myapplication.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.DutyChange
import com.example.myapplication.data.model.DutyPlace
import com.example.myapplication.data.model.DutyPlanResponse
import com.example.myapplication.data.model.User
import com.example.myapplication.data.repository.DutyPlaceRepository
import com.example.myapplication.data.repository.DutyPlanRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.event.DataChangeEventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CurrentDutyPlanViewModel(
    private val planRepository: DutyPlanRepository,
    private val userRepository: UserRepository,
    private val placeRepository: DutyPlaceRepository
) : BaseDutyPlanViewModel() {

    // Добавляем поля для хранения исходных данных
    private var originalTableData: Array<Array<String>> = emptyArray()
    private var originalUsers: List<User> = emptyList()
    private var originalPlaces: List<DutyPlace> = emptyList()

    private val _isEditMode = MutableStateFlow<Boolean>(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode

    private val _planData = MutableStateFlow<DutyPlanResponse?>(null)
    val planData: StateFlow<DutyPlanResponse?> = _planData

    private val _places = MutableStateFlow<List<DutyPlace>>(emptyList())
    val places: StateFlow<List<DutyPlace>> = _places

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    override val _selectedRow = MutableStateFlow(-1)
    override val selectedRow: StateFlow<Int> = _selectedRow

    private var isRestoringState = false

    init {

        viewModelScope.launch {
            DataChangeEventBus.userChanges.collect {
                if (!_isEditMode.value) refreshData()
            }
        }

        viewModelScope.launch {
            DataChangeEventBus.placeChanges.collect {
                if (!_isEditMode.value) refreshData()
            }
        }
    }


    fun loadCurrentPlan() {
        viewModelScope.launch {
            try {
                 _loading.value = true
                 _emptyStateMessage.value = null

                 val (users, places, plan) = Triple(
                     userRepository.getUsers(),
                     placeRepository.getDutyPlaces(),
                     planRepository.getCurrentDutyPlan()
                 )

                // Если в режиме редактирования - не перезаписываем таблицу
                if (!_isEditMode.value) {
                    _planData.value = plan
                    _users.value = users
                    _places.value = places
                    _title.value = plan.getTitle()
                    updateTable(users, places, plan)
                }

            } catch (e: Exception) {
                _error.value = e.message
                _emptyStateMessage.value = "Ошибка загрузки данных"

                refreshData()
            } finally {
                _loading.value = false
            }
        }
    }

    private fun updateTable(users: List<User>, places: List<DutyPlace>, plan: DutyPlanResponse) {
        when {
            users.isEmpty() -> {
                _emptyStateMessage.value = "Пользователи не добавлены"
                _tableData.value = null
            }
            plan.duties.isEmpty() || places.isEmpty() -> {
                _tableData.value = createEmptyTable(users, plan.year, plan.month)
            }
            else -> {
                _tableData.value = buildDutyTable(users, places, plan.duties, plan.year, plan.month)
            }
        }

    }

    fun setEditMode(enabled: Boolean) {
        if (enabled) {
            // При включении режима редактирования сохраняем текущие данные как оригинальные
            _tableData.value?.let { currentData ->
                originalTableData = Array(currentData.third.size) { i ->
                    currentData.third[i].copyOf()
                }
                Log.d("ViewModel", "Saved original data for editing")
            }

            originalUsers = _users.value
            originalPlaces = _places.value

            Log.d("originalPlaces", originalPlaces.toString())
        }
        _isEditMode.value = enabled
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value!!
    }

    override fun toggleRowSelection(position: Int) {
        _selectedRow.value = if (_selectedRow.value == position) -1 else position
    }

    fun updateCellValue(row: Int, col: Int, newValue: String) {
        val current = _tableData.value ?: return.also {
            Log.e("ViewModel", "Current table data is null!")
        }

        Log.d("ViewModel", "Before update at [$row][$col]: ${current.third[row][col]}")

        val newTable = Array(current.third.size) { i ->
            current.third[i].copyOf()
        }
        newTable[row][col] = newValue

        Log.d("newTable", newTable.toString())

        Log.d("ViewModel", "After update at [$row][$col]: ${newTable[row][col]}")

        _tableData.value = Triple(
            current.first,
            current.second,
            newTable
        )
        Log.d("_tableData.value", _tableData.value.toString())
    }

    fun saveChanges() {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                _loading.value = true
                val changes = calculateChanges().also {
                    Log.d("ViewModel", "Changes to save: $it")
                }

                if (changes.isEmpty()) {
                    _isEditMode.value = false
                    return@launch
                }

                if (planRepository.saveDutyChanges(changes)) {
                    // Обновляем оригинальные данные только после успешного сохранения
                    originalTableData = _tableData.value?.third?.let {
                        Array(it.size) { i -> it[i].copyOf() }
                    } ?: emptyArray()
                    _isEditMode.value = false
                } else {
                    _error.value = "Ошибка сохранения"
                    _isEditMode.value = false
                }

            } catch (e: Exception) {
                _error.value = e.message
                _isEditMode.value = false
            } finally {
                _isSaving.value = false
                _loading.value = false
            }
        }
    }

    private fun calculateChanges(): List<DutyChange> {

        val currentData = _tableData.value ?: return emptyList()
        Log.d("currentData", currentData.toString())
        Log.e("ViewModel", currentData.toString())
        val planId = _planData.value?.id ?: return emptyList()
        Log.e("ViewModel", planId.toString())


        Log.d("ViewModel", "Original data rows: ${originalTableData.size}, cols: ${if (originalTableData.isNotEmpty()) originalTableData[0].size else 0}")
        Log.d("ViewModel", "Current data rows: ${currentData.third.size}, cols: ${if (currentData.third.isNotEmpty()) currentData.third[0].size else 0}")

        return currentData.third.flatMapIndexed { row, rowData ->
            rowData.mapIndexedNotNull { col, currentValue ->
                val originalValue = originalTableData.getOrNull(row)?.getOrNull(col) ?: "".also {
                    Log.w("ViewModel", "Original value not found at [$row][$col]")
                }
                if (originalValue != currentValue) {
                    Log.d("ViewModel", "Found change at [$row][$col]: '$originalValue' -> '$currentValue'")

                    Log.d("planId", planId.toString())
                    Log.d("userId", originalUsers[row].id.toString())
                    Log.d("date", (col + 1).toString())
                    Log.d("oldPlaceCode", originalValue.takeIf { it.isNotEmpty() }.toString())
                    Log.d("newPlaceCode", currentValue.takeIf { it.isNotEmpty() }.toString())

                    Log.d("originalUsers", originalUsers.toString())
                    Log.d("row", row.toString())

                    DutyChange(
                        planId = planId,
                        userId = originalUsers[row].id,
                        date = col + 1,
                        oldPlaceCode = if (originalValue.isEmpty()) null else originalValue,
                        newPlaceCode = if (currentValue.isEmpty()) null else currentValue
                    )
                }
                else null
            }
        }
    }

    fun saveState(): Bundle {
        return Bundle().apply {
            putBoolean("is_edit_mode", _isEditMode.value)
            putSerializable("original_table_data", originalTableData)
            _tableData.value?.let {
                putSerializable("current_table_data", it.third)
            }
        }
    }

    fun restoreState(savedState: Bundle) {
        isRestoringState = true
        savedState.getBoolean("is_edit_mode").let {
            _isEditMode.value = it
        }

        (savedState.getSerializable("original_table_data") as? Array<Array<String>>)?.let {
            originalTableData = Array(it.size) { i -> it[i].copyOf() }
        }

        (savedState.getSerializable("current_table_data") as? Array<Array<String>>)?.let { savedTable ->
            _tableData.value?.let { currentData ->
                _tableData.value = Triple(currentData.first, currentData.second, savedTable)
            }
        }
        isRestoringState = false
    }

    fun refreshData() {
        if (!isRestoringState) {
            loadCurrentPlan()
        }
    }
}
