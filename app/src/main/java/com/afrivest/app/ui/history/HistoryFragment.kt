package com.afrivest.app.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.afrivest.app.databinding.FragmentHistoryBinding
import com.afrivest.app.ui.history.adapters.TransactionHistoryAdapter
import com.afrivest.app.ui.transactions.TransactionDetailActivity
import com.afrivest.app.utils.gone
import com.afrivest.app.utils.visible
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: TransactionHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterChips()
        setupObservers()
        setupSwipeRefresh()

        // Load initial data
        viewModel.loadTransactions()
    }

    private fun setupRecyclerView() {
        adapter = TransactionHistoryAdapter { transaction ->
            // Navigate to transaction detail
            val intent = Intent(requireContext(), TransactionDetailActivity::class.java).apply {
                putExtra("transaction", transaction)
            }
            startActivity(intent)
        }

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter
        }

        // Setup pagination
        binding.rvTransactions.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!viewModel.isLoading.value!! && !viewModel.isLastPage.value!!) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                        viewModel.loadMoreTransactions()
                    }
                }
            }
        })
    }

    private fun setupFilterChips() {
        // All filter
        binding.chipAll.setOnClickListener {
            selectChip(binding.chipAll)
            viewModel.filterTransactions(null)
        }

        // Success filter
        binding.chipSuccess.setOnClickListener {
            selectChip(binding.chipSuccess)
            viewModel.filterTransactions("success")
        }

        // Pending filter
        binding.chipPending.setOnClickListener {
            selectChip(binding.chipPending)
            viewModel.filterTransactions("pending")
        }

        // Failed filter
        binding.chipFailed.setOnClickListener {
            selectChip(binding.chipFailed)
            viewModel.filterTransactions("failed")
        }

        // Select "All" by default
        selectChip(binding.chipAll)
    }

    private fun selectChip(selectedChip: Chip) {
        // Deselect all chips
        binding.chipAll.isChecked = false
        binding.chipSuccess.isChecked = false
        binding.chipPending.isChecked = false
        binding.chipFailed.isChecked = false

        // Select the clicked chip
        selectedChip.isChecked = true
    }

    private fun setupObservers() {
        // Loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading && adapter.itemCount == 0) {
                binding.loadingOverlay.root.visible()
            } else {
                binding.loadingOverlay.root.gone()
            }
        }

        // Transactions
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions)

            // Show empty state if no transactions
            if (transactions.isEmpty()) {
                binding.emptyState.root.visible()
                binding.rvTransactions.gone()
            } else {
                binding.emptyState.root.gone()
                binding.rvTransactions.visible()
            }
        }

        // Error message
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshTransactions()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}