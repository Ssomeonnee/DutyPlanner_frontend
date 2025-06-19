package com.example.myapplication.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityListBinding

abstract class ListActivity : AppCompatActivity() {
    protected lateinit var binding: ActivityListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupHandlers()
        setupNav()
        getData()
    }

    abstract fun setupNav()
    abstract fun setupHandlers()
    abstract fun getData()
    abstract override fun onNewIntent(intent: Intent?)
}