package com.cs4530.drawingapp.data

import com.cs4530.drawingapp.data.remote.VisionApiService
import com.cs4530.drawingapp.data.repository.VisionRepository
import com.cs4530.drawingapp.model.MissingApiKeyException
import com.cs4530.drawingapp.model.VisionApiException
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Verifies the real Retrofit client end to end (request building, HTTP, JSON parsing,
 * error handling) against a local mock server standing in for the Vision API.
 */
class VisionRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: VisionRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = repositoryWithKey("test-key")
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun repositoryWithKey(key: String) =
        VisionRepository(VisionApiService.create(server.url("/").toString()), key)

    private fun enqueueJson(body: String, code: Int = 200) {
        server.enqueue(MockResponse().setResponseCode(code).setBody(body))
    }

    @Test
    fun sendsAnnotateRequestWithKeyImageAndFeatures() = runBlocking {
        enqueueJson("""{"responses":[{}]}""")

        repository.analyze("aGVsbG8=")

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v1/images:annotate", request.requestUrl?.encodedPath)
        assertEquals("test-key", request.requestUrl?.queryParameter("key"))

        val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
        val imageRequest = body.getAsJsonArray("requests")[0].asJsonObject
        assertEquals("aGVsbG8=", imageRequest.getAsJsonObject("image").get("content").asString)
        val features = imageRequest.getAsJsonArray("features").map { it.asJsonObject.get("type").asString }
        assertEquals(listOf("OBJECT_LOCALIZATION", "TEXT_DETECTION"), features)
    }

    @Test
    fun parsesSuccessfulResponseIntoDetectedObjects() = runBlocking {
        enqueueJson(
            """
            {"responses":[{
              "localizedObjectAnnotations":[
                {"mid":"/m/01yrx","name":"Cat","score":0.93,
                 "boundingPoly":{"normalizedVertices":[{"x":0.1,"y":0.2},{"x":0.6,"y":0.2},{"x":0.6,"y":0.9},{"x":0.1,"y":0.9}]}},
                {"mid":"/m/0bt9lr","name":"Dog","score":0.71,
                 "boundingPoly":{"normalizedVertices":[{"x":0.5,"y":0.1},{"x":0.9,"y":0.1},{"x":0.9,"y":0.5},{"x":0.5,"y":0.5}]}}
              ],
              "textAnnotations":[{"description":"HELLO\n"}]
            }]}
            """.trimIndent()
        )

        val result = repository.analyze("img")

        assertEquals(listOf("Cat", "Dog"), result.objects.map { it.name })
        assertEquals(0.93f, result.objects[0].confidence, 1e-6f)
        assertEquals(0.1f, result.objects[0].box.left, 1e-6f)
        assertEquals(0.9f, result.objects[0].box.bottom, 1e-6f)
        assertEquals("/m/01yrx", result.objects[0].categoryId)
        assertEquals("HELLO", result.detectedText)
    }

    @Test
    fun responseWithNoDetectionsGivesEmptyResult() = runBlocking {
        enqueueJson("""{"responses":[{}]}""")

        val result = repository.analyze("img")

        assertTrue(result.isEmpty)
        assertTrue(result.objects.isEmpty())
        assertNull(result.detectedText)
    }

    @Test
    fun httpErrorBecomesVisionApiExceptionWithServerMessage() {
        enqueueJson(
            """{"error":{"code":403,"message":"API key not valid.","status":"PERMISSION_DENIED"}}""",
            code = 403
        )

        val e = assertThrows(VisionApiException::class.java) {
            runBlocking { repository.analyze("img") }
        }
        assertTrue(e.message!!.contains("403"))
        assertTrue(e.message!!.contains("API key not valid."))
    }

    @Test
    fun serverErrorWithoutJsonBodyStillGivesReadableMessage() {
        enqueueJson("Internal Server Error", code = 500)

        val e = assertThrows(VisionApiException::class.java) {
            runBlocking { repository.analyze("img") }
        }
        assertEquals("Vision API request failed (HTTP 500)", e.message)
    }

    @Test
    fun perImageErrorInsideSuccessfulResponseThrows() {
        enqueueJson("""{"responses":[{"error":{"code":3,"message":"Bad image data."}}]}""")

        val e = assertThrows(VisionApiException::class.java) {
            runBlocking { repository.analyze("img") }
        }
        assertEquals("Bad image data.", e.message)
    }

    @Test
    fun droppedConnectionThrowsIOException() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertThrows(IOException::class.java) {
            runBlocking { repository.analyze("img") }
        }
    }

    @Test
    fun missingApiKeyFailsWithoutSendingRequest() {
        val noKey = repositoryWithKey("")

        assertThrows(MissingApiKeyException::class.java) {
            runBlocking { noKey.analyze("img") }
        }
        assertEquals(0, server.requestCount)
    }
}
