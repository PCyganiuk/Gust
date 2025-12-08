package com.paveuu.gust

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.paveuu.gust.data.Workout
import com.paveuu.gust.ui.theme.GustTheme
import com.paveuu.gust.data.WorkoutDatabase
import com.paveuu.gust.data.WorkoutRepository
import com.paveuu.gust.ui.components.darken

class WorkoutSessionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val workoutId = intent.getIntExtra("workoutId", -1)

        val db = WorkoutDatabase.getDatabase(this)
        val repo = WorkoutRepository(db.workoutDao())
        val viewModel = WorkoutViewModel(repo)

        viewModel.load()

        setContent {
            GustTheme {
                WorkoutSessionScreen(viewModel, workoutId)
            }
        }
    }
}

@Composable
fun WorkoutSessionScreen(viewModel: WorkoutViewModel, workoutId: Int) {
    val items by viewModel.items.collectAsState()
    val workout = items.firstOrNull { it.id == workoutId } ?: return Text("Not found")

    var isAnimating by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableLongStateOf(0L) }

    // Animate elapsed time for timer
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            val startTime = System.currentTimeMillis()
            while (true) {
                elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime > workout.duration * 1000L) elapsedTime = 0L
                kotlinx.coroutines.delay(16)
            }
        }
    }

    Scaffold { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(24.dp)
        ) {
            Text(
                text = workout.title,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.inversePrimary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                WaveWithStages(
                    modifier = Modifier.fillMaxSize(),
                    workout = workout,
                    isAnimating = isAnimating
                )
            }

            Spacer(Modifier.height(32.dp))

            WorkoutTimer(workout = workout, elapsedTime = elapsedTime)

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .height(100.dp),
                border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                onClick = { isAnimating = !isAnimating }
            ) {
                Text(
                    style = MaterialTheme.typography.displayMedium,
                    color = if (isAnimating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    text = if (isAnimating) "Stop workout" else "Start workout"
                )
            }
        }
    }
}

@Composable
fun WaveWithStages(
    modifier: Modifier = Modifier,
    workout: Workout,
    isAnimating: Boolean
) {
    val totalDuration = workout.duration * 1000L
    var elapsedTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            val startTime = System.currentTimeMillis()
            while (true) {
                elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime > totalDuration) elapsedTime = 0L
                kotlinx.coroutines.delay(16)
            }
        }
    }

    val dotRadius = remember { Animatable(20f) }
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            dotRadius.animateTo(
                targetValue = 30f, // max radius
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = LinearEasing), // half second up
                    repeatMode = RepeatMode.Reverse // back down
                )
            )
        } else {
            dotRadius.snapTo(20f) // reset when stopped
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height / 2
        val centerX = w / 2f

        val timeline = mutableListOf<Pair<Long, Float>>()
        var accTime = 0L
        for (stage in workout.stages) {
            repeat(stage.reps) {
                val durations = listOf(
                    stage.breathInSeconds * 1000L,
                    stage.holdSeconds * 1000L,
                    stage.breathOutSeconds * 1000L,
                    stage.regenerateSeconds * 1000L
                )
                val targets = listOf(0f, 0f, 1f, 1f)
                var segAcc = 0L
                for (i in targets.indices) {
                    timeline.add(accTime + segAcc to targets[i])
                    segAcc += durations[i]
                }
                accTime += segAcc
            }
        }

        val points = mutableListOf<Offset>()
        val step = 16L
        val stretchFactor = 8f
        var t = 0L
        while (t <= totalDuration) {
            val fraction = timeline.interpolatedY(t)

            val progress = t.toFloat() / totalDuration
            val x = centerX + (progress * w * stretchFactor) - (elapsedTime.toFloat() / totalDuration) * w * stretchFactor
            val y = h - (fraction - 0.5f) * h

            if (t == 0L && timeline.isNotEmpty()) {
                val firstFraction = timeline.first().second
                points.add(Offset(x, h - (firstFraction - 0.5f) * h))
            } else {
                points.add(Offset(x, y))
            }
            t += step
        }

        val path = Path()
        if (points.isNotEmpty()) {
            path.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val midX = (prev.x + curr.x) / 2
                val midY = (prev.y + curr.y) / 2
                path.quadraticTo(prev.x, prev.y, midX, midY)
            }
            path.lineTo(points.last().x, points.last().y)
        }

        drawPath(
            path = path,
            color = workout.color,
            style = Stroke(width = 50f, cap = StrokeCap.Round)
        )

        val centerFraction = ((elapsedTime / totalDuration.toFloat()) * totalDuration).toLong()
        val dotFraction = timeline.interpolatedY(centerFraction)
        val dotY = h - (dotFraction - 0.5f) * h

        drawCircle(
            color = workout.color.darken(),
            radius = dotRadius.value,
            center = Offset(centerX, dotY)
        )
    }
}

private fun List<Pair<Long, Float>>.interpolatedY(time: Long): Float {
    if (isEmpty()) return 0.5f
    if (time <= first().first) return first().second
    if (time >= last().first) return last().second

    for (i in 1 until size) {
        val (t0, y0) = this[i - 1]
        val (t1, y1) = this[i]
        if (time in t0..t1) {
            val f = (time - t0).toFloat() / (t1 - t0).toFloat()
            return y0 + f * (y1 - y0)
        }
    }
    return 0.5f
}

@Composable
fun WorkoutTimer(workout: Workout, elapsedTime: Long) {
    val (currentAction, remainingSeconds) = remember(elapsedTime) {
        getCurrentActionAndRemainingTime(workout, elapsedTime)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$currentAction: $remainingSeconds s",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

private fun getCurrentActionAndRemainingTime(workout: Workout, elapsedTime: Long): Pair<String, Int> {
    val getReadyMs = workout.stages[0].regenerateSeconds * 1000L
    if (elapsedTime < getReadyMs) {
        return "Get Ready" to ((getReadyMs - elapsedTime) / 1000).toInt() + 1
    }

    val actions = listOf("Breath In", "Hold", "Breath Out", "Relax")
    var accTime = getReadyMs

    for (stage in workout.stages) {
        repeat(stage.reps) {
            val durations = listOf(
                stage.breathInSeconds * 1000L,
                stage.holdSeconds * 1000L,
                stage.breathOutSeconds * 1000L,
                stage.regenerateSeconds * 1000L
            )
            for (i in durations.indices) {
                val start = accTime
                val end = accTime + durations[i]
                if (elapsedTime in start until end) {
                    val remainingMs = end - elapsedTime
                    return actions[i] to ((remainingMs / 1000).toInt() + 1)
                }
                accTime = end
            }
        }
    }
    return "Finished" to 0
}
