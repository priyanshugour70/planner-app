package com.lssgoo.planner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.lssgoo.planner.data.repository.DataRepository
import com.lssgoo.planner.features.habits.models.Habit
import com.lssgoo.planner.features.habits.models.HabitEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HabitsViewModel(application: Application) : BaseViewModel(application) {

    private val repository = DataRepository(application)

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _habitEntries = MutableStateFlow<List<HabitEntry>>(emptyList())
    val habitEntries: StateFlow<List<HabitEntry>> = _habitEntries.asStateFlow()

    init {
        loadHabits()
    }

    fun loadHabits() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _habits.value = repository.getHabits()
            _habitEntries.value = repository.getHabitEntries()
            _isLoading.value = false
        }
    }

    fun addHabit(habit: Habit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createHabit(habit)
            loadHabits()
        }
    }

    fun logEntry(entry: HabitEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.logHabitEntry(entry)
            _habitEntries.value = repository.getHabitEntries()
        }
    }

    fun getActiveHabits(): List<Habit> = _habits.value.filter { it.isActive }

    fun getEntriesForHabit(habitId: String): List<HabitEntry> =
        _habitEntries.value.filter { it.habitId == habitId }

    fun getHabitById(habitId: String): Habit? = _habits.value.find { it.id == habitId }
}
