package com.example.myapplication.utils

import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import com.example.myapplication.R

object ToastUtil {

    fun showCustomToast(
        context: Context,
        message: String,
        duration: Int = Toast.LENGTH_LONG,
        gravity: Int = Gravity.TOP or Gravity.CENTER_HORIZONTAL,
        yOffset: Int = 100
    ) {
        // Создаем стандартный Toast
        val toast = Toast.makeText(context, message, duration)

        // Применяем наш кастомный стиль
        toast.view?.setBackgroundResource(R.drawable.custom_toast_background)

        // Настраиваем TextView внутри Toast
        val textView = toast.view?.findViewById(android.R.id.message) as? TextView
        textView?.let {
            it.setTextColor(context.getColor(R.color.white))
            it.textSize = 14f
        }

        // Позиционирование
        toast.setGravity(gravity, 0, yOffset)
        toast.show()
    }

    // Вариант для строковых ресурсов
    fun showCustomToast(
        context: Context,
        @StringRes messageRes: Int,
        duration: Int = Toast.LENGTH_LONG
    ) {
        showCustomToast(context, context.getString(messageRes), duration)
    }
}