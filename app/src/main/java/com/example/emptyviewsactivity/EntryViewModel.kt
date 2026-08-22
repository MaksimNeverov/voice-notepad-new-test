package com.example.emptyviewsactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EntryViewModel(private val dao: EntryDao) : ViewModel() {

    private val _entries = dao.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val entries: StateFlow<List<Entry>> = _entries

    fun saveEntry(text: String) {
        if (text.isBlank()) return
        val newEntry = Entry(text = text)
        viewModelScope.launch {
            dao.insert(newEntry)
        }
    }

    // Удаление одной заметки
    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    // Массовое удаление
    fun deleteEntries(ids: List<Int>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            dao.deleteByIds(ids)
        }
    }
}