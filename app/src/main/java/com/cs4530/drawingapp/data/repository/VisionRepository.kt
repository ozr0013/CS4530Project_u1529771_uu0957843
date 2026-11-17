package com.cs4530.drawingapp.data.repository

import com.cs4530.drawingapp.data.remote.VisionApiService
import com.cs4530.drawingapp.data.remote.dto.AnnotateRequest
import com.cs4530.drawingapp.data.remote.toAnalysisResult
import com.cs4530.drawingapp.model.AnalysisResult
import com.cs4530.drawingapp.model.MissingApiKeyException
import com.cs4530.drawingapp.model.VisionApiException
import com.google.gson.JsonParser
import retrofit2.HttpException

/**
 * Talks to the Vision API and turns its replies into domain models.
 * Has no Android dependencies, so it can be unit tested against a mock server.
 */
class VisionRepository(
    private val service: VisionApiService,
    private val apiKey: String
) {

    suspend fun analyze(base64Image: String): AnalysisResult {
        if (apiKey.isBlank()) throw MissingApiKeyException()

        val response = try {
            service.annotateImage(apiKey, AnnotateRequest.forImage(base64Image))
        } catch (e: HttpException) {
            throw VisionApiException(describeHttpError(e))
        }
        return response.toAnalysisResult()
    }

    private fun describeHttpError(e: HttpException): String {
        val apiMessage = runCatching {
            val body = e.response()?.errorBody()?.string().orEmpty()
            JsonParser.parseString(body).asJsonObject
                .getAsJsonObject("error")
                .get("message").asString
        }.getOrNull()
        return "Vision API request failed (HTTP ${e.code()})" +
            (apiMessage?.let { ": $it" } ?: "")
    }
}
