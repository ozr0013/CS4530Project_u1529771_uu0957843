package com.cs4530.drawingapp.data

import com.cs4530.drawingapp.data.remote.dto.AnnotateRequest
import com.cs4530.drawingapp.data.remote.dto.AnnotateResponse
import com.cs4530.drawingapp.data.remote.toAnalysisResult
import com.cs4530.drawingapp.model.NormalizedRect
import com.cs4530.drawingapp.model.VisionApiException
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class VisionResponseMapperTest {

    private val gson = Gson()

    private fun parse(json: String) =
        gson.fromJson(json, AnnotateResponse::class.java).toAnalysisResult()

    @Test
    fun parsesObjectsWithNamesScoresAndBoxes() {
        val result = parse(
            """
            {"responses":[{"localizedObjectAnnotations":[
              {"mid":"/m/01bqk0","name":"Bicycle wheel","score":0.89,
               "boundingPoly":{"normalizedVertices":[
                 {"x":0.25,"y":0.75},{"x":0.5,"y":0.75},{"x":0.5,"y":1.0},{"x":0.25,"y":1.0}]}},
              {"mid":"/m/0199g","name":"Bicycle","score":0.95,
               "boundingPoly":{"normalizedVertices":[
                 {"x":0.25,"y":0.5},{"x":0.75,"y":0.5},{"x":0.75,"y":1.0},{"x":0.25,"y":1.0}]}}
            ]}]}
            """
        )

        // Sorted by confidence, highest first
        assertEquals(listOf("Bicycle", "Bicycle wheel"), result.objects.map { it.name })
        val bike = result.objects[0]
        assertEquals(0.95f, bike.confidence)
        assertEquals("/m/0199g", bike.categoryId)
        assertEquals(NormalizedRect(0.25f, 0.5f, 0.75f, 1.0f), bike.box)
        assertNull(result.detectedText)
        assertFalse(result.isEmpty)
    }

    @Test
    fun missingCoordinatesAreTreatedAsZero() {
        // Vision omits zero-valued fields: the first vertex is really (0, 0).
        val result = parse(
            """
            {"responses":[{"localizedObjectAnnotations":[
              {"name":"Person","score":0.7,
               "boundingPoly":{"normalizedVertices":[{},{"x":0.5},{"x":0.5,"y":0.75},{"y":0.75}]}}
            ]}]}
            """
        )

        assertEquals(NormalizedRect(0f, 0f, 0.5f, 0.75f), result.objects.single().box)
    }

    @Test
    fun annotationsWithoutBoundingBoxAreSkipped() {
        val result = parse(
            """
            {"responses":[{"localizedObjectAnnotations":[
              {"name":"Ghost","score":0.9},
              {"name":"Empty","score":0.9,"boundingPoly":{"normalizedVertices":[]}},
              {"name":"Dog","score":0.6,"boundingPoly":{"normalizedVertices":[{"x":0.1,"y":0.1},{"x":0.2,"y":0.2}]}}
            ]}]}
            """
        )

        assertEquals(listOf("Dog"), result.objects.map { it.name })
    }

    @Test
    fun missingNameAndScoreGetDefaults() {
        val result = parse(
            """
            {"responses":[{"localizedObjectAnnotations":[
              {"boundingPoly":{"normalizedVertices":[{"x":0.1,"y":0.1},{"x":0.2,"y":0.2}]}}
            ]}]}
            """
        )

        val obj = result.objects.single()
        assertEquals("Unknown", obj.name)
        assertEquals(0f, obj.confidence)
        assertNull(obj.categoryId)
    }

    @Test
    fun extractsFullDetectedText() {
        val result = parse(
            """
            {"responses":[{"textAnnotations":[
              {"description":"STOP\nAHEAD\n"}, {"description":"STOP"}, {"description":"AHEAD"}
            ]}]}
            """
        )

        assertEquals("STOP\nAHEAD", result.detectedText)
        assertTrue(result.objects.isEmpty())
        assertFalse(result.isEmpty)
    }

    @Test
    fun noDetectionsGivesEmptyResult() {
        // {"responses":[{}]} is what Vision returns when it finds nothing.
        for (json in listOf("""{"responses":[{}]}""", """{"responses":[]}""", "{}")) {
            assertTrue(json, parse(json).isEmpty)
        }
    }

    @Test
    fun perImageErrorThrowsVisionApiException() {
        val e = assertThrows(VisionApiException::class.java) {
            parse("""{"responses":[{"error":{"code":3,"message":"Bad image data."}}]}""")
        }
        assertEquals("Bad image data.", e.message)
    }

    @Test
    fun requestSerializesToExpectedShape() {
        assertEquals(
            """{"requests":[{"image":{"content":"QUJD"},"features":[""" +
                """{"type":"OBJECT_LOCALIZATION","maxResults":10},""" +
                """{"type":"TEXT_DETECTION","maxResults":1}]}]}""",
            gson.toJson(AnnotateRequest.forImage("QUJD"))
        )
    }
}
