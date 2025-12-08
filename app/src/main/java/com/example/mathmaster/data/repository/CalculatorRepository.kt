package com.example.mathmaster.repository

import com.example.mathmaster.database.CalculationHistoryDAO
import com.example.mathmaster.model.CalculationHistory
import kotlinx.coroutines.flow.Flow

class CalculatorRepository(private val historyDao: CalculationHistoryDAO) {
    fun getAllHistory(): Flow<List<CalculationHistory>> {
        return historyDao.getAllHistory()
    }

    suspend fun insertHistory(expression: String, result: String, calculatorType: String) {
        val history = CalculationHistory(
            expression = expression,
            result = result,
            calculatorType = calculatorType
        )
        historyDao.insertHistory(history)
    }

    suspend fun deleteHistory(history: CalculationHistory) {
        historyDao.deleteHistory(history)
    }

    suspend fun clearAllHistory() {
        historyDao.clearAllHistory()
    }

    fun getGraphHistory(): Flow<List<CalculationHistory>> {
        return historyDao.getGraphHistory()
    }

    suspend fun deleteGraphHistory() {
        historyDao.deleteGraphHistory()
    }
}