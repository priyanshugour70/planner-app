package com.lssgoo.planner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.lssgoo.planner.data.model.*
import com.lssgoo.planner.data.repository.DataRepository
import com.lssgoo.planner.features.habits.models.Habit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : BaseViewModel(application) {

    private val repository = DataRepository(application)

    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    val goals: StateFlow<List<Goal>> = _goals.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _journalEntries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val journalEntries: StateFlow<List<JournalEntry>> = _journalEntries.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _goals.value = repository.getGoals()
            _tasks.value = repository.getTasks()
            _notes.value = repository.getNotes()
            _habits.value = repository.getHabits()
            _reminders.value = repository.getReminders()
            _transactions.value = repository.getTransactions()
            _journalEntries.value = repository.getJournalEntries()
            _isLoading.value = false
        }
    }

    fun getPendingTasksCount(): Int = _tasks.value.count { !it.isCompleted }
    fun getCompletedTasksCount(): Int = _tasks.value.count { it.isCompleted }
    fun getActiveGoalsCount(): Int = _goals.value.size
    fun getOverallGoalProgress(): Float {
        val goals = _goals.value
        return if (goals.isEmpty()) 0f else goals.map { it.progress }.average().toFloat()
    }
}
