package com.cs4530.drawingapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawingDao {

    @Query("SELECT * FROM drawings ORDER BY updatedAt DESC")
    fun getAllDrawings(): Flow<List<DrawingEntity>>

    @Query("SELECT * FROM drawings WHERE id = :id")
    fun getDrawingById(id: Long): DrawingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDrawing(drawing: DrawingEntity): Long

    @Update
    fun updateDrawing(drawing: DrawingEntity): Int

    @Delete
    fun deleteDrawing(drawing: DrawingEntity): Int

    @Query("DELETE FROM drawings WHERE id = :id")
    fun deleteDrawingById(id: Long): Int
}
