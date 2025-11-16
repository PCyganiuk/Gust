package com.paveuu.gust

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import com.paveuu.gust.data.CarouselItem
import com.paveuu.gust.data.WorkoutDatabase
import com.paveuu.gust.data.WorkoutRepository
import kotlin.math.*

import com.paveuu.gust.ui.theme.GustTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = WorkoutDatabase.getDatabase(this)
        val repo = WorkoutRepository(db.workoutDao())
        val viewModel = WorkoutViewModel(repo)
        viewModel.load()

        setContent {
            GustTheme {
                Scaffold (
                    bottomBar = { BottomNavBar() }
                ) {
                    contentPadding ->
                    Box (
                        modifier = Modifier.padding(contentPadding)
                    ) {
                        CarouselExerciseList(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar() {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Start", "My data", "Config")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    ) {

        NavigationBar(
            modifier = Modifier
                .padding(2.dp)
                .clip(RoundedCornerShape(24.dp))
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    clip = false
                )
                .background(MaterialTheme.colorScheme.surface),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 8.dp
        ){
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        when (item) {
                            "Start" -> Icon(Icons.Default.Home, contentDescription = item)
                            "My data" -> Icon(Icons.Default.Person, contentDescription = item)
                            "Config" -> Icon(Icons.Default.Settings, contentDescription = item)
                        }
                    },
                    label = { Text(item) },
                    selected = selectedItem == index,
                    onClick = { selectedItem = index }
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
    onConfirm: (CarouselItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var rounds by remember { mutableStateOf("3") }
    var breaths by remember { mutableStateOf("30") }
    var pacing by remember { mutableStateOf("14") }

    // Colors to choose from
    val colorOptions = listOf(
        Color(0xFFFFC1E3),
        Color(0xFFFFD180),
        Color(0xFF80D8FF),
        Color(0xFFA7FFEB),
        Color(0xFFFF9E80),
        Color(0xFFB39DDB),
        Color(0xFF90CAF9)
    )
    var selectedColor by remember { mutableStateOf(colorOptions.first()) }

    // Validation
    val titleValid = title.isNotBlank()
    val roundsValid = rounds.toIntOrNull()?.let { it >= 1 } == true
    val breathsValid = breaths.toIntOrNull()?.let { it >= 10 } == true
    val pacingValid = pacing.toIntOrNull()?.let { it >= 1 } == true

    val allValid = titleValid && roundsValid && breathsValid && pacingValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Workout") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    isError = !titleValid
                )

                TextField(
                    value = rounds,
                    onValueChange = { rounds = it },
                    label = { Text("Rounds (≥1)") },
                    isError = !roundsValid
                )

                TextField(
                    value = breaths,
                    onValueChange = { breaths = it },
                    label = { Text("Breaths per Round (≥10)") },
                    isError = !breathsValid
                )

                TextField(
                    value = pacing,
                    onValueChange = { pacing = it },
                    label = { Text("Breath Pacing (seconds ≥1)") },
                    isError = !pacingValid
                )

                Spacer(Modifier.height(8.dp))
                Text("Color:")

                ColorPickerRow(
                    colors = colorOptions,
                    selected = selectedColor,
                    onSelect = { selectedColor = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = allValid,
                onClick = {
                    onConfirm(
                        CarouselItem(
                            id = 0, // will be reassigned outside
                            title = title.trim(),
                            colorValue = selectedColor.toArgb(),
                            numOfRounds = rounds.toInt(),
                            breathCyclesInRound = breaths.toInt(),
                            breathPacing = pacing.toInt()
                        )
                    )
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WavyBreathingCircle(item: CarouselItem, animate: Boolean) {
    val canvasSize = 300.dp

    val progress by if (animate) {
        val infiniteTransition = rememberInfiniteTransition()
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = item.breathPacing * 1000 * item.breathCyclesInRound, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Canvas(modifier = Modifier.size(canvasSize)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 3.5f
        val waveAmplitude = radius / 4f
        val waveCount = item.breathCyclesInRound
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

        drawPath(path, color = item.color, style = Stroke(width = 3.dp.toPx()))

        val pathMeasure = PathMeasure()
        pathMeasure.setPath(path, false)
        val pathLength = pathMeasure.length
        val pos: Offset = pathMeasure.getPosition(progress * pathLength)

        drawCircle(Color.White, radius = 3.dp.toPx(), center = pos)
    }
}


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
        Box(
            modifier = Modifier
                .height(300.dp)
                .maskClip(MaterialTheme.shapes.extraLarge)
                .clickable{
                    if (item.id == -1) showAddDialog = true
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

            if (item.id == -1) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Workout",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                WavyBreathingCircle(item = item, animate = isFocused)

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                Text(
                    text = item.numOfRounds.toString() + " X " + item.breathCyclesInRound.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )

                Text(
                    text = "≈" + (item.duration/60).toString()+ " mins",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val context = LocalContext.current

    val db = WorkoutDatabase.getDatabase(context)
    val repo = WorkoutRepository(db.workoutDao())
    val viewModel = WorkoutViewModel(repo)
    viewModel.load()
    GustTheme {
        Scaffold (
            bottomBar = { BottomNavBar() }
        ) {
                contentPadding ->
            Box (
                modifier = Modifier.padding(contentPadding)
            ) {
                CarouselExerciseList(viewModel = viewModel)

            }
        }
    }
}