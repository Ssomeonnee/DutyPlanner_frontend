package com.example.myapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentDutyPlanBinding
import com.example.myapplication.ui.activity.PlaceListActivity
import com.example.myapplication.utils.ToastUtil
import com.example.myapplication.viewmodel.BaseDutyPlanViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseDutyPlanFragment<VM : BaseDutyPlanViewModel> : Fragment() {
    protected var _binding: FragmentDutyPlanBinding? = null
    protected val binding get() = _binding!!

    protected abstract val viewModel: VM // Абстрактное свойство для ViewModel

    protected var isSyncingScroll = false
    protected var isFragmentActive = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDutyPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isFragmentActive = true

        initAdapters()
        setupRecyclerViews()
        setupScrollSync()
        observeViewModel()
    }

    protected abstract fun initAdapters()

    protected abstract fun setupRecyclerViews()

    protected open fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tableData.collectLatest { data ->
               if (data == null) {
                    showEmptyState("")
                    return@collectLatest
                }

                val (users, dates, table) = data

                hideEmptyState()
                updateTableViews(users, dates, table)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.emptyStateMessage.collectLatest { message ->
                message?.let { showEmptyState(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collectLatest { message ->
                message?.let {ToastUtil.showCustomToast(requireContext(), it)}
            }
        }

        lifecycleScope.launch {
            viewModel.loading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    protected fun showEmptyState(message: String) {
        binding.run {
            datesRecyclerView.visibility = View.GONE
            usersRecyclerView.visibility = View.GONE
            dutyPlanHorizontalScroll.visibility = View.GONE
            dutyPlanRecyclerView.visibility = View.GONE
            emptyStateTextView.visibility = View.VISIBLE
            emptyStateTextView.text = message
        }
    }

    protected fun hideEmptyState() {
        binding.run {
            datesRecyclerView.visibility = View.VISIBLE
            usersRecyclerView.visibility = View.VISIBLE
            dutyPlanHorizontalScroll.visibility = View.VISIBLE
            dutyPlanRecyclerView.visibility = View.VISIBLE
            emptyStateTextView.visibility = View.GONE
        }
    }

    protected abstract fun updateTableViews(
        users: List<String>,
        dates: List<String>,
        table: Array<Array<String>>
    )

    private fun setupScrollSync() {
        binding.datesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!isSyncingScroll && isFragmentActive) {
                    isSyncingScroll = true
                    binding.dutyPlanHorizontalScroll.smoothScrollBy(dx, 0)
                    isSyncingScroll = false
                }
            }
        })

        binding.dutyPlanHorizontalScroll.viewTreeObserver.addOnScrollChangedListener {
            if (!isSyncingScroll && isFragmentActive) {
                isSyncingScroll = true
                val scrollX = binding.dutyPlanHorizontalScroll.scrollX
                binding.datesRecyclerView.scrollBy(
                    scrollX - binding.datesRecyclerView.computeHorizontalScrollOffset(),
                    0
                )
                isSyncingScroll = false
            }
        }

        binding.usersRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!isSyncingScroll && isFragmentActive) {
                    isSyncingScroll = true
                    binding.dutyPlanRecyclerView.scrollBy(0, dy)
                    isSyncingScroll = false
                }
            }
        })

        binding.dutyPlanRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!isSyncingScroll && isFragmentActive) {
                    isSyncingScroll = true
                    binding.usersRecyclerView.scrollBy(0, dy)
                    isSyncingScroll = false
                }
            }
        })
    }

    override fun onDestroyView() {
        isFragmentActive = false
        _binding = null
        super.onDestroyView()
    }
}