package com.lssgoo.planner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.lssgoo.planner.data.model.Reminder
import com.lssgoo.planner.data.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RemindersViewModel(application: Application) : BaseViewModel(application) {

    private val repository = DataRepository(application)

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    init {
        loadReminders()
    }

    fun loadReminders() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _reminders.value = repository.getReminders()
            _isLoading.value = false
        }
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createReminder(reminder)
            loadReminders()
        }
    }

    fun getActiveReminders(): List<Reminder> =
        _reminders.value.filter { it.isEnabled && !it.isCompleted }

    fun getUpcomingReminders(): List<Reminder> =
        _reminders.value
            .filter { it.isEnabled && !it.isCompleted && it.reminderTime > System.currentTimeMillis() }
            .sortedBy { it.reminderTime }
}
