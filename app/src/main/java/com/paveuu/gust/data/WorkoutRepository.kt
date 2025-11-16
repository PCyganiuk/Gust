package com.paveuu.gust.data

class WorkoutRepository(private val dao: WorkoutDao) {

    suspend fun getWorkouts(): List<CarouselItem> = dao.getAll()

    suspend fun insert(item: CarouselItem) = dao.insert(item)
}