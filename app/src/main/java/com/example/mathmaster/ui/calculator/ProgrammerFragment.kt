package com.example.mathmaster.ui.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mathmaster.databinding.FragmentProgrammerBinding
import com.example.mathmaster.database.AppDatabase
import com.example.mathmaster.repository.CalculatorRepository
import com.example.mathmaster.ui.history.HistoryViewModel

class ProgrammerFragment : Fragment() {

    private var _binding: FragmentProgrammerBinding? = null
    private val binding get() = _binding!!

    private var currentInput = "0"
    private var currentExpression = "0"
    private var currentResult = "0"
    private var currentBase = 10 // DEC по умолчанию

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgrammerBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var historyViewModel: HistoryViewModel
    private var isFromHistory = false
    private var shouldSaveToHistory = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация ViewModel для истории
        val database = AppDatabase.getDatabase(requireContext())
        val repository = CalculatorRepository(database.calculationHistoryDao())
        historyViewModel = HistoryViewModel(repository)

        // ПРОВЕРЯЕМ ПЕРЕДАННОЕ ВЫРАЖЕНИЕ ИЗ ИСТОРИИ
        val expressionFromHistory = arguments?.getString("expressionFromHistory")
        if (!expressionFromHistory.isNullOrEmpty()) {
            // ИЗВЛЕКАЕМ СИСТЕМУ СЧИСЛЕНИЯ И ВЫРАЖЕНИЕ
            val (cleanExpression, base) = extractExpressionAndBaseFromHistory(expressionFromHistory)
            currentInput = cleanExpression
            currentExpression = cleanExpression
            currentBase = base // УСТАНАВЛИВАЕМ СИСТЕМУ СЧИСЛЕНИЯ ИЗ ИСТОРИИ

            // ВЫЧИСЛЯЕМ И ПОДСТАВЛЯЕМ РЕЗУЛЬТАТ
            calculateAndSetResultFromHistory()
        }

        setupClickListeners()
        updateDisplay()
        updateButtonAvailability()
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
        binding.buttonHex.setOnClickListener { changeBase(16) }
        binding.buttonDec.setOnClickListener { changeBase(10) }
        binding.buttonOct.setOnClickListener { changeBase(8) }
        binding.buttonBin.setOnClickListener { changeBase(2) }

        // Операции
        binding.buttonDelete.setOnClickListener { deleteLast() }
        binding.buttonAdd.setOnClickListener { appendOperation("+") }
        binding.buttonSubtract.setOnClickListener { appendOperation("-") }
        binding.buttonMultiply.setOnClickListener { appendOperation("*") }
        binding.buttonDivide.setOnClickListener { appendOperation("/") }
        binding.buttonEquals.setOnClickListener { calculate() }
        binding.buttonOpenBracket.setOnClickListener { appendOperation("(") }
        binding.buttonCloseBracket.setOnClickListener { appendOperation(")") }
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
        shouldSaveToHistory = true // РАЗРЕШАЕМ СОХРАНЕНИЕ ПРИ ЛЮБОМ ВВОДЕ
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
                // ОБНОВЛЯЕМ ВЫРАЖЕНИЕ ПРИ СМЕНЕ СИСТЕМЫ
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
        // Включаем/выключаем кнопки в зависимости от системы счисления
        val digits = listOf(
            binding.button2, binding.button3, binding.button4, binding.button5,
            binding.button6, binding.button7, binding.button8, binding.button9
        )

        val hexDigits = listOf(
            binding.buttonA, binding.buttonB, binding.buttonC,
            binding.buttonD, binding.buttonE, binding.buttonF
        )

