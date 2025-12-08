package com.example.mathmaster.ui.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mathmaster.database.AppDatabase
import com.example.mathmaster.repository.CalculatorRepository
import com.example.mathmaster.ui.history.HistoryViewModel
import com.example.mathmaster.databinding.FragmentCalculatorBinding
import kotlin.math.*

class CalculatorFragment : Fragment() {
    private var _binding: FragmentCalculatorBinding? = null
    private val binding get() = _binding!!

    private var currentExpression = "0"
    private var currentResult = "0"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var historyViewModel: HistoryViewModel
    private var isFromHistory = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val repository = CalculatorRepository(database.calculationHistoryDao())
        historyViewModel = HistoryViewModel(repository)

        val expressionFromHistory = arguments?.getString("expressionFromHistory")
        if (!expressionFromHistory.isNullOrEmpty()) {
            currentExpression = expressionFromHistory
            isFromHistory = true
            calculateFromHistory()
        }

        setupClickListeners()
        updateDisplay()
    }

    private fun setupClickListeners() {
        // Numbers
        binding.button0.setOnClickListener { appendNumber("0") }
        binding.button1.setOnClickListener { appendNumber("1") }
        binding.button2.setOnClickListener { appendNumber("2") }
        binding.button3.setOnClickListener { appendNumber("3") }
        binding.button4.setOnClickListener { appendNumber("4") }
        binding.button5.setOnClickListener { appendNumber("5") }
        binding.button6.setOnClickListener { appendNumber("6") }
        binding.button7.setOnClickListener { appendNumber("7") }
        binding.button8.setOnClickListener { appendNumber("8") }
        binding.button9.setOnClickListener { appendNumber("9") }

        // Basic Operations
        binding.buttonAdd.setOnClickListener { appendOperation("+") }
        binding.buttonSubtract.setOnClickListener { appendOperation("-") }
        binding.buttonMultiply.setOnClickListener { appendOperation("*") }
        binding.buttonDivide.setOnClickListener { appendOperation("/") }

        // Functions
        binding.buttonDecimal.setOnClickListener { appendDecimal() }
        binding.buttonOpenBracket.setOnClickListener { appendOperation("(") }
        binding.buttonCloseBracket.setOnClickListener { appendOperation(")") }
        binding.buttonClear.setOnClickListener { clear() }
        binding.buttonEquals.setOnClickListener { calculate() }
        binding.buttonDelete.setOnClickListener { deleteLast() }

        // Scientific Functions
        binding.buttonSin.setOnClickListener { appendTrigFunction("sin(") }
        binding.buttonCos.setOnClickListener { appendTrigFunction("cos(") }
        binding.buttonTan.setOnClickListener { appendTrigFunction("tan(") }
        binding.buttonLog.setOnClickListener { appendFunction("log(") }
        binding.buttonLn.setOnClickListener { appendFunction("ln(") }
        binding.buttonSqrt.setOnClickListener { appendFunction("sqrt(") }
        binding.buttonPower.setOnClickListener { appendOperation("^") }
        binding.buttonPi.setOnClickListener { appendConstant("π") }
        binding.buttonE.setOnClickListener { appendConstant("e") }
    }

    private fun appendNumber(number: String) {
        if (currentExpression == "0") {
            currentExpression = number
        } else {
            currentExpression += number
        }
        currentResult = "0"
        isFromHistory = false
        updateDisplay()
    }

    private fun appendOperation(operation: String) {
        if (currentResult != "0" && currentResult != "Error") {
            currentExpression = currentResult + operation
            currentResult = "0"
        } else {
            currentExpression += operation
        }
        isFromHistory = false
        updateDisplay()
    }

    private fun appendFunction(function: String) {
        if (currentResult != "0" && currentResult != "Error") {
            currentExpression = function + currentResult + ")"
            currentResult = "0"
        } else {
            currentExpression = if (currentExpression == "0") {
                function
            } else {
                currentExpression + function
            }
        }
        updateDisplay()
    }

    private fun appendDecimal() {
        if (!currentExpression.contains('.')) {
            currentExpression += "."
            updateDisplay()
        }
    }

    private fun deleteLast() {
        if (currentExpression.length > 1) {
            currentExpression = currentExpression.substring(0, currentExpression.length - 1)
        } else {
            currentExpression = "0"
        }
        updateDisplay()
    }

    private fun clear() {
        currentExpression = "0"
        currentResult = "0"
        updateDisplay()
    }

    private fun calculateFromHistory() {
        try {
            val result = evaluateExpression(currentExpression)
            currentResult = result
            updateDisplay()
        } catch (e: Exception) {
            currentResult = "Error"
            updateDisplay()
        }
    }

    private fun calculate() {
        try {
            val result = evaluateExpression(currentExpression)
            currentResult = result
            updateDisplay()

            if (result != "Error") {
                historyViewModel.saveCalculation(
                    expression = currentExpression,
                    result = result,
                    calculatorType = "engineering"
                )
            }

        } catch (e: Exception) {
            currentResult = "Error"
            updateDisplay()
        }
    }

    private fun appendTrigFunction(function: String) {
        if (currentResult != "0" && currentResult != "Error") {
            currentExpression = function + currentResult + ")"
            currentResult = "0"
        } else {
            currentExpression = if (currentExpression == "0") {
                function
            } else {
                val lastChar = currentExpression.lastOrNull()
                if (lastChar != null && lastChar.isDigit()) {
                    currentExpression + "*" + function
                } else {
                    currentExpression + function
                }
            }
        }
        updateDisplay()
    }

    private fun evaluateExpression(expression: String): String {
        return try {
            var processedExpr = expression

            processedExpr = processScientificFunctions(processedExpr)

            processedExpr = processedExpr
                .replace("π", Math.PI.toString())
                .replace("e", Math.E.toString())

            val result = eval(processedExpr)

            when {
                result.isNaN() -> "Error: Undefined"
                result.isInfinite() -> "Error: Infinity"
                else -> formatResult(result)
            }
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun processScientificFunctions(expression: String): String {
        var processed = expression

        val trigFunctionRegex = "(sin|cos|tan)\\(([^()]+)\\)".toRegex()

        while (true) {
            val match = trigFunctionRegex.find(processed) ?: break
            val functionName = match.groupValues[1]
            val argumentStr = match.groupValues[2]

            try {
                var tempArg = argumentStr
                    .replace("π", Math.PI.toString())
                    .replace("e", Math.E.toString())

                val argumentValue = eval(tempArg)

                // cos(90, 270, 450, ...) - возвращаем 0 сразу
                if (functionName == "cos" && argumentValue % 90 == 0.0 && argumentValue % 180 != 0.0) {
                    processed = processed.replace(match.value, "0")
                    continue
                }

                // sin(180, 360, ...) - возвращаем 0 сразу
                if (functionName == "sin" && argumentValue % 180 == 0.0) {
                    processed = processed.replace(match.value, "0")
                    continue
                }

                // tan(180, 360, ...) - возвращаем 0 сразу
                if (functionName == "tan" && argumentValue % 180 == 0.0) {
                    processed = processed.replace(match.value, "0")
                    continue
                }

                // tan(90, 270, ...) - возвращаем Error сразу
                if (functionName == "tan" && argumentValue % 180 == 90.0) {
                    processed = processed.replace(match.value, "NaN")
                    continue
                }

                val radians = argumentValue * Math.PI / 180.0
                val functionResult = when (functionName) {
                    "sin" -> sin(radians)
                    "cos" -> cos(radians)
                    "tan" -> {
                        if (argumentValue % 180.0 == 90.0) {
                            Double.NaN
                        } else {
                            tan(radians)
                        }
                    }
                    else -> throw RuntimeException("Unknown function: $functionName")
                }

                processed = processed.replace(match.value, functionResult.toString())
            } catch (e: Exception) {
                throw RuntimeException("Error in $functionName")
            }
        }

        val functionRegex = "(log|ln|sqrt)\\(([^()]+)\\)".toRegex()

        while (true) {
            val match = functionRegex.find(processed) ?: break
            val functionName = match.groupValues[1]
            val argumentStr = match.groupValues[2]

            try {
                var tempArg = argumentStr
                    .replace("π", Math.PI.toString())
                    .replace("e", Math.E.toString())

                val argumentValue = eval(tempArg)
                val functionResult = when (functionName) {
                    "log" -> log10(argumentValue)
                    "ln" -> ln(argumentValue)
                    "sqrt" -> sqrt(argumentValue)
                    else -> throw RuntimeException("Unknown function: $functionName")
                }

                processed = processed.replace(match.value, functionResult.toString())
            } catch (e: Exception) {
                throw RuntimeException("Error in $functionName")
            }
        }

        return processed
    }

    private fun appendConstant(constant: String) {
        val constantValue = when (constant) {
            "π" -> "π"
            "e" -> "e"
            else -> constant
        }

        if (currentExpression == "0") {
            currentExpression = constantValue
        } else {
            val lastChar = currentExpression.lastOrNull()
            if (lastChar != null && lastChar.isDigit()) {
                currentExpression += "*" + constantValue
            } else {
                currentExpression += constantValue
            }
        }
        currentResult = "0"
        updateDisplay()
    }

    private fun formatResult(result: Double): String {
        return if (result % 1 == 0.0) {
            result.toInt().toString()
        } else {
            var formatted = String.format("%.10f", result)
                .trimEnd('0')
                .trimEnd('.')

            if (formatted.isEmpty()) "0" else formatted
        }
    }

    private fun eval(expression: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> x += parseTerm()
                        eat('-'.code) -> x -= parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> x *= parseFactor()
                        eat('/'.code) -> x /= parseFactor()
                        eat('^'.code) -> x = x.pow(parseFactor())
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Double
                val startPos = pos
                when {
                    eat('('.code) -> {
                        x = parseExpression()
                        eat(')'.code)
                    }
                    ch >= '0'.code && ch <= '9'.code || ch == '.'.code -> {
                        while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                        x = expression.substring(startPos, pos).toDouble()
                    }
                    else -> throw RuntimeException("Unexpected: " + ch.toChar())
                }

                return x
            }
        }.parse()
    }

    private fun updateDisplay() {
        binding.expressionTextView.text = currentExpression
        binding.resultTextView.text = currentResult
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}