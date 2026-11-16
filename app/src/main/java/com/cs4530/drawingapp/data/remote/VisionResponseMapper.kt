package com.cs4530.drawingapp.data.remote

import com.cs4530.drawingapp.data.remote.dto.AnnotateResponse
import com.cs4530.drawingapp.model.AnalysisResult
import com.cs4530.drawingapp.model.DetectedObject
import com.cs4530.drawingapp.model.NormalizedRect
import com.cs4530.drawingapp.model.VisionApiException

/**
 * Maps the response for a single-image request into domain models.
 *
 * @throws VisionApiException if the API reported an error for the image.
 */
fun AnnotateResponse.toAnalysisResult(): AnalysisResult {
    val image = responses?.firstOrNull() ?: return AnalysisResult(emptyList())
    image.error?.let { throw VisionApiException(it.message ?: "Vision API error") }

    val objects = image.localizedObjectAnnotations.orEmpty().mapNotNull { obj ->
        val vertices = obj.boundingPoly?.normalizedVertices
        if (vertices.isNullOrEmpty()) return@mapNotNull null // no box, nothing to draw
        val xs = vertices.map { it.x ?: 0f } // missing coordinate means 0
        val ys = vertices.map { it.y ?: 0f }
        DetectedObject(
            name = obj.name ?: "Unknown",
            confidence = obj.score ?: 0f,
            box = NormalizedRect(xs.min(), ys.min(), xs.max(), ys.max()),
            categoryId = obj.mid
        )
    }.sortedByDescending { it.confidence }

    // textAnnotations[0] holds the full text block; the rest are individual words.
    val text = image.textAnnotations?.firstOrNull()?.description?.trim()?.ifEmpty { null }

    return AnalysisResult(objects, text)
}
