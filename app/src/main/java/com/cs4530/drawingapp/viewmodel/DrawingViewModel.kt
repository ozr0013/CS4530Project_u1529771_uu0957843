package com.cs4530.drawingapp.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.cs4530.drawingapp.model.DrawingUiState
import com.cs4530.drawingapp.model.PenSettings
import com.cs4530.drawingapp.model.PenShape
import com.cs4530.drawingapp.model.PenStroke
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DrawingViewModel : ViewModel() {

    private val _state = MutableStateFlow(DrawingUiState())
    val state: StateFlow<DrawingUiState> = _state.asStateFlow()

    fun startStroke(point: Offset) = _state.update {
        it.copy(currentStroke = PenStroke(listOf(point), it.pen))
    }

    fun addPoint(point: Offset) = _state.update { s ->
        val current = s.currentStroke ?: return@update s
        s.copy(currentStroke = current.copy(points = current.points + point))
    }

    fun endStroke() = _state.update { s ->
        val current = s.currentStroke ?: return@update s
        s.copy(strokes = s.strokes + current, currentStroke = null, redoStack = emptyList())
    }

    fun setColor(color: Color) = _state.update { it.copy(pen = it.pen.copy(color = color)) }

    fun setSize(size: Float) = _state.update {
        it.copy(pen = it.pen.copy(size = size.coerceIn(PenSettings.MIN_SIZE, PenSettings.MAX_SIZE)))
    }

    fun setShape(shape: PenShape) = _state.update { it.copy(pen = it.pen.copy(shape = shape)) }

    fun undo() = _state.update { s ->
        if (s.strokes.isEmpty()) s
        else s.copy(strokes = s.strokes.dropLast(1), redoStack = s.redoStack + s.strokes.last())
    }

    fun redo() = _state.update { s ->
        if (s.redoStack.isEmpty()) s
        else s.copy(strokes = s.strokes + s.redoStack.last(), redoStack = s.redoStack.dropLast(1))
    }

    fun clear() = _state.update {
        it.copy(strokes = emptyList(), currentStroke = null, redoStack = emptyList())
    }
}
