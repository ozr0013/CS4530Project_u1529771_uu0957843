package com.cs4530.drawingapp

import androidx.compose.ui.geometry.Offset
import com.cs4530.drawingapp.model.AnalysisResult
import com.cs4530.drawingapp.model.DetectedObject
import com.cs4530.drawingapp.model.MissingApiKeyException
import com.cs4530.drawingapp.model.NormalizedRect
import com.cs4530.drawingapp.model.VisionApiException
import com.cs4530.drawingapp.viewmodel.DrawingViewModel
import com.cs4530.drawingapp.viewmodel.analysisErrorMessage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Checks the ViewModel stays stable and shows sensible state for every analysis outcome. */
@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModelTest {

    private lateinit var vm: DrawingViewModel

    private val cat = DetectedObject("Cat", 0.9f, NormalizedRect(0.1f, 0.1f, 0.5f, 0.5f), "/m/01yrx")

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        vm = DrawingViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun successStoresResultAndStopsLoading() {
        vm.runAnalysis(null) { AnalysisResult(listOf(cat), "hello") }

        val s = vm.state.value
        assertFalse(s.isAnalyzing)
        assertNull(s.analysisError)
        assertEquals(listOf(cat), s.analysisResult?.objects)
        assertEquals("hello", s.analysisResult?.detectedText)
    }

    @Test
    fun showsLoadingWhileRequestIsInFlight() {
        val pending = CompletableDeferred<AnalysisResult>()
        vm.runAnalysis(null) { pending.await() }

        assertTrue(vm.state.value.isAnalyzing)
        assertNull(vm.state.value.analysisResult)

        pending.complete(AnalysisResult(emptyList()))
        assertFalse(vm.state.value.isAnalyzing)
    }

    @Test
    fun noDetectionsIsAValidEmptyResultNotAnError() {
        vm.runAnalysis(null) { AnalysisResult(emptyList()) }

        val s = vm.state.value
        assertFalse(s.isAnalyzing)
        assertNull(s.analysisError)
        assertNotNull(s.analysisResult)
        assertTrue(s.analysisResult!!.isEmpty)
    }

    @Test
    fun networkFailureBecomesErrorInsteadOfCrash() {
        vm.runAnalysis(null) { throw UnknownHostException("vision.googleapis.com") }

        val s = vm.state.value
        assertFalse(s.isAnalyzing)
        assertNull(s.analysisResult)
        assertEquals("No internet connection. Check your network and try again.", s.analysisError)
    }

    @Test
    fun apiErrorMessageIsShownToUser() {
        vm.runAnalysis(null) { throw VisionApiException("Vision API request failed (HTTP 403): API key not valid.") }

        assertEquals("Vision API request failed (HTTP 403): API key not valid.", vm.state.value.analysisError)
    }

    @Test
    fun unexpectedExceptionIsAlsoHandled() {
        vm.runAnalysis(null) { throw IllegalStateException("boom") }

        assertFalse(vm.state.value.isAnalyzing)
        assertEquals("Failed to analyze image: boom", vm.state.value.analysisError)
    }

    @Test
    fun retryAfterFailureClearsErrorAndSucceeds() {
        vm.runAnalysis(null) { throw IOException("reset") }
        assertNotNull(vm.state.value.analysisError)

        vm.runAnalysis(null) { AnalysisResult(listOf(cat)) }

        assertNull(vm.state.value.analysisError)
        assertEquals(1, vm.state.value.analysisResult?.objects?.size)
    }

    @Test
    fun dismissingErrorKeepsPreviousState() {
        vm.runAnalysis(null) { throw IOException("x") }
        vm.dismissAnalysisError()

        assertNull(vm.state.value.analysisError)
        assertFalse(vm.state.value.isAnalyzing)
    }

    @Test
    fun clearAnalysisRemovesBoxes() {
        vm.runAnalysis(null) { AnalysisResult(listOf(cat)) }
        vm.clearAnalysis()

        assertNull(vm.state.value.analysisResult)
    }

    @Test
    fun clearingCanvasAlsoClearsAnalysis() {
        vm.startStroke(Offset(1f, 1f))
        vm.endStroke()
        vm.runAnalysis(null) { AnalysisResult(listOf(cat)) }

        vm.clear()

        assertNull(vm.state.value.analysisResult)
        assertTrue(vm.state.value.strokes.isEmpty())
    }

    @Test
    fun newDrawingClearsAnalysis() {
        vm.runAnalysis(null) { AnalysisResult(listOf(cat)) }
        vm.newDrawing()

        assertNull(vm.state.value.analysisResult)
    }

    @Test
    fun errorMessagesAreReadableForEachFailureType() {
        assertTrue(analysisErrorMessage(MissingApiKeyException()).contains("VISION_API_KEY"))
        assertEquals(
            "The Vision API took too long to respond. Try again.",
            analysisErrorMessage(SocketTimeoutException())
        )
        assertEquals("Network error: reset", analysisErrorMessage(IOException("reset")))
    }
}
