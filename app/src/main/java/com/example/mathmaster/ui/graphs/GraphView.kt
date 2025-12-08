package com.example.mathmaster.ui.graphs

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.max
import kotlin.math.min

class GraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Всегда белый фон для плоскости
    private val axisPaint = Paint().apply {
        color = Color.parseColor("#000000") // Черные оси всегда
        strokeWidth = 3f
        isAntiAlias = true
    }
    private val gridPaint = Paint().apply {
        color = Color.parseColor("#E0E0E0") // Светло-серые линии сетки
        strokeWidth = 1f
    }
    private val textPaint = Paint().apply {
        color = Color.parseColor("#000000") // Черные цифры всегда
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

    // Camera/View properties
    private var scale = 50f // pixels per unit
    private var offsetX = 0f
    private var offsetY = 0f

    // Gesture detectors
    private val gestureDetector: GestureDetector
    private val scaleGestureDetector: ScaleGestureDetector

    init {
        gestureDetector = GestureDetector(context, GestureListener())
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())

        // Set minimum scale
        val density = context.resources.displayMetrics.density
        scale = max(scale, 20f * density)

        // Устанавливаем белый фон
        setBackgroundColor(Color.WHITE)
    }

    fun plotFunction(function: String, color: Int) {
        functions = functions + (function to color)
        invalidate()
    }

    fun clearGraph() {
        functions = emptyList()
        // Reset view
        scale = 50f
        offsetX = 0f
        offsetY = 0f
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f + offsetX
        val centerY = height / 2f + offsetY

        drawGrid(canvas, centerX, centerY)
        drawAxes(canvas, centerX, centerY)
        drawLabels(canvas, centerX, centerY)
        drawFunctions(canvas, centerX, centerY)
    }

    private fun drawGrid(canvas: Canvas, centerX: Float, centerY: Float) {
        // Calculate visible grid lines
        val startX = -((centerX) / scale).toInt() - 1
        val endX = ((width - centerX) / scale).toInt() + 1
        val startY = -((height - centerY) / scale).toInt() - 1
        val endY = ((centerY) / scale).toInt() + 1

        for (x in startX..endX) {
            val screenX = centerX + x * scale
            canvas.drawLine(screenX, 0f, screenX, height.toFloat(), gridPaint)
        }

        for (y in startY..endY) {
            val screenY = centerY - y * scale
            canvas.drawLine(0f, screenY, width.toFloat(), screenY, gridPaint)
        }
    }

    private fun drawAxes(canvas: Canvas, centerX: Float, centerY: Float) {
        canvas.drawLine(0f, centerY, width.toFloat(), centerY, axisPaint)
        canvas.drawLine(centerX, 0f, centerX, height.toFloat(), axisPaint)

        // Стрелки на осях
        val arrowSize = 10f

        // Стрелка на оси X
        canvas.drawLine(
            width.toFloat(), centerY,
            width.toFloat() - arrowSize, centerY - arrowSize,
            axisPaint
        )
        canvas.drawLine(
            width.toFloat(), centerY,
            width.toFloat() - arrowSize, centerY + arrowSize,
            axisPaint
        )

        // Стрелка на оси Y
        canvas.drawLine(
            centerX, 0f,
            centerX - arrowSize, arrowSize,
            axisPaint
        )
        canvas.drawLine(
            centerX, 0f,
            centerX + arrowSize, arrowSize,
            axisPaint
        )
    }

    private fun drawLabels(canvas: Canvas, centerX: Float, centerY: Float) {
        val labelStep = max(1, (scale / 30).toInt()) // Adjust label density based on zoom

        val startX = -((centerX) / scale).toInt() - 1
        val endX = ((width - centerX) / scale).toInt() + 1
        val startY = -((height - centerY) / scale).toInt() - 1
        val endY = ((centerY) / scale).toInt() + 1

        // Подписи на оси X
        for (x in startX..endX step labelStep) {
            if (x == 0) continue
            val screenX = centerX + x * scale
            if (screenX in 40f..(width - 40f)) {
                canvas.drawText(x.toString(), screenX - 10, centerY + 35, textPaint)
                // Маленькая отметка на оси
                canvas.drawLine(screenX, centerY - 5, screenX, centerY + 5, axisPaint)
            }
        }

        // Подписи на оси Y
        for (y in startY..endY step labelStep) {
            if (y == 0) continue
            val screenY = centerY - y * scale
            if (screenY in 40f..(height - 40f)) {
                canvas.drawText(y.toString(), centerX + 15, screenY + 10, textPaint)
                // Маленькая отметка на оси
                canvas.drawLine(centerX - 5, screenY, centerX + 5, screenY, axisPaint)
            }
        }

        // Ноль в центре
        if (centerX in 40f..(width - 40f) && centerY in 40f..(height - 40f)) {
            canvas.drawText("0", centerX + 10, centerY + 35, textPaint)
        }
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

    // Gesture handling classes
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (!scaleGestureDetector.isInProgress) {
                offsetX -= distanceX
                offsetY -= distanceY
                invalidate()
                return true
            }
            return false
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val newScale = scale * scaleFactor

            // Clamp scale between min and max values
            val density = context.resources.displayMetrics.density
            val minScale = 10f * density
            val maxScale = 200f * density

            if (newScale in minScale..maxScale) {
                val focusX = detector.focusX
                val focusY = detector.focusY

                // Adjust offset to zoom around focal point
                offsetX = focusX - (focusX - offsetX) * scaleFactor
                offsetY = focusY - (focusY - offsetY) * scaleFactor

                scale = newScale
                invalidate()
                return true
            }
            return false
        }
    }
}