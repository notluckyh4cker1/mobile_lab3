package com.example.mathmaster.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mathmaster.repository.CalculatorRepository
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: CalculatorRepository) : ViewModel() {

    fun saveCalculation(expression: String, result: String, calculatorType: String) {
        viewModelScope.launch {
            repository.insertHistory(expression, result, calculatorType)
        }
    }
}