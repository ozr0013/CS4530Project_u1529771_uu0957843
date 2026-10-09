package com.cs4530.drawingapp.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.ceil

enum class PenShape { LINE, SQUARE }

data class PenSettings(
    val color: Color = Color.Black,
    val size: Float = 12f,
    val shape: PenShape = PenShape.LINE
) {
    companion object {
        const val MIN_SIZE = 2f
        const val MAX_SIZE = 60f
    }
}

data class PenStroke(
    val points: List<Offset>,
    val pen: PenSettings
)

data class DrawingUiState(
    val strokes: List<PenStroke> = emptyList(),
    val currentStroke: PenStroke? = null,
    val pen: PenSettings = PenSettings(),
    val redoStack: List<PenStroke> = emptyList()
) {
    val canUndo: Boolean get() = strokes.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
}

/** Fills gaps between touch points so square stamps form a continuous line. */
fun stampPoints(points: List<Offset>, spacing: Float): List<Offset> {
    if (points.size < 2) return points
    val result = mutableListOf(points.first())
    for (i in 1 until points.size) {
        val start = points[i - 1]
        val end = points[i]
        val steps = maxOf(1, ceil((end - start).getDistance() / spacing).toInt())
        for (s in 1..steps) {
            result += start + (end - start) * (s / steps.toFloat())
        }
    }
    return result
}
