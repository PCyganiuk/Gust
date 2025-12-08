package com.paveuu.gust.data

class WorkoutRepository(private val dao: WorkoutDao) {

    suspend fun getWorkouts(): List<Workout> = dao.getAll()

    suspend fun insert(item: Workout) = dao.insert(item)
}