package com.example.mathmaster.ui.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mathmaster.databinding.FragmentProgrammerBinding
import com.example.mathmaster.database.AppDatabase
import com.example.mathmaster.repository.CalculatorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProgrammerFragment : Fragment() {

    private var _binding: FragmentProgrammerBinding? = null
    private val binding get() = _binding!!

    private var currentInput = "0"
    private var currentExpression = "0"
    private var currentResult = "0"
    private var currentBase = 10

    // Для сохранения в историю
    private lateinit var repository: CalculatorRepository
    private var isFromHistory = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgrammerBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var wasFromHistory = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        repository = CalculatorRepository(database.calculationHistoryDao())

        val fromHistory = arguments?.getBoolean("fromHistory", false) ?: false
        wasFromHistory = fromHistory
        val expressionFromHistory = arguments?.getString("expressionFromHistory")

        if (fromHistory && !expressionFromHistory.isNullOrEmpty()) {
            val (cleanExpression, base) = extractExpressionAndBaseFromHistory(expressionFromHistory)
            currentInput = cleanExpression
            currentExpression = cleanExpression
            currentBase = base

            calculateAndSetResultFromHistory()
        }

        setupClickListeners()
        updateDisplay()
        updateButtonAvailability()
        updateBaseButtonHighlight()
    }

    override fun onResume() {
        super.onResume()

        if (wasFromHistory) {
            wasFromHistory = false
            arguments?.clear()
        }
    }

    private fun setupClickListeners() {
        // Цифры 0-9
        binding.button0.setOnClickListener { appendDigit("0") }
        binding.button1.setOnClickListener { appendDigit("1") }
        binding.button2.setOnClickListener { appendDigit("2") }
        binding.button3.setOnClickListener { appendDigit("3") }
        binding.button4.setOnClickListener { appendDigit("4") }
        binding.button5.setOnClickListener { appendDigit("5") }
        binding.button6.setOnClickListener { appendDigit("6") }
        binding.button7.setOnClickListener { appendDigit("7") }
        binding.button8.setOnClickListener { appendDigit("8") }
        binding.button9.setOnClickListener { appendDigit("9") }

        // Шестнадцатеричные цифры A-F
        binding.buttonA.setOnClickListener { appendDigit("A") }
        binding.buttonB.setOnClickListener { appendDigit("B") }
        binding.buttonC.setOnClickListener { appendDigit("C") }
        binding.buttonD.setOnClickListener { appendDigit("D") }
        binding.buttonE.setOnClickListener { appendDigit("E") }
        binding.buttonF.setOnClickListener { appendDigit("F") }

        // Системы счисления
        binding.buttonHex.setOnClickListener {
            changeBase(16)
            updateBaseButtonHighlight()
        }
        binding.buttonDec.setOnClickListener {
            changeBase(10)
            updateBaseButtonHighlight()
        }
        binding.buttonOct.setOnClickListener {
            changeBase(8)
            updateBaseButtonHighlight()
        }
        binding.buttonBin.setOnClickListener {
            changeBase(2)
            updateBaseButtonHighlight()
        }

        // Операции
        binding.buttonDelete.setOnClickListener { deleteLast() }
        binding.buttonAdd.setOnClickListener { appendOperation("+") }
        binding.buttonSubtract.setOnClickListener { appendOperation("-") }
        binding.buttonMultiply.setOnClickListener { appendOperation("*") }
        binding.buttonDivide.setOnClickListener { appendOperation("/") }
        binding.buttonEquals.setOnClickListener { calculate() }
        binding.buttonOpenBracket.setOnClickListener { appendOperation("(") }
        binding.buttonCloseBracket.setOnClickListener { appendOperation(")") }
        binding.buttonClear.setOnClickListener { clearAll() }
    }

    private fun updateBaseButtonHighlight() {
        val isDarkTheme = isDarkTheme()

        val unselectedTextColor = if (isDarkTheme) {
            ContextCompat.getColor(requireContext(), android.R.color.white)
        } else {
            ContextCompat.getColor(requireContext(), android.R.color.black)
        }

        val selectedBgColor = ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light)
        val selectedTextColor = ContextCompat.getColor(requireContext(), android.R.color.white)
        val defaultBgColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)

        binding.buttonHex.setBackgroundColor(defaultBgColor)
        binding.buttonHex.setTextColor(unselectedTextColor)

        binding.buttonDec.setBackgroundColor(defaultBgColor)
        binding.buttonDec.setTextColor(unselectedTextColor)

        binding.buttonOct.setBackgroundColor(defaultBgColor)
        binding.buttonOct.setTextColor(unselectedTextColor)

        binding.buttonBin.setBackgroundColor(defaultBgColor)
        binding.buttonBin.setTextColor(unselectedTextColor)

        when (currentBase) {
            16 -> {
                binding.buttonHex.setBackgroundColor(selectedBgColor)
                binding.buttonHex.setTextColor(selectedTextColor)
            }
            10 -> {
                binding.buttonDec.setBackgroundColor(selectedBgColor)
                binding.buttonDec.setTextColor(selectedTextColor)
            }
            8 -> {
                binding.buttonOct.setBackgroundColor(selectedBgColor)
                binding.buttonOct.setTextColor(selectedTextColor)
            }
            2 -> {
                binding.buttonBin.setBackgroundColor(selectedBgColor)
                binding.buttonBin.setTextColor(selectedTextColor)
            }
        }
    }

    private fun isDarkTheme(): Boolean {
        val configuration = resources.configuration
        val currentNightMode = configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    private fun appendDigit(digit: String) {
        if (currentInput == "0" || isFromHistory) {
            currentInput = digit
            currentExpression = digit
            isFromHistory = false
        } else {
            currentInput += digit
            currentExpression += digit
        }
        currentResult = "0"
        updateDisplay()
        convertAndDisplay()
    }

    private fun appendOperation(operation: String) {
        currentInput += operation
        currentExpression += operation
        isFromHistory = false
        updateDisplay()
    }

    private fun changeBase(newBase: Int) {
        if (currentBase != newBase) {
            try {
                val number = currentInput.toLongOrNull(currentBase) ?: 0
                currentInput = when (newBase) {
                    2 -> number.toString(2)
                    8 -> number.toString(8)
                    10 -> number.toString(10)
                    16 -> number.toString(16).uppercase()
                    else -> number.toString(10)
                }
                currentExpression = currentInput
                currentBase = newBase
                updateButtonAvailability()
                updateDisplay()
                convertAndDisplay()
            } catch (e: Exception) {
                currentInput = "0"
                currentExpression = "0"
                updateDisplay()
            }
        }
    }

    private fun convertAndDisplay() {
        try {
            val number = currentInput.toLongOrNull(currentBase) ?: 0

            binding.textHexResult.text = "HEX: ${number.toString(16).uppercase()}"
            binding.textDecResult.text = "DEC: ${number.toString(10)}"
            binding.textOctResult.text = "OCT: ${number.toString(8)}"
            binding.textBinResult.text = "BIN: ${number.toString(2)}"
        } catch (e: Exception) {
            binding.textHexResult.text = "HEX: Error"
            binding.textDecResult.text = "DEC: Error"
            binding.textOctResult.text = "OCT: Error"
            binding.textBinResult.text = "BIN: Error"
        }
    }

    private fun updateButtonAvailability() {
        val digits = listOf(
            binding.button0, binding.button1, binding.button2, binding.button3,
            binding.button4, binding.button5, binding.button6, binding.button7,
            binding.button8, binding.button9
        )

        val hexDigits = listOf(
            binding.buttonA, binding.buttonB, binding.buttonC,
            binding.buttonD, binding.buttonE, binding.buttonF
        )

        when (currentBase) {
            2 -> { // BIN - только 0-1
                digits.forEachIndexed { index, button ->
                    val isEnabled = index <= 1 // 0-1
                    button.isEnabled = isEnabled
                    button.alpha = if (isEnabled) 1f else 0.3f
                }
                hexDigits.forEach { button ->
                    button.isEnabled = false
                    button.alpha = 0.3f
                }
            }
            8 -> { // OCT - только 0-7
                digits.forEachIndexed { index, button ->
                    val isEnabled = index <= 7 // 0-7
                    button.isEnabled = isEnabled
                    button.alpha = if (isEnabled) 1f else 0.3f
                }
                hexDigits.forEach { button ->
                    button.isEnabled = false
                    button.alpha = 0.3f
                }
            }
            10 -> { // DEC - только 0-9
                digits.forEach { button ->
                    button.isEnabled = true
                    button.alpha = 1f
                }
                hexDigits.forEach { button ->
                    button.isEnabled = false
                    button.alpha = 0.3f
                }
            }
            16 -> { // HEX - 0-9, A-F
                digits.forEach { button ->
                    button.isEnabled = true
                    button.alpha = 1f
                }
                hexDigits.forEach { button ->
                    button.isEnabled = true
                    button.alpha = 1f
                }
            }
        }
    }

    private fun extractExpressionAndBaseFromHistory(historyExpression: String): Pair<String, Int> {
        val baseRegex = "\\((.*)\\)".toRegex()
        val baseMatch = baseRegex.find(historyExpression)
        var base = 10

        baseMatch?.let { match ->
            val baseName = match.groupValues[1]
            base = when (baseName.uppercase()) {
                "BIN" -> 2
                "OCT" -> 8
                "HEX" -> 16
                else -> 10
            }
        }

        val cleanExpression = historyExpression.replace("\\s*\\(.*\\)".toRegex(), "").trim()

        return Pair(cleanExpression, base)
    }

    private fun calculateAndSetResultFromHistory() {
        try {
            val result = evaluateProgrammerExpression(currentInput, currentBase)
            currentResult = result
            currentInput = result
            currentExpression = currentInput

            updateDisplay()
            convertAndDisplay()
        } catch (e: Exception) {
            currentResult = "Error"
            currentInput = "Error"
            updateDisplay()
        }
    }

    private fun calculate() {
        try {
            val result = evaluateProgrammerExpression(currentInput, currentBase)
            currentResult = result
            currentInput = result
            updateDisplay()
            convertAndDisplay()

            saveToHistory(result)

        } catch (e: Exception) {
            currentInput = "Error"
            updateDisplay()
        }
    }

    private fun saveToHistory(result: String) {
        if (result != "Error" && result.isNotEmpty() && result != "NaN") {
            try {
                result.toLongOrNull(currentBase)
                val displayExpression = "${currentExpression} (${getBaseName(currentBase)})"

                CoroutineScope(Dispatchers.IO).launch {
                    repository.insertHistory(
                        expression = displayExpression,
                        result = result,
                        calculatorType = "programmer"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun clearAll() {
        currentInput = "0"
        currentExpression = "0"
        currentResult = "0"
        updateDisplay()
        convertAndDisplay()
    }

    private fun getBaseName(base: Int): String {
        return when (base) {
            2 -> "BIN"
            8 -> "OCT"
            10 -> "DEC"
            16 -> "HEX"
            else -> "DEC"
        }
    }

    private fun evaluateProgrammerExpression(expression: String, base: Int): String {
        return try {
            val decimalExpression = convertExpressionToDecimal(expression, base)

            val decimalResult = eval(decimalExpression)

            decimalResult.toLong().toString(base).uppercase()
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun convertExpressionToDecimal(expression: String, base: Int): String {
        val result = StringBuilder()
        var currentNumber = StringBuilder()

        for (char in expression) {
            if (char in "+-*/()") {
                if (currentNumber.isNotEmpty()) {
                    val number = currentNumber.toString().toLongOrNull(base)
                    if (number == null) throw RuntimeException("Invalid number: ${currentNumber}")
                    result.append(number.toString())
                    currentNumber.clear()
                }
                result.append(char)
            } else {
                currentNumber.append(char)
            }
        }

        if (currentNumber.isNotEmpty()) {
            val number = currentNumber.toString().toLongOrNull(base)
            if (number == null) throw RuntimeException("Invalid number: ${currentNumber}")
            result.append(number.toString())
        }

        return result.toString()
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

    private fun deleteLast() {
        if (currentInput.length > 1) {
            currentInput = currentInput.substring(0, currentInput.length - 1)
            currentExpression = currentExpression.substring(0, currentExpression.length - 1)
        } else {
            currentInput = "0"
            currentExpression = "0"
        }
        updateDisplay()
        convertAndDisplay()
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