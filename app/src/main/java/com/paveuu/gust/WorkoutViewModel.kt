package com.paveuu.gust

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paveuu.gust.data.CarouselItem
import com.paveuu.gust.data.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val repo: WorkoutRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<CarouselItem>>(emptyList())
    val items: StateFlow<List<CarouselItem>> = _items

    fun load() {
        viewModelScope.launch {
            _items.value = repo.getWorkouts() + listOf(
                CarouselItem(id = -1, title = "Add", colorValue = 0xFF888888.toInt())
            )
        }
    }

    fun add(workout: CarouselItem) {
        viewModelScope.launch {
            repo.insert(workout)
            load()
        }
    }
}