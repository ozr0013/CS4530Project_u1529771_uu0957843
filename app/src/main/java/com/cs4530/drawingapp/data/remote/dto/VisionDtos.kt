package com.cs4530.drawingapp.data.remote.dto

/*
 * Wire-format types for the Google Cloud Vision `images:annotate` REST endpoint.
 * https://cloud.google.com/vision/docs/reference/rest/v1/images/annotate
 *
 * Response fields are all nullable on purpose: Vision omits zero/empty values
 * (e.g. a vertex at x = 0 arrives as {"y": 0.3}), and Gson bypasses Kotlin
 * constructors, so non-null defaults would not be applied anyway.
 */

/** ---------- Request ---------- */

data class AnnotateRequest(val requests: List<AnnotateImageRequest>) {
    companion object {
        /** Asks for object localization and text detection for an image */
        fun forImage(base64Content: String) = AnnotateRequest(
            listOf(
                AnnotateImageRequest(
                    image = VisionImage(base64Content),
                    features = listOf(
                        Feature("OBJECT_LOCALIZATION", 10),
                        Feature("TEXT_DETECTION", 1)
                    )
                )
            )
        )
    }
}

data class AnnotateImageRequest(val image: VisionImage, val features: List<Feature>)

data class VisionImage(val content: String)

data class Feature(val type: String, val maxResults: Int)

/** ---------- Response ---------- */

data class AnnotateResponse(val responses: List<AnnotateImageResponse>?)

data class AnnotateImageResponse(
    val localizedObjectAnnotations: List<LocalizedObjectAnnotationDto>?,
    val textAnnotations: List<TextAnnotationDto>?,
    val error: StatusDto?
)

data class LocalizedObjectAnnotationDto(
    val mid: String?,
    val name: String?,
    val score: Float?,
    val boundingPoly: BoundingPolyDto?
)

data class TextAnnotationDto(val description: String?)

data class BoundingPolyDto(val normalizedVertices: List<NormalizedVertexDto>?)

data class NormalizedVertexDto(val x: Float?, val y: Float?)

data class StatusDto(val message: String?)
