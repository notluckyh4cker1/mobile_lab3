package com.example.mathmaster.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mathmaster.R
import com.example.mathmaster.database.AppDatabase
import com.example.mathmaster.databinding.FragmentHistoryBinding
import com.example.mathmaster.model.CalculationHistory
import com.example.mathmaster.repository.CalculatorRepository
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: CalculatorRepository
    private lateinit var historyAdapter: HistoryAdapter

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

        val database = AppDatabase.getDatabase(requireContext())
        repository = CalculatorRepository(database.calculationHistoryDao())

        setupRecyclerView()
        setupClickListeners()
        loadHistory()
        updateStatistics()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            onDeleteClick = { history ->
                lifecycleScope.launch {
                    repository.deleteHistory(history)
                    updateStatistics()
                }
            },
            onItemClick = { history ->
                navigateToCalculator(history)
            }
        )

        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun navigateToCalculator(history: CalculationHistory) {
        val navController = findNavController()

        when (history.calculatorType.lowercase()) {
            "engineering", "standard" -> {
                val bundle = Bundle().apply {
                    putString("expressionFromHistory", history.expression)
                    putBoolean("fromHistory", true)
                }
                // Без NavOptions - стандартная навигация
                navController.navigate(R.id.calculatorFragment, bundle)
            }
            "programmer" -> {
                val bundle = Bundle().apply {
                    putString("expressionFromHistory", history.expression)
                    putBoolean("fromHistory", true)
                }
                navController.navigate(R.id.programmerFragment, bundle)
            }
            "graph" -> {
                val bundle = Bundle().apply {
                    putString("functionFromHistory", history.expression)
                    putBoolean("fromHistory", true)
                }
                navController.navigate(R.id.graphsFragment, bundle)
            }
            else -> {
                val bundle = Bundle().apply {
                    putString("expressionFromHistory", history.expression)
                    putBoolean("fromHistory", true)
                }
                navController.navigate(R.id.calculatorFragment, bundle)
            }
        }
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.clearHistoryButton.setOnClickListener {
            lifecycleScope.launch {
                repository.clearAllHistory()
                updateStatistics()
            }
        }
    }

    private fun loadHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.getAllHistory().collect { historyList ->
                    historyAdapter.submitList(historyList)

                    if (historyList.isEmpty()) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.historyRecyclerView.visibility = View.GONE
                    } else {
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.historyRecyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun updateStatistics() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.getAllHistory().collect { historyList ->
                    val totalCalculations = historyList.size
                    binding.totalCalculationsText.text = totalCalculations.toString()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}