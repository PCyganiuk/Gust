package com.paveuu.gust

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext

import com.paveuu.gust.data.WorkoutDatabase
import com.paveuu.gust.data.WorkoutRepository
import com.paveuu.gust.ui.components.BottomNavBar
import com.paveuu.gust.ui.components.CarouselExerciseList
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