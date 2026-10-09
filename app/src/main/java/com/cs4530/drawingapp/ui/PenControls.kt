package com.cs4530.drawingapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cs4530.drawingapp.model.PenSettings
import com.cs4530.drawingapp.model.PenShape

private val penColors = listOf(
    Color.Black,
    Color.Red,
    Color(0xFFFF9800),
    Color(0xFFFFEB3B),
    Color(0xFF4CAF50),
    Color.Blue,
    Color(0xFF9C27B0)
)

/** Color swatches, a size slider and a shape selector for the current [pen]. Stateless. */
@Composable
fun PenControls(
    pen: PenSettings,
    onColorChange: (Color) -> Unit,
    onSizeChange: (Float) -> Unit,
    onShapeChange: (PenShape) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            penColors.forEach { color ->
                val selected = color == pen.color
                Row(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color, CircleShape)
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { onColorChange(color) }
                ) {}
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Size ${pen.size.toInt()}")
            Slider(
                value = pen.size,
                onValueChange = onSizeChange,
                valueRange = PenSettings.MIN_SIZE..PenSettings.MAX_SIZE,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PenShape.entries.forEach { shape ->
                FilterChip(
                    selected = shape == pen.shape,
                    onClick = { onShapeChange(shape) },
                    label = { Text(shape.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PenControlsPreview() {
    PenControls(
        pen = PenSettings(color = Color.Blue, size = 24f, shape = PenShape.SQUARE),
        onColorChange = {},
        onSizeChange = {},
        onShapeChange = {}
    )
}
