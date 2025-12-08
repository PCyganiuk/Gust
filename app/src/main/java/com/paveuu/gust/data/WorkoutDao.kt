package com.paveuu.gust.data

import androidx.room.*

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workouts ORDER BY id ASC")
    suspend fun getAll(): List<Workout>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Workout)

    @Delete
    suspend fun delete(item: Workout)
}