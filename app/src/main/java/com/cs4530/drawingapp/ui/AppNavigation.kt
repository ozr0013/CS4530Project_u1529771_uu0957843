package com.cs4530.drawingapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cs4530.drawingapp.viewmodel.DrawingViewModel

@Composable
fun AppNavigation(viewModel: DrawingViewModel) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()
    val title by viewModel.currentTitle.collectAsState()
    val allDrawings by viewModel.allDrawings.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(onFinished = {
                navController.navigate("gallery") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        composable("gallery") {
            MainGalleryScreen(
                drawings = allDrawings,
                repository = viewModel.repository,
                onOpenDrawing = { id ->
                    viewModel.loadDrawing(id)
                    navController.navigate("canvas/$id")
                },
                onNewDrawing = {
                    viewModel.newDrawing()
                    navController.navigate("canvas/-1")
                },
                onDeleteDrawing = { id ->
                    viewModel.deleteDrawing(id)
                }
            )
        }

        composable(
            route = "canvas/{drawingId}",
            arguments = listOf(navArgument("drawingId") { type = NavType.LongType })
        ) { backStackEntry ->
            val drawingId = backStackEntry.arguments?.getLong("drawingId") ?: -1L
            LaunchedEffect(drawingId) {
                if (drawingId > 0 && viewModel.currentDrawingId.value != drawingId) {
                    viewModel.loadDrawing(drawingId)
                }
            }

            val context = LocalContext.current
            CanvasScreen(
                state = state,
                title = title,
                onTitleChange = viewModel::setTitle,
                onStrokeStart = viewModel::startStroke,
                onStrokeMove = viewModel::addPoint,
                onStrokeEnd = viewModel::endStroke,
                onColorChange = viewModel::setColor,
                onSizeChange = viewModel::setSize,
                onShapeChange = viewModel::setShape,
                onUndo = viewModel::undo,
                onRedo = viewModel::redo,
                onClear = viewModel::clear,
                onSave = {
                    viewModel.saveCurrentDrawing(previewBitmap = null) {
                        navController.popBackStack()
                    }
                },
                onShare = {
                    viewModel.shareDrawing(context)
                },
                onImportImage = { uri ->
                    viewModel.importImageFromUri(context, uri)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
