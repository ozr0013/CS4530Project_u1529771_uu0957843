package com.cs4530.drawingapp.data.local

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.cs4530.drawingapp.model.PenSettings
import com.cs4530.drawingapp.model.PenShape
import com.cs4530.drawingapp.model.PenStroke

object StrokeConverter {

    fun toJson(strokes: List<PenStroke>): String {
        if (strokes.isEmpty()) return "[]"
        val sb = StringBuilder("[")
        for (i in strokes.indices) {
            val stroke = strokes[i]
            if (i > 0) sb.append(",")
            sb.append("{\"pen\":{")
            sb.append("\"color\":").append(stroke.pen.color.value.toLong()).append(",")
            sb.append("\"size\":").append(stroke.pen.size).append(",")
            sb.append("\"shape\":\"").append(stroke.pen.shape.name).append("\"")
            sb.append("},\"points\":[")
            for (j in stroke.points.indices) {
                val pt = stroke.points[j]
                if (j > 0) sb.append(",")
                sb.append("{\"x\":").append(pt.x).append(",\"y\":").append(pt.y).append("}")
            }
            sb.append("]}")
        }
        sb.append("]")
        return sb.toString()
    }

    fun fromJson(jsonStr: String): List<PenStroke> {
        val trimmed = jsonStr.trim()
        if (trimmed.length <= 2 || !trimmed.startsWith("[")) return emptyList()
        val strokes = mutableListOf<PenStroke>()

        val strokeBlocks = regexFindBlocks(trimmed)
        for (block in strokeBlocks) {
            val colorVal = extractLong(block, "\"color\":") ?: 0L
            val color = Color(colorVal.toULong())
            val size = extractFloat(block, "\"size\":") ?: 12f
            val shapeStr = extractString(block, "\"shape\":\"") ?: PenShape.LINE.name
            val shape = try { PenShape.valueOf(shapeStr) } catch (e: Exception) { PenShape.LINE }
            val pen = PenSettings(color = color, size = size, shape = shape)

            val points = mutableListOf<Offset>()
            val pointsPart = block.substringAfter("\"points\":[", "").substringBefore("]", "")
            if (pointsPart.isNotBlank()) {
                val ptRegex = Regex("""\{"x":\s*(-?\d+(?:\.\d+)?)\s*,\s*"y":\s*(-?\d+(?:\.\d+)?)\}""")
                for (match in ptRegex.findAll(pointsPart)) {
                    val x = match.groupValues[1].toFloatOrNull() ?: 0f
                    val y = match.groupValues[2].toFloatOrNull() ?: 0f
                    points.add(Offset(x, y))
                }
            }
            strokes.add(PenStroke(points = points, pen = pen))
        }

        return strokes
    }

    private fun regexFindBlocks(str: String): List<String> {
        val list = mutableListOf<String>()
        var depth = 0
        var start = -1
        for (i in str.indices) {
            val c = str[i]
            if (c == '{') {
                if (depth == 0) start = i
                depth++
            } else if (c == '}') {
                depth--
                if (depth == 0 && start != -1) {
                    list.add(str.substring(start, i + 1))
                    start = -1
                }
            }
        }
        return list
    }

    private fun extractLong(str: String, key: String): Long? {
        val start = str.indexOf(key)
        if (start == -1) return null
        val numStart = start + key.length
        val end = str.indexOfAny(charArrayOf(',', '}', ']'), numStart)
        val numStr = if (end != -1) str.substring(numStart, end).trim() else str.substring(numStart).trim()
        return numStr.toLongOrNull()
    }

    private fun extractFloat(str: String, key: String): Float? {
        val start = str.indexOf(key)
        if (start == -1) return null
        val numStart = start + key.length
        val end = str.indexOfAny(charArrayOf(',', '}', ']'), numStart)
        val numStr = if (end != -1) str.substring(numStart, end).trim() else str.substring(numStart).trim()
        return numStr.toFloatOrNull()
    }

    private fun extractString(str: String, key: String): String? {
        val start = str.indexOf(key)
        if (start == -1) return null
        val valStart = start + key.length
        val end = str.indexOf('"', valStart)
        return if (end != -1) str.substring(valStart, end) else null
    }
}
