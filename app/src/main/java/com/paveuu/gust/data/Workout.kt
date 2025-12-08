package com.paveuu.gust.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val colorValue: Int,

    val stages: List<Stage>
) {
    val color: Color get() = Color(colorValue)

    val duration: Int
        get() = stages.sumOf { stage ->
            (stage.breathInSeconds +
                    stage.holdSeconds +
                    stage.breathOutSeconds +
                    stage.regenerateSeconds) * stage.reps
        }
    val totalReps: Int
        get() = stages.sumOf { it.reps }
}

data class Stage(
    val breathInSeconds: Int,
    val holdSeconds: Int,
    val breathOutSeconds: Int,
    val regenerateSeconds: Int,
    val reps: Int
)