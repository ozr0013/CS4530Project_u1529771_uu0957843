package com.cs4530.drawingapp.model

/**
 * A bounding box in normalized image coordinates: (0,0) is the top-left of the
 * image and (1,1) the bottom-right.
 * Independent of the image pixel size, so the UI can map it onto no matter how the image is displayed.
 */
data class NormalizedRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

data class DetectedObject(
    val name: String,
    val confidence: Float,
    val box: NormalizedRect,
    val categoryId: String? = null
)

data class AnalysisResult(
    val objects: List<DetectedObject>,
    val detectedText: String? = null
) {
    val isEmpty: Boolean get() = objects.isEmpty() && detectedText == null
}

/** The API answered, but reported an error for the request (bad key, bad image, ect.) */
class VisionApiException(message: String) : Exception(message)

/** No Vision API key was configured at build time. */
class MissingApiKeyException : Exception(
    /**"/////////////////////////////////////////"*/
    "No Vision API key is configured. Add VISION_API_KEY=your_key to local.properties and rebuild."
)
