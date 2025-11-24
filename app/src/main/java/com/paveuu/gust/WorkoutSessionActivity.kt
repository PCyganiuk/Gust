package com.paveuu.gust

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paveuu.gust.ui.theme.GustTheme
import com.paveuu.gust.data.WorkoutDatabase
import com.paveuu.gust.data.WorkoutRepository

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
    val workout = items.firstOrNull { it.id == workoutId }

    if (workout == null) {
        Text("Workout not found")
        return
    }

    Scaffold { paddingValues ->
        Text(
            text = "Workout: ${workout.title}\nRounds: ${workout.numOfRounds}",
            modifier = Modifier
                .padding(paddingValues)
                .padding(32.dp)
        )
    }
}

