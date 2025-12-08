package com.paveuu.gust.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StageListConverter {

    @TypeConverter
    fun fromList(stages: List<Stage>): String {
        return Gson().toJson(stages)
    }

    @TypeConverter
    fun toList(json: String): List<Stage> {
        val type = object : TypeToken<List<Stage>>() {}.type
        return Gson().fromJson(json, type)
    }
}
