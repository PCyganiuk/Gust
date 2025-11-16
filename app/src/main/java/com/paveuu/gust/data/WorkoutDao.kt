package com.paveuu.gust.data

import androidx.room.*

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workouts ORDER BY id ASC")
    suspend fun getAll(): List<CarouselItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CarouselItem)

    @Delete
    suspend fun delete(item: CarouselItem)
}