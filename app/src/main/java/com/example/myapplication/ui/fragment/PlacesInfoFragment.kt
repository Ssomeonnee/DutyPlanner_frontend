package com.example.myapplication.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.data.model.DutyPlace
import com.example.myapplication.data.repository.DutyPlaceRepository
import com.example.myapplication.databinding.FragmentPlacesInfoBinding
import com.example.myapplication.di.PlacesInfoViewModelFactory
import com.example.myapplication.utils.ToastUtil
import com.example.myapplication.viewmodel.PlacesInfoViewModel
import kotlinx.coroutines.launch
import kotlin.getValue
import android.text.TextWatcher

class PlacesInfoFragment : DialogFragment() {
    private var _binding: FragmentPlacesInfoBinding? = null
    private val binding get() = _binding!!

    private var place: DutyPlace? = null
    private lateinit var mode: String

    private val viewModel: PlacesInfoViewModel by viewModels {
        PlacesInfoViewModelFactory(DutyPlaceRepository(requireContext()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_MyApplication_FullScreenDialog)
        place = arguments?.getParcelable("place")
        mode = arguments?.getString("mode").toString()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPlacesInfoBinding.inflate(inflater, container, false)
        // Инициализируем поля
        place?.let {
            populateUserData(place!!)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Инициализация основных элементов
        setupUI()
        observeViewModel()
        viewModel.initialize(place, mode)
    }

    private fun setupUI() {
        binding.errorNotificationCardView.visibility = View.GONE
        binding.iconBackImageView.setOnClickListener { dismiss() }
        setupFocusListeners()
        setupTextSync()

        binding.iconEditImageView.setOnClickListener {
            viewModel.switchToEditMode()
        }

        binding.saveButton.setOnClickListener {
            validateAndSave()
        }

        binding.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.toastMessage.collect { message ->
                ToastUtil.showCustomToast(requireContext(), message)
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is PlacesInfoViewModel.UiState.Loading -> {}//showLoading(true)
                    is PlacesInfoViewModel.UiState.ViewMode -> {
                        setupViewMode()
                    }
                    is PlacesInfoViewModel.UiState.EditMode -> {
                        setupEditMode()
                    }
                    is PlacesInfoViewModel.UiState.CreateMode -> {
                        setupCreateMode()
                    }
                    is PlacesInfoViewModel.UiState.Success -> {
                        dismissWithResult()
                    }
                }
            }
        }
    }

    private fun validateAndSave() {
        with(binding) {
            val name = nameEditText.text.toString()
            val shortName = shortNameEditText.text.toString()

            if (shortName.isEmpty() || name.isEmpty()) {
                ToastUtil.showCustomToast(requireContext(), "Все поля должны быть заполнены")
                return
            }
            viewModel.saveDutyPlace(name, shortName)
        }
    }

    private fun showDeleteConfirmationDialog() {
        DeleteDialogFragment.newInstance(
            title = "Удалить "+ place?.name +"?",
            message = "Место будет удалено из списка мест дежурств и графика на текущий месяц",
            deleteName = "Удалить",
            onDeleteConfirmed = { viewModel.deleteDutyPlace() }
        ).show(parentFragmentManager, "DeleteDialog")
    }

    private fun dismissWithResult() {
        setFragmentResult("place_updated", Bundle())
        dismiss()
    }

    private fun setupViewMode() {
        with(binding) {
            setEditTextEnabled(false)
            iconEditImageView.visibility = View.VISIBLE
            saveButton.visibility = View.GONE
            deleteButton.visibility = View.VISIBLE
        }
    }

    private fun setupEditMode() {
        with(binding) {
            setEditTextEnabled(true)
            iconEditImageView.visibility = View.INVISIBLE
            saveButton.visibility = View.VISIBLE
            deleteButton.visibility = View.GONE
        }
    }

    private fun setupCreateMode() {
        with(binding) {
            setEditTextEnabled(true)
            iconEditImageView.visibility = View.INVISIBLE
            saveButton.visibility = View.VISIBLE
            deleteButton.visibility = View.GONE
        }
    }

    private fun populateUserData(place: DutyPlace) {
        with(binding) {
            nameEditText.setText(place.name)
            shortNameEditText.setText(place.shortName)
        }
    }

    private fun setEditTextEnabled(enabled: Boolean) {
        listOf(
            binding.nameEditText,
            binding.shortNameEditText
        ).forEach { it.isEnabled = enabled }
    }

    private fun setupFocusListeners() {
        val iconMap = mapOf(
            binding.nameEditText to binding.nameIcon,
            binding.shortNameEditText to binding.shortNameIcon
        )

        iconMap.forEach { (editText, icon) ->
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.blue))
                } else {
                    icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray))
                }
            }
        }
    }

    private fun setupTextSync() {

        binding.nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.isNotEmpty()) {
                        // Получаем первую букву и преобразуем в верхний регистр
                        val firstChar = it.toString().take(1).uppercase()
                        // Устанавливаем значение в поле сокращения
                        binding.shortNameEditText.setText(firstChar)
                    } else {
                        binding.shortNameEditText.text?.clear()
                    }
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // Установка размеров
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

            // Цвета системных баров
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.lightest_gray)
            navigationBarColor = ContextCompat.getColor(requireContext(), R.color.lightest_gray)

            // Для API 23+ - светлые иконки
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

            // Критически важные флаги
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
