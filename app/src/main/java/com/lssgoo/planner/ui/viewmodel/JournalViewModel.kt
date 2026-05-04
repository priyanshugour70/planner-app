package com.lssgoo.planner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.lssgoo.planner.data.model.JournalEntry
import com.lssgoo.planner.data.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JournalViewModel(application: Application) : BaseViewModel(application) {

    private val repository = DataRepository(application)

    private val _entries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val entries: StateFlow<List<JournalEntry>> = _entries.asStateFlow()

    init {
        loadEntries()
    }

    fun loadEntries() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _entries.value = repository.getJournalEntries()
            _isLoading.value = false
        }
    }

    fun addEntry(entry: JournalEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createJournalEntry(entry)
            loadEntries()
        }
    }

    fun getEntryById(entryId: String): JournalEntry? =
        _entries.value.find { it.id == entryId }

    fun getEntriesByMood(mood: com.lssgoo.planner.features.journal.models.JournalMood): List<JournalEntry> =
        _entries.value.filter { it.mood == mood }
}
