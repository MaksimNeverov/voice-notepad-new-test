package com.example.emptyviewsactivity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Insert
    suspend fun insert(entry: Entry)

    // Самое важное: ORDER BY timestamp DESC — новые заметки первыми
    @Query("SELECT * FROM entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<Entry>>

    // Для группировки по дням (если захочешь потом)
    @Query("SELECT * FROM entries WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getEntriesForDay(startTime: Long): Flow<List<Entry>>

    // --- НОВОЕ: удаление по ID ---
    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    // Если захочешь удалять сразу много (по списку ID)
    @Query("DELETE FROM entries WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)
}

