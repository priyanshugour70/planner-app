package com.lssgoo.planner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.lssgoo.planner.data.model.Note
import com.lssgoo.planner.data.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : BaseViewModel(application) {

    private val repository = DataRepository(application)

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _notes.value = repository.getNotes()
            _isLoading.value = false
        }
    }

    fun addNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createNote(note)
            loadNotes()
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
            loadNotes()
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(noteId)
            loadNotes()
        }
    }

    fun getNoteById(noteId: String): Note? {
        return _notes.value.find { it.id == noteId }
    }

    fun getPinnedNotes(): List<Note> = _notes.value.filter { it.isPinned }

    fun getNotesByCategory(category: String): List<Note> =
        _notes.value.filter { it.category == category }
}
