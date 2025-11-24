package com.paveuu.gust

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paveuu.gust.data.CarouselItem
import com.paveuu.gust.data.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WorkoutViewModel( private val repo: WorkoutRepository ) : ViewModel() {
    companion object {
        const val ADD_BUTTON_ID = -1
    }
    private val _items = MutableStateFlow<List<CarouselItem>>(emptyList())
    val items: StateFlow<List<CarouselItem>> = _items

    fun load() {
        viewModelScope.launch {
            val workouts = repo.getWorkouts()
            val addButton = CarouselItem(
                id = ADD_BUTTON_ID,
                title = "Add",
                colorValue = 0xFF888888.toInt()
            )
            _items.value = workouts + addButton
        }
    }

    fun add(workout: CarouselItem) {
        viewModelScope.launch {
            repo.insert(workout)
            load()
        }
    }

    fun getWorkoutById(id: Int): CarouselItem? {
        return _items.value.firstOrNull { it.id == id }
    }

    fun isAddButton(id: Int): Boolean = id == ADD_BUTTON_ID
}