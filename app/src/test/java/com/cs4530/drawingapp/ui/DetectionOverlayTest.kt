package com.cs4530.drawingapp.ui

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.cs4530.drawingapp.model.NormalizedRect
import org.junit.Assert.assertEquals
import org.junit.Test

/** Checks that bounding boxes land on the right pixels when the image is letterboxed. */
class DetectionOverlayTest {

    @Test
    fun wideImageIsCenteredVertically() {
        // 200x100 image in a 100x100 box -> scaled to 100x50, 25px bars top and bottom
        assertEquals(Rect(0f, 25f, 100f, 75f), fitInside(Size(200f, 100f), Size(100f, 100f)))
    }

    @Test
    fun tallImageIsCenteredHorizontally() {
        // 100x200 image in a 100x100 box -> scaled to 50x100, 25px bars left and right
        assertEquals(Rect(25f, 0f, 75f, 100f), fitInside(Size(100f, 200f), Size(100f, 100f)))
    }

    @Test
    fun boxIsMappedOntoTheImageAreaNotTheWholeCanvas() {
        val area = Rect(0f, 25f, 100f, 75f) // letterboxed image from the first test

        assertEquals(Rect(0f, 25f, 100f, 75f), NormalizedRect(0f, 0f, 1f, 1f).toScreen(area))
        assertEquals(Rect(25f, 37.5f, 75f, 62.5f), NormalizedRect(0.25f, 0.25f, 0.75f, 0.75f).toScreen(area))
    }
}
