package com.paveuu.gust.ui.components

import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paveuu.gust.WorkoutSessionActivity
import com.paveuu.gust.WorkoutViewModel
import com.paveuu.gust.data.Stage
import kotlin.math.cos
import kotlin.math.sin

import com.paveuu.gust.data.Workout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselExerciseList(viewModel: WorkoutViewModel) {

    val items by viewModel.items.collectAsState()

    val carouselState = rememberCarouselState { items.count() }
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddWorkoutDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newItem ->
                viewModel.add(
                    newItem.copy(colorValue = newItem.color.toArgb())
                )
                showAddDialog = false
            }
        )
    }

    HorizontalCenteredHeroCarousel(
        state = carouselState,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 16.dp),
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) { i ->
        val item = items[i]
        val isFocused = carouselState.currentItem == i
        val context = LocalContext.current
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .maskClip(MaterialTheme.shapes.extraLarge)
                .clickable{
                    if (viewModel.isAddButton(item.id)) {
                        showAddDialog = true
                    }
                    else{
                        val intent = Intent(context, WorkoutSessionActivity::class.java).apply {
                            putExtra("workoutId", item.id)
                        }
                        context.startActivity(intent)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.extraLarge
                    )
            )

            if (viewModel.isAddButton(item.id)) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Workout",
                    tint = MaterialTheme.colorScheme.inversePrimary,
                    modifier = Modifier.size(124.dp)
                )
            }
            else {
                WavyBreathingCircle(item = item, animate = isFocused)

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.inversePrimary,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
/*
                Text(
                    text = item.numOfRounds.toString() + " X " + item.breathCyclesInRound.toString(),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 36.sp),
                    color = MaterialTheme.colorScheme.inversePrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
*/
                Text(
                    text = "≈" + (item.duration/60).toString()+ " mins",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.inversePrimary,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun ColorPickerRow(
    colors: List<Color>,
    selected: Color,
    onSelect: (Color) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (color == selected) 3.dp else 1.dp,
                        color = if (color == selected) Color.White else Color.Gray,
                        shape = CircleShape
                    )
                    .clickable { onSelect(color) }
            )
        }
    }
}

@Composable
fun AddWorkoutDialog(
    onDismiss: () -> Unit,
    onConfirm: (Workout) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFFFFC1E3)) }

    // Stage input fields
    var breathIn by remember { mutableStateOf("") }
    var hold by remember { mutableStateOf("") }
    var breathOut by remember { mutableStateOf("") }
    var regenerate by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }

    // List of stages added
    var stages by remember { mutableStateOf(listOf<Stage>()) }

    val colors = listOf(
        Color(0xFFFFC1E3),
        Color(0xFFFFD180),
        Color(0xFF80D8FF),
        Color(0xFFA7FFEB),
        Color(0xFFFF9E80),
        Color(0xFFB39DDB),
        Color(0xFF90CAF9)
    )

    fun stageFieldsValid(): Boolean {
        return breathIn.toIntOrNull() != null &&
                hold.toIntOrNull() != null &&
                breathOut.toIntOrNull() != null &&
                regenerate.toIntOrNull() != null &&
                reps.toIntOrNull()?.let { it >= 1 } == true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Workout") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Title
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Workout Title") }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Stage editor
                Text("Add Stage", style = MaterialTheme.typography.titleMedium)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = breathIn,
                        onValueChange = { breathIn = it },
                        label = { Text("Breath In (sec)") }
                    )
                    TextField(
                        value = hold,
                        onValueChange = { hold = it },
                        label = { Text("Hold (sec)") }
                    )
                    TextField(
                        value = breathOut,
                        onValueChange = { breathOut = it },
                        label = { Text("Breath Out (sec)") }
                    )
                    TextField(
                        value = regenerate,
                        onValueChange = { regenerate = it },
                        label = { Text("Regenerate (sec)") }
                    )
                    TextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text("Reps (≥1)") }
                    )

                    TextButton(
                        enabled = stageFieldsValid(),
                        onClick = {
                            stages = stages + Stage(
                                breathInSeconds = breathIn.toInt(),
                                holdSeconds = hold.toInt(),
                                breathOutSeconds = breathOut.toInt(),
                                regenerateSeconds = regenerate.toInt(),
                                reps = reps.toInt()
                            )
                            // clear inputs
                            breathIn = ""
                            hold = ""
                            breathOut = ""
                            regenerate = ""
                            reps = ""
                        }
                    ) { Text("Add Stage") }

                    // Stage list + remove
                    if (stages.isNotEmpty()) {
                        Text("Stages:", style = MaterialTheme.typography.titleSmall)
                        stages.forEachIndexed { index, s ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Stage ${index + 1}: in ${s.breathInSeconds}s, hold ${s.holdSeconds}s, out ${s.breathOutSeconds}s, reg ${s.regenerateSeconds}s — ${s.reps} reps",
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = {
                                    stages = stages.toMutableList().also { it.removeAt(index) }
                                }) { Text("Remove") }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Color:", style = MaterialTheme.typography.titleMedium)

                ColorPickerRow(colors, selectedColor) {
                    selectedColor = it
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && stages.isNotEmpty(),
                onClick = {
                    onConfirm(
                        Workout(
                            id = 0,
                            title = title.trim(),
                            colorValue = selectedColor.toArgb(),
                            stages = stages
                        )
                    )
                }
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WavyBreathingCircle(item: Workout, animate: Boolean) {

    val progress by if (animate) {
        val infiniteTransition = rememberInfiniteTransition()
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 10 * 1000 * 30, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 3.5f
        val waveAmplitude = radius / 20f
        val waveCount = item.stages[0].reps
        val segments = 360

        val path = Path().apply {
            var prevX = 0f
            var prevY = 0f
            for (angle in 0..segments) {
                val rad = Math.toRadians(angle.toDouble())
                val wave = waveAmplitude * sin(rad * waveCount)
                val x = (center.x + (radius + wave) * cos(rad)).toFloat()
                val y = (center.y + (radius + wave) * sin(rad)).toFloat()

                if (angle == 0) {
                    moveTo(x, y)
                    prevX = x
                    prevY = y
                } else {
                    val midX = (prevX + x) / 2
                    val midY = (prevY + y) / 2
                    quadraticTo(prevX, prevY, midX, midY)
                    prevX = x
                    prevY = y
                }
            }
            close()
        }

        drawPath(path, color = item.color, style = Stroke(width = 7.dp.toPx()))

        val pathMeasure = PathMeasure()
        pathMeasure.setPath(path, false)
        val pathLength = pathMeasure.length
        val pos: Offset = pathMeasure.getPosition(progress * pathLength)

        drawCircle(item.color.darken(), radius = 3.5f.dp.toPx(), center = pos)
    }
}

fun Color.darken(factor: Float = 0.5f): Color {
    return Color(
        red = this.red * factor,
        green = this.green * factor,
        blue = this.blue * factor,
        alpha = this.alpha
    )
}