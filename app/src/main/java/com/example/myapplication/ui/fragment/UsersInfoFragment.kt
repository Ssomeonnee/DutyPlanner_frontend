package com.example.myapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResult
import com.example.myapplication.R
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.model.User
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.databinding.FragmentUsersInfoBinding
import com.example.myapplication.ui.viewmodel.UsersInfoViewModel
import com.example.myapplication.di.UsersInfoViewModelFactory
import com.example.myapplication.utils.ToastUtil
import com.example.myapplication.ui.viewmodel.UsersInfoViewModel.UiState
import kotlinx.coroutines.launch

class UsersInfoFragment : DialogFragment() {
    private var _binding: FragmentUsersInfoBinding? = null
    private val binding get() = _binding!!

    private var user: User? = null
    private lateinit var mode: String

    private val viewModel: UsersInfoViewModel by viewModels {
        UsersInfoViewModelFactory(UserRepository(requireContext()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_MyApplication_FullScreenDialog)
        user = arguments?.getParcelable("user")
        mode = arguments?.getString("mode").toString()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUsersInfoBinding.inflate(inflater, container, false)
        // Инициализируем поля
        user?.let {
            populateUserData(user!!)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        viewModel.initialize(user, mode)
    }

    private fun setupUI() {
        binding.errorNotificationCardView.visibility = View.GONE
        binding.iconBackImageView.setOnClickListener { dismiss() }
        setupFocusListeners()

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
                    is UiState.Loading -> {}
                    is UiState.ViewMode -> {
                        setupViewMode()
                    }
                    is UiState.EditMode -> {
                        setupEditMode()
                    }
                    is UiState.CreateMode -> {
                        setupCreateMode()
                    }
                    is UiState.Success -> {
                        dismissWithResult()
                    }
                }
            }
        }
    }

    private fun validateAndSave() {

        with(binding) {
            val surname = surnameEditText.text.toString()
            val name = nameEditText.text.toString()
            val patronymic = patronymicEditText.text.toString()
            val email = emailEditText.text.toString()
            val login = loginEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (viewModel.uiState== UiState.CreateMode){
                if (surname.isEmpty() || name.isEmpty() || patronymic.isEmpty() ||
                    login.isEmpty() || password.isEmpty() || email.isEmpty()) {

                    ToastUtil.showCustomToast(requireContext(), "Все поля должны быть заполнены")
                    return
                }
            } else {
                if (surname.isEmpty() || name.isEmpty() || patronymic.isEmpty() ||
                    login.isEmpty() || email.isEmpty()) {

                    ToastUtil.showCustomToast(requireContext(), "Все поля должны быть заполнены")
                    return
                }
            }

            // Проверка длины пароля (например, от 6 до 30 символов)
            if (!password.isEmpty() && (password.length < 6 || password.length > 30)) {
                ToastUtil.showCustomToast(requireContext(), "Пароль должен быть не менее 6 символов")
                return
            }

            // Проверка на английские символы в логине
            if (!login.matches(Regex("^[a-zA-Z0-9]+$"))) {
                ToastUtil.showCustomToast(requireContext(), "Логин содержит английские буквы и цифры")
                return
            }

            // Проверка на английские символы в пароле (можно добавить спецсимволы)
            if (!password.isEmpty() && !password.matches(Regex("^[a-zA-Z0-9]+$"))) {
                ToastUtil.showCustomToast(requireContext(), "Пароль содержит английские буквы и цифры")
                return
            }

            viewModel.saveUser(surname, name, patronymic, email, login, password)
        }
    }

    private fun showDeleteConfirmationDialog() {
        DeleteDialogFragment.newInstance(
            title = "Удалить "+ user?.getSurnameWithInitials() +"?",
            message = "Пользователь будет удален из списка сотрудников и графика на текущий месяц",
            deleteName = "Удалить",
            onDeleteConfirmed = { viewModel.deleteUser() }
        ).show(parentFragmentManager, "DeleteDialog")
    }

    private fun dismissWithResult() {
        setFragmentResult("user_updated", Bundle())
        dismiss()
    }

    private fun setupViewMode() {
        with(binding) {
            setEditTextEnabled(false)
            iconEditImageView.visibility = View.VISIBLE
            saveButton.visibility = View.GONE
            deleteButton.visibility = View.VISIBLE
            passwordCardView.visibility = View.GONE
        }
    }

    private fun setupEditMode() {
        with(binding) {
            setEditTextEnabled(true)
            iconEditImageView.visibility = View.INVISIBLE
            saveButton.visibility = View.VISIBLE
            deleteButton.visibility = View.GONE
            passwordCardView.visibility = View.VISIBLE
            passwordEditText.hint = "Сбросить пароль"
        }
    }

    private fun setupCreateMode() {
        with(binding) {
            setEditTextEnabled(true)
            iconEditImageView.visibility = View.INVISIBLE
            saveButton.visibility = View.VISIBLE
            deleteButton.visibility = View.GONE
            passwordEditText.hint = "Пароль"
        }
    }

    private fun populateUserData(user: User) {
        with(binding) {
            surnameEditText.setText(user.surname)
            nameEditText.setText(user.name)
            patronymicEditText.setText(user.patronymic)
            emailEditText.setText(user.email)
            loginEditText.setText(user.login)
            //passwordEditText.setText(user.password)
        }
    }

    private fun setEditTextEnabled(enabled: Boolean) {
        listOf(
            binding.surnameEditText,
            binding.nameEditText,
            binding.patronymicEditText,
            binding.emailEditText,
            binding.loginEditText,
            binding.passwordEditText
        ).forEach { it.isEnabled = enabled }
    }

    private fun setupFocusListeners() {
        val iconMap = mapOf(
            binding.surnameEditText to binding.surnameIcon,
            binding.nameEditText to binding.nameIcon,
            binding.patronymicEditText to binding.patronymicIcon,
            binding.emailEditText to binding.emailIcon,
            binding.loginEditText to binding.loginIcon,
            binding.passwordEditText to binding.passwordIcon
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

