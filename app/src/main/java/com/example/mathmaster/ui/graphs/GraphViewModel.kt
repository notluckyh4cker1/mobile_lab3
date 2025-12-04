package com.example.mathmaster.ui.graphs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mathmaster.data.repository.GraphRepository
import com.example.mathmaster.model.CalculationHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class GraphViewModel(private val repository: GraphRepository) : ViewModel() {

    fun saveGraph(function: String) {
        viewModelScope.launch {
            repository.saveGraph(function)
        }
    }

    fun getGraphHistory(): Flow<List<CalculationHistory>> {
        return repository.getGraphHistory()
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearGraphHistory()
        }
    }
}