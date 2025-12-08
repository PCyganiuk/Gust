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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paveuu.gust.data.Workout
import com.paveuu.gust.ui.theme.GustTheme
import com.paveuu.gust.data.WorkoutDatabase
import com.paveuu.gust.data.WorkoutRepository
import com.paveuu.gust.ui.components.darken
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.sin

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
                    .height(500.dp) // viewport height
            ) {
                WaveWithStages(
                    modifier = Modifier.fillMaxSize(),
                    workout = workout,
                    isAnimating = isAnimating
                )
            }

            Spacer(Modifier.height(32.dp))

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
    val totalDuration = workout.duration * 1000L // in ms
    var elapsedTime by remember { mutableStateOf(0L) }

    // Animate elapsed time
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            val startTime = System.currentTimeMillis()
            while (isActive) {
                elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime > totalDuration) elapsedTime = 0L
                kotlinx.coroutines.delay(16)
            }
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerY = h / 2f

        // Build timeline of stages
        val timeline = mutableListOf<Pair<Long, Float>>() // time from start -> vertical fraction 0..1
        var accTime = 0L

        for (stage in workout.stages) {
            repeat(stage.reps) {
                val durations = listOf(
                    stage.breathInSeconds * 1000L,
                    stage.holdSeconds * 1000L,
                    stage.breathOutSeconds * 1000L,
                    stage.regenerateSeconds * 1000L
                )
                val targets = listOf(1f, 1f, 0f, 0f) // 0=bottom, 1=top
                var segAcc = 0L
                for (i in targets.indices) {
                    timeline.add(accTime + segAcc to targets[i])
                    segAcc += durations[i]
                }
                accTime += segAcc
            }
        }

        // Smooth interpolation
        val points = mutableListOf<Offset>()
        val step = 16L // ms per point
        var t = 0L
        while (t <= totalDuration) {
            val fraction = timeline.interpolatedY(t)
            val x = w - (elapsedTime.toFloat() / totalDuration) * w + (t.toFloat() / totalDuration) * w
            val y = centerY - (fraction - 0.5f) * h
            points.add(Offset(x, y))
            t += step
        }

        val path = Path().apply {
            if (points.isNotEmpty()) moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }

        drawPath(
            path = path,
            color = workout.color,
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )

        // Fixed dot in vertical center
        drawCircle(
            color = workout.color.darken(),
            radius = 20f,
            Offset(w / 2f, centerY)
        )
    }
}

// Extension to interpolate timeline
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
