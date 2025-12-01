package com.example.mathmaster.data.repository

import com.example.mathmaster.model.CalculationHistory
import com.example.mathmaster.repository.CalculatorRepository
import kotlinx.coroutines.flow.Flow

class GraphRepository(private val calculatorRepository: CalculatorRepository) {

    fun getGraphHistory(): Flow<List<CalculationHistory>> {
        return calculatorRepository.getGraphHistory()
    }

    suspend fun saveGraph(function: String) {
        calculatorRepository.insertHistory(
            expression = function,
            result = "graph",
            calculatorType = "graph"
        )
    }

    suspend fun clearGraphHistory() {
        calculatorRepository.deleteGraphHistory()
    }
}