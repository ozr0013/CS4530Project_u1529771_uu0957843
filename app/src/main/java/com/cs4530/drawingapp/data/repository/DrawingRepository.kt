package com.cs4530.drawingapp.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.cs4530.drawingapp.data.local.DrawingDao
import com.cs4530.drawingapp.data.local.DrawingDatabase
import com.cs4530.drawingapp.data.local.DrawingEntity
import com.cs4530.drawingapp.data.local.StrokeConverter
import com.cs4530.drawingapp.model.PenStroke
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class DrawingRepository private constructor(
    private val dao: DrawingDao,
    private val appContext: Context
) {

    val allDrawings: Flow<List<DrawingEntity>> = dao.getAllDrawings()

    suspend fun getDrawingById(id: Long): DrawingEntity? = withContext(Dispatchers.IO) {
        dao.getDrawingById(id)
    }

    suspend fun saveDrawing(
        id: Long = 0,
        title: String,
        strokes: List<PenStroke>,
        previewBitmap: Bitmap? = null
    ): Long = withContext(Dispatchers.IO) {
        val fileName = if (id > 0) {
            val existing = dao.getDrawingById(id)
            existing?.imageFileName ?: "drawing_${System.currentTimeMillis()}.png"
        } else {
            "drawing_${System.currentTimeMillis()}.png"
        }

        if (previewBitmap != null) {
            saveBitmapToStorage(fileName, previewBitmap)
        }

        val strokesJson = StrokeConverter.toJson(strokes)
        val entity = DrawingEntity(
            id = id,
            title = title,
            updatedAt = System.currentTimeMillis(),
            imageFileName = fileName,
            strokesJson = strokesJson
        )

        dao.insertDrawing(entity)
    }

    suspend fun deleteDrawing(id: Long) = withContext(Dispatchers.IO) {
        val drawing = dao.getDrawingById(id)
        if (drawing != null) {
            deleteFileFromStorage(drawing.imageFileName)
            dao.deleteDrawingById(id)
        }
    }

    private fun saveBitmapToStorage(fileName: String, bitmap: Bitmap) {
        val file = File(appContext.filesDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    private fun deleteFileFromStorage(fileName: String) {
        val file = File(appContext.filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }
    }

    fun getDrawingFile(fileName: String): File {
        return File(appContext.filesDir, fileName)
    }

    companion object {
        @Volatile
        private var INSTANCE: DrawingRepository? = null

        fun getInstance(context: Context): DrawingRepository {
            return INSTANCE ?: synchronized(this) {
                val db = DrawingDatabase.getDatabase(context.applicationContext)
                val instance = DrawingRepository(db.drawingDao(), context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
