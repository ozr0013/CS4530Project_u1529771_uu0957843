package com.cs4530.drawingapp.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import com.cs4530.drawingapp.model.DrawingUiState
import com.cs4530.drawingapp.model.PenSettings
import com.cs4530.drawingapp.model.PenShape
import com.cs4530.drawingapp.model.PenStroke
import com.cs4530.drawingapp.model.stampPoints

/**
 * Renders the committed strokes and the in-progress stroke from [state], and reports
 * the first finger's touch as a stroke through the callbacks. Holds no state itself.
 */
@Composable
fun DrawingCanvas(
    state: DrawingUiState,
    modifier: Modifier = Modifier,
    onStrokeStart: (Offset) -> Unit = {},
    onStrokeMove: (Offset) -> Unit = {},
    onStrokeEnd: () -> Unit = {}
) {
    val currentStart by rememberUpdatedState(onStrokeStart)
    val currentMove by rememberUpdatedState(onStrokeMove)
    val currentEnd by rememberUpdatedState(onStrokeEnd)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                awaitEachGesture {
                    // Handled manually (not detectDragGestures) so a tap with no movement
                    // still starts and ends a stroke instead of waiting for touch slop.
                    val down = awaitFirstDown()
                    down.consume()
                    currentStart(down.position)
                    drag(down.id) { change ->
                        currentMove(change.position)
                        change.consume()
                    }
                    currentEnd()
                }
            }
    ) {
        state.strokes.forEach { drawPenStroke(it) }
        state.currentStroke?.let { drawPenStroke(it) }
    }
}

private fun DrawScope.drawPenStroke(stroke: PenStroke) {
    val pen = stroke.pen
    when (pen.shape) {
        PenShape.LINE -> drawLineStroke(stroke.points, pen)
        PenShape.SQUARE -> stampPoints(stroke.points, spacing = pen.size / 4f).forEach {
            drawRect(
                color = pen.color,
                topLeft = Offset(it.x - pen.size / 2f, it.y - pen.size / 2f),
                size = Size(pen.size, pen.size)
            )
        }
    }
}

private fun DrawScope.drawLineStroke(points: List<Offset>, pen: PenSettings) {
    if (points.isEmpty()) return
    if (points.size == 1) {
        // A tap with no movement: draw a dot so it is visible.
        drawCircle(color = pen.color, radius = pen.size / 2f, center = points.first())
        return
    }
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
    }
    drawPath(
        path = path,
        color = pen.color,
        style = Stroke(width = pen.size, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun DrawingCanvasPreview() {
    val line = PenStroke(
        points = listOf(Offset(40f, 80f), Offset(300f, 160f), Offset(500f, 120f)),
        pen = PenSettings(color = Color.Blue, size = 16f, shape = PenShape.LINE)
    )
    val squares = PenStroke(
        points = listOf(Offset(40f, 400f), Offset(500f, 360f)),
        pen = PenSettings(color = Color.Green, size = 30f, shape = PenShape.SQUARE)
    )
    DrawingCanvas(DrawingUiState(strokes = listOf(line, squares)))
}
