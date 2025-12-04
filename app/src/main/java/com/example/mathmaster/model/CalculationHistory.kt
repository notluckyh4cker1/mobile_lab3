package com.example.mathmaster.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis(),
    val calculatorType: String,
    val historyType: String = if (calculatorType == "graph") "graph" else "calculation"
) {
    fun isGraphHistory(): Boolean {
        return historyType == "graph"
    }
}