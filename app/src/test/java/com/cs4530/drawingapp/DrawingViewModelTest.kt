package com.cs4530.drawingapp

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.cs4530.drawingapp.model.PenSettings
import com.cs4530.drawingapp.model.PenShape
import com.cs4530.drawingapp.model.stampPoints
import com.cs4530.drawingapp.viewmodel.DrawingViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DrawingViewModelTest {

    private lateinit var vm: DrawingViewModel

    @Before
    fun setUp() {
        vm = DrawingViewModel()
    }

    private fun drawStroke(p1: Offset, p2: Offset) {
        drawStroke(listOf(p1, p2))
    }

    private fun drawStroke(p1: Offset, p2: Offset, p3: Offset) {
        drawStroke(listOf(p1, p2, p3))
    }

    private fun drawStroke(points: List<Offset>) {
        vm.startStroke(points.first())
        points.drop(1).forEach { vm.addPoint(it) }
        vm.endStroke()
    }

    @Test
    fun initialStateIsEmpty() {
        val s = vm.state.value
        assertTrue(s.strokes.isEmpty())
        assertNull(s.currentStroke)
        assertEquals(PenSettings(), s.pen)
    }

    @Test
    fun completedStrokeIsSaved() {
        drawStroke(Offset(0f, 0f), Offset(10f, 10f), Offset(20f, 20f))
        val s = vm.state.value
        assertEquals(1, s.strokes.size)
        assertEquals(3, s.strokes[0].points.size)
        assertNull(s.currentStroke)
    }

    @Test
    fun strokeInProgressIsNotCommitted() {
        vm.startStroke(Offset(0f, 0f))
        vm.addPoint(Offset(5f, 5f))
        assertNotNull(vm.state.value.currentStroke)
        assertTrue(vm.state.value.strokes.isEmpty())
    }

    @Test
    fun penChangesOnlyAffectNewStrokes() {
        drawStroke(Offset(0f, 0f), Offset(1f, 1f))
        vm.setColor(Color.Red)
        vm.setShape(PenShape.SQUARE)
        drawStroke(Offset(2f, 2f), Offset(3f, 3f))

        val strokes = vm.state.value.strokes
        assertEquals(Color.Black, strokes[0].pen.color)
        assertEquals(PenShape.LINE, strokes[0].pen.shape)
        assertEquals(Color.Red, strokes[1].pen.color)
        assertEquals(PenShape.SQUARE, strokes[1].pen.shape)
    }

    @Test
    fun sizeIsClampedToRange() {
        vm.setSize(1000f)
        assertEquals(PenSettings.MAX_SIZE, vm.state.value.pen.size)
        vm.setSize(-5f)
        assertEquals(PenSettings.MIN_SIZE, vm.state.value.pen.size)
    }

    @Test
    fun undoThenRedoRestoresStroke() {
        drawStroke(Offset(0f, 0f), Offset(1f, 1f))
        vm.undo()
        assertTrue(vm.state.value.strokes.isEmpty())
        assertTrue(vm.state.value.canRedo)

        vm.redo()
        assertEquals(1, vm.state.value.strokes.size)
        assertFalse(vm.state.value.canRedo)
    }

    @Test
    fun newStrokeClearsRedoStack() {
        drawStroke(Offset(0f, 0f), Offset(1f, 1f))
        vm.undo()
        drawStroke(Offset(5f, 5f), Offset(6f, 6f))
        assertFalse(vm.state.value.canRedo)
    }

    @Test
    fun undoOnEmptyCanvasDoesNothing() {
        vm.undo()
        assertTrue(vm.state.value.strokes.isEmpty())
    }

    @Test
    fun clearRemovesEverything() {
        drawStroke(Offset(0f, 0f), Offset(1f, 1f))
        drawStroke(Offset(2f, 2f), Offset(3f, 3f))
        vm.clear()
        assertTrue(vm.state.value.strokes.isEmpty())
        assertFalse(vm.state.value.canUndo)
    }

    @Test
    fun stampPointsFillsGaps() {
        val result = stampPoints(listOf(Offset(0f, 0f), Offset(10f, 0f)), spacing = 2f)
        assertEquals(6, result.size)
        assertEquals(Offset(10f, 0f), result.last())
    }
}
