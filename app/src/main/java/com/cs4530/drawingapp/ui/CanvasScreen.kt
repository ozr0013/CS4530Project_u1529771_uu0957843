package com.cs4530.drawingapp.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cs4530.drawingapp.model.DrawingUiState
import com.cs4530.drawingapp.model.PenShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen(
    state: DrawingUiState,
    title: String,
    onTitleChange: (String) -> Unit,
    onStrokeStart: (Offset) -> Unit,
    onStrokeMove: (Offset) -> Unit,
    onStrokeEnd: () -> Unit,
    onColorChange: (Color) -> Unit,
    onSizeChange: (Float) -> Unit,
    onShapeChange: (PenShape) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onImportImage: (Uri) -> Unit,
    onAnalyze: () -> Unit,
    onDismissAnalysis: () -> Unit,
    onBack: () -> Unit
) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportImage(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        singleLine = true,
                        placeholder = { Text("Drawing Title") },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAnalyze) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = "Analyze with AI Vision",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Import Image from Gallery",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onShare) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share Drawing",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onSave) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Save Drawing",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            DrawingCanvas(
                state = state,
                modifier = Modifier.weight(1f),
                onStrokeStart = onStrokeStart,
                onStrokeMove = onStrokeMove,
                onStrokeEnd = onStrokeEnd
            )
            PenControls(
                pen = state.pen,
                onColorChange = onColorChange,
                onSizeChange = onSizeChange,
                onShapeChange = onShapeChange
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Button(
                    onClick = onClear,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Canvas")
                }
            }
        }
    }

    if (state.isAnalyzing) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Analyzing image with AI Vision...")
                }
            }
        }
    }

    if (state.analysisError != null) {
        AlertDialog(
            onDismissRequest = onDismissAnalysis,
            title = { Text("Analysis Error") },
            text = { Text(state.analysisError) },
            confirmButton = {
                TextButton(onClick = onDismissAnalysis) {
                    Text("OK")
                }
            }
        )
    }

    if (state.analysisResult != null) {
        AlertDialog(
            onDismissRequest = onDismissAnalysis,
            title = { Text("AI Vision Analysis Results") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.analysisResult.detectedText != null) {
                        Text("Detected Text: ${state.analysisResult.detectedText}", fontWeight = FontWeight.Bold)
                    }
                    if (state.analysisResult.objects.isEmpty()) {
                        Text("No objects detected.")
                    } else {
                        Text("Detected Objects (${state.analysisResult.objects.size}):", fontWeight = FontWeight.Bold)
                        LazyColumn(
                            modifier = Modifier.height(200.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(state.analysisResult.objects.size) { index ->
                                val obj = state.analysisResult.objects[index]
                                Text("• ${obj.name} (${(obj.confidence * 100).toInt()}% confidence)")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissAnalysis) {
                    Text("Close")
                }
            }
        )
    }
}
