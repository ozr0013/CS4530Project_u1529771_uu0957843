package com.cs4530.drawingapp.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.cs4530.drawingapp.model.DetectedObject
import com.cs4530.drawingapp.model.NormalizedRect

/** One colour per detected object, so boxes on the image match rows in the results list. */
private val detectionPalette = listOf(
    Color(0xFFE53935), // red
    Color(0xFF1E88E5), // blue
    Color(0xFF43A047), // green
    Color(0xFFFB8C00), // orange
    Color(0xFF8E24AA), // purple
    Color(0xFF00ACC1), // teal
)

fun detectionColor(index: Int): Color = detectionPalette[index % detectionPalette.size]

fun confidenceLabel(confidence: Float): String = "${(confidence * 100).toInt()}%"

/** The largest centered area with the image's aspect ratio that fits in [container] (letterboxing). */
fun fitInside(image: Size, container: Size): Rect {
    val scale = minOf(container.width / image.width, container.height / image.height)
    val fitted = Size(image.width * scale, image.height * scale)
    return Rect(Offset((container.width - fitted.width) / 2f, (container.height - fitted.height) / 2f), fitted)
}

/** Maps a normalized (0..1) box onto the on-screen image [area]. */
fun NormalizedRect.toScreen(area: Rect) = Rect(
    left = area.left + left * area.width,
    top = area.top + top * area.height,
    right = area.left + right * area.width,
    bottom = area.top + bottom * area.height
)

/** Draws a labelled box for each object, mapped onto the image [imageArea] (whole canvas by default). */
fun DrawScope.drawDetections(
    objects: List<DetectedObject>,
    imageArea: Rect = Rect(Offset.Zero, size),
    highlighted: Int? = null
) {
    val density = density
    val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 13f * density
        typeface = Typeface.DEFAULT_BOLD
        color = android.graphics.Color.WHITE
    }
    val padding = 4f * density

    objects.forEachIndexed { index, obj ->
        val color = detectionColor(index)
        val dimmed = highlighted != null && highlighted != index
        val (left, top, right, bottom) = obj.box.toScreen(imageArea)

        drawRect(
            color = color.copy(alpha = if (dimmed) 0.08f else 0.15f),
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top)
        )
        drawRect(
            color = color.copy(alpha = if (dimmed) 0.35f else 1f),
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            style = Stroke(width = (if (highlighted == index) 4f else 2.5f) * density)
        )
        if (dimmed) return@forEachIndexed

        // Label chip above the box (or inside it when the box touches the top edge).
        val label = "${obj.name} ${confidenceLabel(obj.confidence)}"
        val textWidth = textPaint.measureText(label)
        val chipHeight = textPaint.textSize + padding * 2
        val chipTop = if (top - chipHeight >= imageArea.top) top - chipHeight else top
        drawRoundRect(
            color = color,
            topLeft = Offset(left, chipTop),
            size = Size(textWidth + padding * 2, chipHeight),
            cornerRadius = CornerRadius(4f * density)
        )
        drawContext.canvas.nativeCanvas.drawText(
            label,
            left + padding,
            chipTop + padding + textPaint.textSize * 0.85f,
            textPaint.apply { this.color = Color.White.toArgb() }
        )
    }
}
