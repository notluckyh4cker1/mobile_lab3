package com.example.mathmaster.ui.graphs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mathmaster.R
import com.example.mathmaster.data.repository.GraphRepository
import com.example.mathmaster.database.AppDatabase
import com.example.mathmaster.databinding.FragmentGraphBinding
import com.example.mathmaster.repository.CalculatorRepository
import kotlinx.coroutines.launch
import net.objecthunter.exp4j.ExpressionBuilder

class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!
    private lateinit var graphViewModel: GraphViewModel
    private lateinit var colors: List<Pair<Int, String>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        colors = listOf(
            Color.parseColor("#FF0000") to getString(R.string.red),
            Color.parseColor("#6750A4") to getString(R.string.purple),
            Color.parseColor("#FFB02E") to getString(R.string.orange),
            Color.parseColor("#4CAF50") to getString(R.string.green),
            Color.parseColor("#2196F3") to getString(R.string.blue),
            Color.parseColor("#9C27B0") to getString(R.string.magenta)
        )

        val database = AppDatabase.getDatabase(requireContext())
        val calculatorRepository = CalculatorRepository(database.calculationHistoryDao())
        val graphRepository = GraphRepository(calculatorRepository)
        graphViewModel = GraphViewModel(graphRepository)

        setupColorSpinner()
        setupButtons()

        val functionFromHistory = arguments?.getString("functionFromHistory")
        if (!functionFromHistory.isNullOrEmpty()) {
            binding.functionInput.setText(functionFromHistory)
            val selectedColor = colors[0].first
            binding.graphView.plotFunction(functionFromHistory, selectedColor)
        }
    }

    private fun setupColorSpinner() {
        val colorNames = colors.map { it.second }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, colorNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.colorSpinner.adapter = adapter
    }

    private fun setupButtons() {
        binding.plotButton.setOnClickListener {
            val function = binding.functionInput.text.toString().trim()
            if (function.isNotEmpty()) {
                if (isValidFunction(function)) {
                    val selectedColor = colors[binding.colorSpinner.selectedItemPosition].first
                    binding.graphView.plotFunction(function, selectedColor)

                    lifecycleScope.launch {
                        graphViewModel.saveGraph(function)
                    }
                } else {
                    showFunctionError()
                }
            }
        }

        binding.clearButton.setOnClickListener {
            binding.graphView.clearGraph()
            binding.functionInput.text?.clear()
        }
    }

    private fun isValidFunction(function: String): Boolean {
        return try {
            ExpressionBuilder(function)
                .variables("x")
                .build()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun showFunctionError() {
        android.widget.Toast.makeText(
            requireContext(),
            getString(R.string.invalid_function),
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}