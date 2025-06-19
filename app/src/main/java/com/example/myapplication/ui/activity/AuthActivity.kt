package com.example.myapplication.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.databinding.ActivityAuthBinding
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import android.view.View
import androidx.activity.viewModels
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.di.AuthViewModelFactory
import com.example.myapplication.ui.activity.UserListActivity
import com.example.myapplication.utils.ToastUtil
import com.example.myapplication.viewmodel.AuthViewModel.AuthState

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(application)
    }
    private var isAdminMode = true
    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupInitialState()
        setupButtonListeners()
        setupFocusListeners()
    }

    private fun setupInitialState() {
        //binding.errorNotificationCardView.visibility = View.GONE
        binding.registerButton.visibility = View.GONE
        binding.confirmPasswordCardView.visibility = View.GONE
        binding.iconBackImageView.visibility = View.GONE
        selectAdminButton()
    }

    private fun setupButtonListeners() {
        binding.adminButton.setOnClickListener {
            if (!isAdminMode) {
                selectAdminButton()
                isAdminMode = true
                isRegisterMode = false
                updateUIForMode()
            }
        }

        binding.userButton.setOnClickListener {
            if (isAdminMode) {
                selectUserButton()
                isAdminMode = false
                isRegisterMode = false
                updateUIForMode()
            }
        }

        binding.enterButton.setOnClickListener {
            handleLogin()
        }

        binding.registerButton.setOnClickListener {
            handleRegister()
        }

        binding.registerTextView.setOnClickListener {
            isRegisterMode = !isRegisterMode
            updateUIForMode()
        }

        binding.iconBackImageView.setOnClickListener {
            isRegisterMode = !isRegisterMode
            updateUIForMode()
        }
    }

    private fun updateUIForMode() {
        if (isRegisterMode) {
            binding.registerButton.visibility = View.VISIBLE
            binding.confirmPasswordCardView.visibility = View.VISIBLE
            binding.enterButton.visibility = View.GONE
            binding.userButton.visibility = View.GONE
            binding.adminButton.visibility = View.GONE
            binding.iconBackImageView.visibility = View.VISIBLE
            binding.authTitleTextView.text = "Регистрация"
        } else {
            binding.registerButton.visibility = View.GONE
            binding.confirmPasswordCardView.visibility = View.GONE
            binding.enterButton.visibility = View.VISIBLE
            binding.userButton.visibility = View.VISIBLE
            binding.adminButton.visibility = View.VISIBLE
            binding.authTitleTextView.text = "Авторизация"
            binding.iconBackImageView.visibility = View.GONE
        }
    }

    private fun selectAdminButton() {
        binding.adminButton.apply {
            setBackgroundResource(R.drawable.rounded_button_blue)
            setTextColor(ContextCompat.getColor(this@AuthActivity, R.color.white))
        }
        binding.userButton.apply {
            setBackgroundResource(R.drawable.rounded_button_white)
            setTextColor(ContextCompat.getColor(this@AuthActivity, R.color.gray))
        }
        binding.registerTextView.visibility = View.VISIBLE
    }

    private fun selectUserButton() {
        binding.userButton.apply {
            setBackgroundResource(R.drawable.rounded_button_blue)
            setTextColor(ContextCompat.getColor(this@AuthActivity, R.color.white))
        }
        binding.adminButton.apply {
            setBackgroundResource(R.drawable.rounded_button_white)
            setTextColor(ContextCompat.getColor(this@AuthActivity, R.color.gray))
        }
        binding.registerTextView.visibility = View.INVISIBLE
    }

    private fun handleLogin() {
        val login = binding.loginEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        viewModel.login(login, password, isAdminMode)
    }

    private fun handleRegister() {
        val login = binding.loginEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        val confirmPassword = binding.confirmPasswordEditText.text.toString()
        viewModel.registerAdmin(login, password, confirmPassword)
    }

    private fun setupObservers() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> showLoading()
                is AuthState.Success -> handleAuthSuccess(state.isAdmin)
                is AuthState.Error -> ToastUtil.showCustomToast(this@AuthActivity, state.message)
                null -> hideLoading()
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun handleAuthSuccess(isAdmin: Boolean) {
        hideLoading()
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("is_admin", isAdmin)
        }
        startActivity(intent)
        finish()
    }

    private fun setupFocusListeners() {
        val iconMap = mapOf(
            binding.loginEditText to binding.loginIcon,
            binding.passwordEditText to binding.passwordIcon,
            binding.confirmPasswordEditText to binding.confirmPasswordIcon,
            binding.loginEditText to binding.loginIcon,
            binding.passwordEditText to binding.passwordIcon
        )

        iconMap.forEach { (editText, icon) ->
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    icon.setColorFilter(ContextCompat.getColor(this, R.color.blue))
                } else {
                    icon.setColorFilter(ContextCompat.getColor(this, R.color.gray))
                }
            }
        }
    }
}