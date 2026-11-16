package com.cs4530.drawingapp.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs4530.drawingapp.BuildConfig
import com.cs4530.drawingapp.data.local.DrawingEntity
import com.cs4530.drawingapp.data.local.StrokeConverter
import com.cs4530.drawingapp.data.repository.DrawingRepository
import com.cs4530.drawingapp.model.DrawingUiState
import com.cs4530.drawingapp.model.PenSettings
import com.cs4530.drawingapp.model.PenShape
import com.cs4530.drawingapp.model.PenStroke
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DrawingViewModel(application: Application = Application()) : AndroidViewModel(application) {

    val repository: DrawingRepository by lazy {
        DrawingRepository.getInstance(application)
    }

    val allDrawings: StateFlow<List<DrawingEntity>> by lazy {
        repository.allDrawings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private val _state = MutableStateFlow(DrawingUiState())
    val state: StateFlow<DrawingUiState> = _state.asStateFlow()

    private val _currentDrawingId = MutableStateFlow<Long?>(null)
    val currentDrawingId: StateFlow<Long?> = _currentDrawingId.asStateFlow()

    private val _currentTitle = MutableStateFlow("Untitled Drawing")
    val currentTitle: StateFlow<String> = _currentTitle.asStateFlow()

    fun setTitle(title: String) {
        _currentTitle.value = title
    }

    fun loadDrawing(id: Long) {
        if (id <= 0) {
            newDrawing()
            return
        }
        viewModelScope.launch {
            val entity = repository.getDrawingById(id)
            if (entity != null) {
                _currentDrawingId.value = entity.id
                _currentTitle.value = entity.title
                val loadedStrokes = StrokeConverter.fromJson(entity.strokesJson)
                _state.update {
                    it.copy(strokes = loadedStrokes, currentStroke = null, redoStack = emptyList())
                }
            } else {
                newDrawing()
            }
        }
    }

    fun newDrawing() {
        _currentDrawingId.value = null
        _currentTitle.value = "Untitled Drawing"
        _state.update {
            it.copy(strokes = emptyList(), currentStroke = null, redoStack = emptyList())
        }
    }

    fun saveCurrentDrawing(previewBitmap: Bitmap? = null, onSaved: () -> Unit = {}) {
        val title = _currentTitle.value.ifBlank { "Untitled Drawing" }
        val id = _currentDrawingId.value ?: 0L
        val strokes = _state.value.strokes

        viewModelScope.launch {
            val newId = repository.saveDrawing(
                id = id,
                title = title,
                strokes = strokes,
                previewBitmap = previewBitmap
            )
            _currentDrawingId.value = newId
            onSaved()
        }
    }

    fun deleteDrawing(id: Long) {
        viewModelScope.launch {
            repository.deleteDrawing(id)
            if (_currentDrawingId.value == id) {
                newDrawing()
            }
        }
    }

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
        it.copy(strokes = emptyList(), currentStroke = null, redoStack = emptyList(), backgroundImage = null)
    }

    fun importImageFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    _state.update { it.copy(backgroundImage = bitmap) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun shareDrawing(context: Context) {
        val id = _currentDrawingId.value
        val title = _currentTitle.value.ifBlank { "Drawing" }

        viewModelScope.launch {
            val fileName = if (id != null && id > 0) {
                repository.getDrawingById(id)?.imageFileName ?: "shared_drawing.png"
            } else {
                "shared_drawing.png"
            }

            val file = repository.getDrawingFile(fileName)
            if (!file.exists()) {
                repository.saveDrawing(
                    id = id ?: 0L,
                    title = title,
                    strokes = _state.value.strokes
                )
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Drawing")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }

    fun dismissAnalysis() {
        _state.update { it.copy(analysisResult = null, analysisError = null) }
    }

    fun analyzeCurrentDrawing(apiKey: String = com.cs4530.drawingapp.BuildConfig.VISION_API_KEY) {
        val currentState = _state.value
        val bitmapToAnalyze = currentState.backgroundImage ?: generateDrawingBitmap()

        _state.update { it.copy(isAnalyzing = true, analysisError = null, analysisResult = null) }

        viewModelScope.launch {
            try {
                val key = apiKey.ifBlank { "AIzaSyDummyKeyForTestingOrFallback" }
                val result = repository.analyzeImage(bitmapToAnalyze, key)
                _state.update { it.copy(isAnalyzing = false, analysisResult = result) }
            } catch (e: Exception) {
                _state.update { it.copy(isAnalyzing = false, analysisError = e.localizedMessage ?: "Failed to analyze image") }
            }
        }
    }

    private fun generateDrawingBitmap(width: Int = 1024, height: Int = 1024): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val currentState = _state.value
        currentState.backgroundImage?.let { bg ->
            canvas.drawBitmap(bg, null, Rect(0, 0, width, height), null)
        }

        val paint = Paint().apply {
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            style = Paint.Style.STROKE
        }

        currentState.strokes.forEach { stroke ->
            val color = stroke.pen.color
            paint.color = android.graphics.Color.argb(
                (color.alpha * 255).toInt(),
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt()
            )
            paint.strokeWidth = stroke.pen.size
            if (stroke.points.size == 1) {
                canvas.drawCircle(stroke.points[0].x, stroke.points[0].y, stroke.pen.size / 2f, paint.apply { style = Paint.Style.FILL })
                paint.style = Paint.Style.STROKE
            } else if (stroke.points.size > 1) {
                val path = Path().apply {
                    moveTo(stroke.points[0].x, stroke.points[0].y)
                    for (i in 1 until stroke.points.size) {
                        lineTo(stroke.points[i].x, stroke.points[i].y)
                    }
                }
                canvas.drawPath(path, paint)
            }
        }
        return bitmap
    }
}
