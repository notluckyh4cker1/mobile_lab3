package com.example.mathmaster.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mathmaster.R
import com.example.mathmaster.model.CalculationHistory
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val onDeleteClick: (CalculationHistory) -> Unit,
    private val onItemClick: (CalculationHistory) -> Unit
) : ListAdapter<CalculationHistory, HistoryAdapter.HistoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = getItem(position)
        holder.bind(historyItem)
    }

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val expressionText: TextView = itemView.findViewById(R.id.expressionText)
        private val resultText: TextView = itemView.findViewById(R.id.resultText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val deleteButton: View = itemView.findViewById(R.id.deleteButton)

        fun bind(history: CalculationHistory) {
            if (history.calculatorType == "graph") {
                expressionText.text = "График: ${history.expression}"
                resultText.text = "Нажмите для построения"
            } else {
                expressionText.text = history.expression
                resultText.text = "= ${history.result}"
            }

            val date = Date(history.timestamp)
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            dateText.text = dateFormat.format(date)

            itemView.setOnClickListener {
                onItemClick(history)
            }

            deleteButton.setOnClickListener {
                onDeleteClick(history)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CalculationHistory>() {
        override fun areItemsTheSame(oldItem: CalculationHistory, newItem: CalculationHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CalculationHistory, newItem: CalculationHistory): Boolean {
            return oldItem == newItem
        }
    }
}