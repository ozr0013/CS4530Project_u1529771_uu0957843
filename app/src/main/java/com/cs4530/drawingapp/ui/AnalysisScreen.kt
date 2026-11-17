package com.cs4530.drawingapp.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.cs4530.drawingapp.model.DetectedObject
import com.cs4530.drawingapp.model.DrawingUiState

/**
 * Full-screen view of the latest Vision API analysis: the analyzed image with bounding boxes,
 * and a list of detected objects with confidence scores. Tapping a row highlights its box.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    state: DrawingUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    var highlighted by remember { mutableStateOf<Int?>(null) }
    val result = state.analysisResult

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Vision Analysis") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to canvas")
                    }
                },
                actions = {
                    IconButton(onClick = onRetry, enabled = !state.isAnalyzing) {
                        Icon(Icons.Default.Refresh, contentDescription = "Analyze again")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AnalyzedImage(state, highlighted)
            }

            when {
                state.isAnalyzing -> item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Analyzing image…")
                    }
                }

                state.analysisError != null -> item {
                    MessageCard(
                        title = "Analysis failed",
                        body = state.analysisError,
                        actionLabel = "Try again",
                        onAction = onRetry
                    )
                }

                result == null -> item {
                    MessageCard(
                        title = "No analysis yet",
                        body = "Import an image or tap Analyze on the canvas to detect objects.",
                        actionLabel = "Analyze now",
                        onAction = onRetry
                    )
                }

                else -> {
                    item {
                        Text(
                            text = if (result.objects.isEmpty()) "No objects detected"
                            else "${result.objects.size} object${if (result.objects.size == 1) "" else "s"} detected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (result.objects.isEmpty()) {
                        item {
                            Text(
                                "The Vision API didn't find any objects in this image. " +
                                    "Try a photo with clearer, larger subjects.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    itemsIndexed(result.objects) { index, obj ->
                        DetectionRow(
                            index = index,
                            obj = obj,
                            selected = highlighted == index,
                            onClick = { highlighted = if (highlighted == index) null else index }
                        )
                    }
                    result.detectedText?.let { text ->
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("Detected text", fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.size(4.dp))
                                    Text(text, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** The analyzed image, letterboxed to keep its aspect ratio, with boxes drawn on top. */
@Composable
private fun AnalyzedImage(state: DrawingUiState, highlighted: Int?) {
    val image = state.analyzedImage ?: return
    val bitmap = remember(image) { image.asImageBitmap() }
    val aspect = image.width.toFloat() / image.height.coerceAtLeast(1)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp)
            .aspectRatio(aspect.coerceIn(0.5f, 2f))
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            // Fit the image inside the canvas and map boxes onto the area it actually covers.
            val scale = minOf(size.width / image.width, size.height / image.height)
            val drawn = Size(image.width * scale, image.height * scale)
            val origin = Offset((size.width - drawn.width) / 2f, (size.height - drawn.height) / 2f)
            drawImage(
                image = bitmap,
                dstOffset = IntOffset(origin.x.toInt(), origin.y.toInt()),
                dstSize = IntSize(drawn.width.toInt(), drawn.height.toInt())
            )
            state.analysisResult?.let {
                drawDetections(it.objects, origin, drawn, highlighted)
            }
        }
    }
}

@Composable
private fun DetectionRow(index: Int, obj: DetectedObject, selected: Boolean, onClick: () -> Unit) {
    val color = detectionColor(index)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(obj.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text(confidenceLabel(obj.confidence), fontWeight = FontWeight.Bold, color = color)
                }
                Spacer(Modifier.size(6.dp))
                LinearProgressIndicator(
                    progress = { obj.confidence.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = color
                )
                obj.categoryId?.let {
                    Spacer(Modifier.size(4.dp))
                    Text(
                        "Category ID: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageCard(title: String, body: String, actionLabel: String, onAction: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(body, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onAction) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(actionLabel)
            }
        }
    }
}
