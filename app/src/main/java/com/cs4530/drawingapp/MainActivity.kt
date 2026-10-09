package com.cs4530.drawingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs4530.drawingapp.ui.DrawingCanvas
import com.cs4530.drawingapp.ui.PenControls
import com.cs4530.drawingapp.ui.SplashScreen
import com.cs4530.drawingapp.ui.theme.DrawingAppTheme
import com.cs4530.drawingapp.viewmodel.DrawingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawingAppTheme {
                // rememberSaveable so rotating the phone doesn't replay the splash.
                var showSplash by rememberSaveable { mutableStateOf(true) }
                val viewModel: DrawingViewModel = viewModel()
                val state by viewModel.state.collectAsState()
                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Column(modifier = Modifier.padding(innerPadding)) {
                            DrawingCanvas(
                                state = state,
                                modifier = Modifier.weight(1f),
                                onStrokeStart = viewModel::startStroke,
                                onStrokeMove = viewModel::addPoint,
                                onStrokeEnd = viewModel::endStroke
                            )
                            PenControls(
                                pen = state.pen,
                                onColorChange = viewModel::setColor,
                                onSizeChange = viewModel::setSize,
                                onShapeChange = viewModel::setShape
                            )
                            Button(
                                onClick = viewModel::clear,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text("Clear")
                            }
                        }
                    }
                }
            }
        }
    }
}
