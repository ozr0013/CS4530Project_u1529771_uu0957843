package com.cs4530.drawingapp.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.cs4530.drawingapp.data.local.StrokeConverter
import com.cs4530.drawingapp.model.PenSettings
import com.cs4530.drawingapp.model.PenShape
import com.cs4530.drawingapp.model.PenStroke
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StrokeConverterTest {

    @Test
    fun toJsonAndFromJsonPreservesStrokesData() {
        val originalStrokes = listOf(
            PenStroke(
                points = listOf(Offset(10f, 20f), Offset(30f, 40f)),
                pen = PenSettings(color = Color.Red, size = 15f, shape = PenShape.SQUARE)
            ),
            PenStroke(
                points = listOf(Offset(100f, 200f)),
                pen = PenSettings(color = Color.Blue, size = 8f, shape = PenShape.LINE)
            )
        )

        val jsonStr = StrokeConverter.toJson(originalStrokes)
        val restoredStrokes = StrokeConverter.fromJson(jsonStr)

        assertEquals(2, restoredStrokes.size)

        // Stroke 1
        assertEquals(2, restoredStrokes[0].points.size)
        assertEquals(Offset(10f, 20f), restoredStrokes[0].points[0])
        assertEquals(Offset(30f, 40f), restoredStrokes[0].points[1])
        assertEquals(Color.Red, restoredStrokes[0].pen.color)
        assertEquals(15f, restoredStrokes[0].pen.size, 0.01f)
        assertEquals(PenShape.SQUARE, restoredStrokes[0].pen.shape)

        // Stroke 2
        assertEquals(1, restoredStrokes[1].points.size)
        assertEquals(Offset(100f, 200f), restoredStrokes[1].points[0])
        assertEquals(Color.Blue, restoredStrokes[1].pen.color)
        assertEquals(8f, restoredStrokes[1].pen.size, 0.01f)
        assertEquals(PenShape.LINE, restoredStrokes[1].pen.shape)
    }

    @Test
    fun fromJsonWithBlankStringReturnsEmptyList() {
        val result = StrokeConverter.fromJson("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun toJsonWithEmptyListReturnsEmptyJsonArray() {
        val jsonStr = StrokeConverter.toJson(emptyList())
        assertEquals("[]", jsonStr)
    }
}