        when (currentBase) {
            2 -> { // BIN - только 0-1
                digits.forEach { it.isEnabled = false }
                hexDigits.forEach { it.isEnabled = false }
            }
            8 -> { // OCT - только 0-7
                digits.forEachIndexed { index, button ->
                    button.isEnabled = index < 6 // 2-7
                }
                hexDigits.forEach { it.isEnabled = false }
            }
            10 -> { // DEC - только 0-9
                digits.forEach { it.isEnabled = true }
                hexDigits.forEach { it.isEnabled = false }
            }
            16 -> { // HEX - 0-9, A-F
                digits.forEach { it.isEnabled = true }
                hexDigits.forEach { it.isEnabled = true }
            }
        }
    }

    private fun extractExpressionAndBaseFromHistory(historyExpression: String): Pair<String, Int> {
        // Извлекаем систему счисления из скобок
        val baseRegex = "\\((.*)\\)".toRegex()
        val baseMatch = baseRegex.find(historyExpression)
        var base = 10 // по умолчанию DEC

        baseMatch?.let { match ->
            val baseName = match.groupValues[1]
            base = when (baseName.uppercase()) {
                "BIN" -> 2
                "OCT" -> 8
                "HEX" -> 16
                else -> 10
            }
        }

        // Извлекаем чистое выражение (убираем систему счисления в скобках)
        val cleanExpression = historyExpression.replace("\\s*\\(.*\\)".toRegex(), "").trim()

        return Pair(cleanExpression, base)
    }

    private fun calculateAndSetResultFromHistory() {
        try {
            val result = evaluateProgrammerExpression(currentInput, currentBase)
            currentResult = result
            currentInput = result // ПОДСТАВЛЯЕМ РЕЗУЛЬТАТ В КАЧЕСТВЕ ТЕКУЩЕГО ВВОДА
            currentExpression = currentInput // ОБНОВЛЯЕМ ВЫРАЖЕНИЕ ДЛЯ ОТОБРАЖЕНИЯ
            shouldSaveToHistory = false // НЕ СОХРАНЯЕМ ПЕРВОЕ ВЫЧИСЛЕНИЕ

            updateDisplay()
            convertAndDisplay()
        } catch (e: Exception) {
            currentResult = "Error"
            currentInput = "Error"
            updateDisplay()
            shouldSaveToHistory = true
        }
    }

    private fun calculateFromHistory() {
        calculateAndSetResultFromHistory() // ИСПОЛЬЗУЕМ ОДИН МЕТОД
    }

    private fun calculate() {
        try {
            val result = evaluateProgrammerExpression(currentInput, currentBase)
            currentResult = result
            currentInput = result
            updateDisplay()
            convertAndDisplay()

            // СОХРАНЯЕМ ТОЛЬКО ЕСЛИ РАЗРЕШЕНО
            if (result != "Error" && result.isNotEmpty() && result != "NaN" && shouldSaveToHistory) {
                try {
                    result.toLongOrNull(currentBase)
                    val displayExpression = "${currentExpression} (${getBaseName(currentBase)})"

                    historyViewModel.saveCalculation(
                        expression = displayExpression,
                        result = result,
                        calculatorType = "programmer"
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                // РАЗРЕШАЕМ СОХРАНЕНИЕ ДЛЯ СЛЕДУЮЩИХ ВЫЧИСЛЕНИЙ
                shouldSaveToHistory = true
            }

        } catch (e: Exception) {
            currentInput = "Error"
            updateDisplay()
            shouldSaveToHistory = true
        }
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
            // Конвертируем все числа в выражении в десятичную систему для вычислений
            val decimalExpression = convertExpressionToDecimal(expression, base)

            // Вычисляем выражение в десятичной системе
            val decimalResult = eval(decimalExpression)

            // Конвертируем результат обратно в текущую систему
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
                // Если встречаем оператор или скобку, добавляем текущее число (если есть) и оператор
                if (currentNumber.isNotEmpty()) {
                    // Конвертируем число из текущей системы в десятичную
                    val number = currentNumber.toString().toLongOrNull(base)
                    if (number == null) throw RuntimeException("Invalid number: ${currentNumber}")
                    result.append(number.toString())
                    currentNumber.clear()
                }
                result.append(char)
            } else {
                // Добавляем цифру к текущему числу
                currentNumber.append(char)
            }
        }

        // Добавляем последнее число
        if (currentNumber.isNotEmpty()) {
            val number = currentNumber.toString().toLongOrNull(base)
            if (number == null) throw RuntimeException("Invalid number: ${currentNumber}")
            result.append(number.toString())
        }

        return result.toString()
    }

    // Используем тот же eval метод что и в обычном калькуляторе
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
        binding.baseDisplay.text = when (currentBase) {
            2 -> "BIN"
            8 -> "OCT"
            10 -> "DEC"
            16 -> "HEX"
            else -> "DEC"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}