package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.data.model.Duty
import com.example.myapplication.data.model.DutyPlace
import com.example.myapplication.data.model.DutyPlan
import com.example.myapplication.data.model.User
import com.example.myapplication.data.repository.DutyPlaceRepository
import com.example.myapplication.data.repository.DutyPlanRepository
import com.example.myapplication.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import android.util.Log
import com.example.myapplication.data.model.DutyPlanResponse
import java.util.Calendar

abstract class BaseDutyPlanViewModel : ViewModel() {
    protected val _loading = MutableStateFlow(false)
    protected val _error = MutableStateFlow<String?>(null)
    protected val _tableData = MutableStateFlow<Triple<List<String>, List<String>, Array<Array<String>>>?>(null)
    protected open val _selectedRow = MutableStateFlow(-1)
    val _emptyStateMessage = MutableStateFlow<String?>(null)

    val loading: StateFlow<Boolean> = _loading
    val error: StateFlow<String?> = _error
    val tableData: StateFlow<Triple<List<String>, List<String>, Array<Array<String>>>?> = _tableData
    open val selectedRow: StateFlow<Int> = _selectedRow
    val emptyStateMessage: StateFlow<String?> = _emptyStateMessage


    protected fun createEmptyTable(users: List<User>, year: Int, month: Int): Triple<List<String>, List<String>, Array<Array<String>>> {
        val daysInMonth = getDaysInMonth(year, month)
        val dates = (1..daysInMonth).map { it.toString() }
        val users = users //.sortedBy { it.surname }

        return Triple(
            users.map { it.getSurnameWithInitials() },
            dates,
            Array(users.size) { Array(dates.size) { "" } }
        )
    }

    protected fun buildDutyTable(users: List<User>, places: List<DutyPlace>, duties: List<Duty>, year: Int, month: Int): Triple<List<String>, List<String>, Array<Array<String>>> {
        val daysInMonth = getDaysInMonth(year, month)
        val dates = (1..daysInMonth).map { it.toString() }
        val users = users //.sortedBy { it.surname ?: "" }

        return Triple(
            users.map { it.getSurnameWithInitials() },
            dates,
            Array(users.size) { userIndex ->
                Array(dates.size) { dateIndex ->
                    duties.find { duty ->
                        duty.userId == users[userIndex].id && duty.date == dateIndex + 1
                    }?.let { duty ->
                        places.find { it.id == duty.dutyPlaceId }?.shortName ?: "?"
                    } ?: ""
                }
            }
        )
    }

    private fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, 1)
        }
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    open fun toggleRowSelection(position: Int) {
        viewModelScope.launch {
            _selectedRow.emit(if (_selectedRow.value == position) -1 else position)
        }
    }

}