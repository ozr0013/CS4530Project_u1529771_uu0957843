package com.cs4530.drawingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs4530.drawingapp.ui.AppNavigation
import com.cs4530.drawingapp.ui.theme.DrawingAppTheme
import com.cs4530.drawingapp.viewmodel.DrawingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawingAppTheme {
                val viewModel: DrawingViewModel = viewModel()
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
