package com.example.myapplication.ui.adapter

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import androidx.core.content.ContextCompat

class EditableDutyPlanAdapter(
    private var data: Array<Array<String>>,
    private var validPlaceCodes: List<String>,
    private val onItemClick: (Int) -> Unit,
    private val onCellValueChanged: (Int, Int, String) -> Unit
) : RecyclerView.Adapter<EditableDutyPlanAdapter.CellViewHolder>() {

    private var selectedRow = -1
    private var isEditMode = false
    private var editingPosition: Int = -1
    private var hasInvalidInput = false

    fun setSelectedRow(rowPosition: Int) {
        // Проверяем, что массив не пустой
        if (data.isEmpty() || data[0].isEmpty()) {
            return
        }

        val prev = selectedRow
        selectedRow = rowPosition

        // Обновляем все ячейки в предыдущей и новой выделенной строке
        data[0].indices.forEach { col ->
            if (prev != -1) notifyItemChanged(prev * data[0].size + col)
            if (selectedRow != -1) notifyItemChanged(selectedRow * data[0].size + col)
        }
    }

    inner class CellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val editText: EditText = itemView.findViewById(R.id.editText)
        private val frameLayout: FrameLayout = itemView as FrameLayout
        private var currentValue: String = ""


        fun bind(cellValue: String, rowPosition: Int, position: Int) {
            currentValue = cellValue
            editText.setText(cellValue)
            updateTextColor(cellValue)

            editText.isFocusable = isEditMode
            editText.isFocusableInTouchMode = isEditMode
            editText.isClickable = isEditMode
            editText.isLongClickable = isEditMode

            // Очищаем все предыдущие слушатели
            editText.setOnFocusChangeListener(null)
            editText.setOnEditorActionListener(null)
            editText.removeTextChangedListener(textWatcher)
            frameLayout.setOnClickListener(null)

            // Новый TextWatcher для каждой ячейки
            editText.addTextChangedListener(textWatcher)

            if (isEditMode && editingPosition == position) {
                editText.post {
                    editText.requestFocus()
                    showKeyboard(editText)
                }
            }

            frameLayout.setOnClickListener {
                if (isEditMode) {
                    if (editingPosition == position) return@setOnClickListener
                    editingPosition = position
                    notifyItemChanged(position)
                } else {
                    onItemClick(rowPosition) // Просто передаем номер строки
                }
            }

            updateSelection(rowPosition)

            editText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus && isEditMode && editingPosition == position) {

                    val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE)
                            as InputMethodManager
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)

                    val newValue = editText.text.toString().take(1).uppercase()
                    if (newValue != currentValue) {
                        val row = position / data[0].size
                        val col = position % data[0].size
                        Log.d("Adapter", "Value changed: [$row][$col] = '$newValue'")

                        currentValue = newValue
                        onCellValueChanged(row, col, newValue)
                    }
                }
            }

            // Добавляем обработчик клика по корневому View
            frameLayout.setOnClickListener {
                if (isEditMode) {
                    if (editingPosition == position) {
                        // Клик по уже редактируемой ячейке - ничего не делаем
                        return@setOnClickListener
                    }
                    editingPosition = position
                    notifyItemChanged(position)
                } else {
                    onItemClick(if (rowPosition == selectedRow) -1 else rowPosition)
                }
            }

            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    editText.clearFocus()
                    true
                } else false
            }
        }

        private val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val newText = it.toString().take(1).uppercase()
                    if (newText != currentValue) {
                        val row = adapterPosition / data[0].size
                        val col = adapterPosition % data[0].size
                        data[row][col] = newText
                        currentValue = newText
                        onCellValueChanged(row, col, newText)
                        updateTextColor(newText)

                        // Закрываем клавиатуру после ввода
                        editText.post {
                            editText.clearFocus()
                            val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE)
                                    as InputMethodManager
                            imm.hideSoftInputFromWindow(editText.windowToken, 0)
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        }

        private fun updateTextColor(value: String) {
            val isValid = value.isEmpty() || validPlaceCodes.contains(value)
            editText.setTextColor(
                ContextCompat.getColor(
                    editText.context,
                    if (isValid) R.color.black else R.color.red
                )
            )

            // Обновляем флаг недопустимых значений
            if (!isValid) {
                hasInvalidInput = true
            }
        }

        private fun updateSelection(rowPosition: Int) {
            frameLayout.background = if (rowPosition == selectedRow) {
                ContextCompat.getDrawable(frameLayout.context, R.drawable.dutyplan_selected_cell_bg)
            } else {
                ContextCompat.getDrawable(frameLayout.context, R.drawable.dutyplan_default_cell_bg)
            }
        }

        private fun showKeyboard(view: View) {
            view.postDelayed({
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)

                (itemView.parent as? RecyclerView)?.let { recycler ->
                    recycler.post {
                        recycler.smoothScrollToPosition(adapterPosition)
                    }
                }
            }, 100)
        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_duty_cell, parent, false)
        return CellViewHolder(view)
    }

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        if (data.isEmpty() || data[0].isEmpty()) return

        val row = position / data[0].size
        val col = position % data[0].size
        holder.bind(data[row][col], row, position)
    }

    override fun getItemCount(): Int = if (data.isEmpty() || data[0].isEmpty()) 0 else data.size * data[0].size

    fun updateData(newData: Array<Array<String>>) {
        if (newData.size != data.size || newData[0].size != data[0].size) {
            data = Array(newData.size) { i -> newData[i].copyOf() }
        } else {
            for (i in newData.indices) {
                System.arraycopy(newData[i], 0, data[i], 0, newData[i].size)
            }
        }
        notifyDataSetChanged()
    }

    fun setEditMode(enabled: Boolean, view: View? = null) {
        val wasEditMode = isEditMode
        isEditMode = enabled

        if (!enabled && wasEditMode) {
            editingPosition = -1
            // Закрываем клавиатуру при выходе из режима редактирования
            view?.let { closeKeyboard(it) }
        }

        notifyDataSetChanged()
    }

    fun updateValidPlaceCodes(newCodes: List<String>) {
        validPlaceCodes = newCodes
        notifyDataSetChanged()
    }

    fun hasInvalidInput(): Boolean {
        hasInvalidInput = false
        // Принудительно проверяем все ячейки
        notifyDataSetChanged()
        return hasInvalidInput
    }

    private fun closeKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}


