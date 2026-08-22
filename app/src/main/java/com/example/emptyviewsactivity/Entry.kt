package com.example.emptyviewsactivity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    // Время создания заметки (в миллисекундах)
    val timestamp: Long = System.currentTimeMillis()
)