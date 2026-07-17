package com.example.data

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    private val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Double::class.javaObjectType)
    private val adapter = moshi.adapter<Map<String, Double>>(mapType)

    @TypeConverter
    fun fromString(value: String?): Map<String, Double>? {
        return value?.let { adapter.fromJson(it) } ?: emptyMap()
    }

    @TypeConverter
    fun fromMap(map: Map<String, Double>?): String? {
        return adapter.toJson(map ?: emptyMap())
    }
}
