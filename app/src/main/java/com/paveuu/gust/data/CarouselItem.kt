package com.paveuu.gust.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class CarouselItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val colorValue: Int,
    val numOfRounds: Int = 3,
    val breathCyclesInRound: Int = 30,
    val secondsToHold: Int = 10,
    val breathPacing: Int = 5,
    val holdFor: Int = 30,
    val holdAfter: Boolean = true,
) {
    val color: Color get() = Color(colorValue)

    val duration: Int
        get() = numOfRounds * breathCyclesInRound * breathPacing + secondsToHold * holdFor
}