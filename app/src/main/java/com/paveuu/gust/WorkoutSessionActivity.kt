package com.paveuu.gust

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paveuu.gust.data.CarouselItem
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

    var breaths by remember { mutableIntStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) } // <--- Animation state
    Scaffold { pad ->
        Column(
            Modifier
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

            WaveWithCenterDotTracking(
                modifier = Modifier
                    .fillMaxWidth()
                    //.fillMaxHeight(),
                    .height(500.dp),
                workout = workout,
                isAnimating = isAnimating,
                onBreath = {
                    if (breaths < workout.breathCyclesInRound) {
                        breaths += 1
                    }
                }
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Breaths: $breaths / ${workout.breathCyclesInRound}",
                fontSize = 22.sp)
            Button(onClick = {
                isAnimating = !isAnimating // <--- Toggle animation
            }) {
                Text(if (isAnimating) "Stop workout" else "Start workout")
            }
        }
    }
}

@Composable
fun WaveWithCenterDotTracking(
    modifier: Modifier = Modifier,
    workout: CarouselItem,
    isAnimating: Boolean,
    onBreath: () -> Unit
) {
    var waveOffset by remember { mutableFloatStateOf(0f) }
    var lastDotT by remember { mutableFloatStateOf(0f) }
    // Launch or stop animation based on isAnimating
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            var lastTime = 0L
            while (isActive && isAnimating) {
                withFrameMillis { time ->
                    if (lastTime != 0L) {
                        val delta = (time - lastTime) / 2000f // duration in ms
                        waveOffset = (waveOffset + delta) % 1f

                        // Detect when dot reaches the top (middleT % 1 ~ 0.25 for sin wave max)
                        val middleT = 0.5f // dot is at centerX, sin reaches max at 0.25
                        val currentDotT = (middleT + waveOffset) % 1f

                        // Detect crossing from below to above 0.25
                        if (lastDotT < 0.25f && currentDotT >= 0.25f) {
                            onBreath()
                        }
                        lastDotT = currentDotT
                    }
                    lastTime = time
                }
            }
        }
    }

    Canvas(modifier = modifier) {

        val w = size.width
        val h = size.height

        val centerX = w / 2f
        val centerY = h / 2f
        val amplitude = h * 0.25f

        val path = Path()
        val points = 200
        val dx = w / points

        for (i in 0..points) {
            val x = i * dx
            val t = (x / w + waveOffset) % 1f

            val y = centerY + amplitude * sin(2f * PI.toFloat() * t)

            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = workout.color,
            style = Stroke(
                width = 80f,
                cap = StrokeCap.Round
            )
        )

        val middleT = ((centerX / w) + waveOffset) % 1f
        val dotY = centerY + amplitude * sin(2f * PI.toFloat() * middleT)
        val radius = 50f //@TODO dodaj dynamiczną kropkę
        drawCircle(
            color = workout.color.darken(),
            radius = radius,
            center = Offset(centerX, dotY)
        )
    }
}
