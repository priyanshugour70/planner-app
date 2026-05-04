package com.lssgoo.planner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.lssgoo.planner.data.local.LocalStorageManager
import com.lssgoo.planner.data.repository.DataRepository
import com.lssgoo.planner.data.search.SearchManager
import com.lssgoo.planner.features.search.models.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : BaseViewModel(application) {

    private val repository = DataRepository(application)
    private val storage = LocalStorageManager(application)
    private val searchManager = SearchManager(storage)

    private val _results = MutableStateFlow<List<SearchResult>>(emptyList())
    val results: StateFlow<List<SearchResult>> = _results.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun search(queryText: String) {
        _query.value = queryText
        if (queryText.isBlank()) {
            _results.value = emptyList()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val localResults = searchManager.search(
                query = queryText,
                goals = storage.getGoals(),
                tasks = storage.getTasks(),
                notes = storage.getNotes(),
                events = storage.getEvents(),
                reminders = storage.getReminders(),
                habits = storage.getHabits(),
                journalEntries = storage.getJournalEntries(),
                transactions = storage.getTransactions()
            )
            _results.value = localResults
            _isLoading.value = false
        }
    }

    fun clearSearch() {
        _query.value = ""
        _results.value = emptyList()
    }
}
