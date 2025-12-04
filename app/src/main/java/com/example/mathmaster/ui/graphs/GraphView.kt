package com.example.mathmaster.ui.graphs

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class GraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val axisPaint = Paint().apply {
        color = Color.parseColor("#49454F")
        strokeWidth = 3f
        isAntiAlias = true
    }
    private val gridPaint = Paint().apply {
        color = Color.parseColor("#E7E0EC")
        strokeWidth = 1f
    }
    private val textPaint = Paint().apply {
        color = Color.parseColor("#49454F")
        textSize = 24f
        isAntiAlias = true
    }
    private val graphPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 4f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private var functions: List<Pair<String, Int>> = emptyList()
    private val scale = 50f // pixels per unit

    fun plotFunction(function: String, color: Int) {
        functions = functions + (function to color)
        invalidate()
    }

    fun clearGraph() {
        functions = emptyList()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        drawGrid(canvas, centerX, centerY)
        drawAxes(canvas, centerX, centerY)
        drawLabels(canvas, centerX, centerY)
        drawFunctions(canvas, centerX, centerY)
    }

    private fun drawGrid(canvas: Canvas, centerX: Float, centerY: Float) {
        for (x in -8..8) {
            val screenX = centerX + x * scale
            canvas.drawLine(screenX, 0f, screenX, height.toFloat(), gridPaint)
        }

        for (y in -7..7) {
            val screenY = centerY - y * scale
            canvas.drawLine(0f, screenY, width.toFloat(), screenY, gridPaint)
        }
    }

    private fun drawAxes(canvas: Canvas, centerX: Float, centerY: Float) {
        canvas.drawLine(0f, centerY, width.toFloat(), centerY, axisPaint)
        canvas.drawLine(centerX, 0f, centerX, height.toFloat(), axisPaint)
    }

    private fun drawLabels(canvas: Canvas, centerX: Float, centerY: Float) {
        for (x in -8..8) {
            if (x == 0) continue
            val screenX = centerX + x * scale
            canvas.drawText(x.toString(), screenX - 8, centerY + 30, textPaint)
        }

        for (y in -7..7) {
            if (y == 0) continue
            val screenY = centerY - y * scale
            canvas.drawText(y.toString(), centerX + 10, screenY + 8, textPaint)
        }

        canvas.drawText("0", centerX + 5, centerY + 30, textPaint)
    }

    private fun drawFunctions(canvas: Canvas, centerX: Float, centerY: Float) {
        functions.forEach { (function, color) ->
            graphPaint.color = color
            drawFunction(canvas, function, centerX, centerY)
        }
    }

    private fun drawFunction(canvas: Canvas, function: String, centerX: Float, centerY: Float) {
        val path = Path()
        var firstPoint = true

        for (screenX in 0 until width) {
            val x = (screenX - centerX) / scale
            try {
                val y = evaluateFunction(function, x.toDouble())
                if (y.isFinite()) {
                    val screenY = centerY - y.toFloat() * scale

                    if (firstPoint) {
                        path.moveTo(screenX.toFloat(), screenY)
                        firstPoint = false
                    } else {
                        path.lineTo(screenX.toFloat(), screenY)
                    }
                } else {
                    firstPoint = true
                }
            } catch (e: Exception) {
                firstPoint = true
            }
        }

        canvas.drawPath(path, graphPaint)
    }

    private fun evaluateFunction(function: String, x: Double): Double {
        return try {
            val expression = net.objecthunter.exp4j.ExpressionBuilder(function)
                .variables("x")
                .build()
                .setVariable("x", x)
            expression.evaluate()
        } catch (e: Exception) {
            Double.NaN
        }
    }

    private fun evalSimpleExpression(expr: String): Double {
        return when {
            expr.contains("+") -> {
                val parts = expr.split("+")
                evalSimpleExpression(parts[0]) + evalSimpleExpression(parts[1])
            }
            expr.contains("-") -> {
                val parts = expr.split("-")
                evalSimpleExpression(parts[0]) - evalSimpleExpression(parts[1])
            }
            expr.contains("*") -> {
                val parts = expr.split("*")
                evalSimpleExpression(parts[0]) * evalSimpleExpression(parts[1])
            }
            expr.contains("/") -> {
                val parts = expr.split("/")
                evalSimpleExpression(parts[0]) / evalSimpleExpression(parts[1])
            }
            expr.contains("^") -> {
                val parts = expr.split("^")
                Math.pow(evalSimpleExpression(parts[0]), evalSimpleExpression(parts[1]))
            }
            else -> expr.toDoubleOrNull() ?: Double.NaN
        }
    }
}