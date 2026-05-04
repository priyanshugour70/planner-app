package com.lssgoo.planner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.lssgoo.planner.data.model.CalendarEvent
import com.lssgoo.planner.data.model.CalendarItem
import com.lssgoo.planner.data.model.CalendarItemType
import com.lssgoo.planner.data.model.Reminder
import com.lssgoo.planner.data.model.Task
import com.lssgoo.planner.data.repository.DataRepository
import com.lssgoo.planner.features.reminders.models.ItemPriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarViewModel(application: Application) : BaseViewModel(application) {

    private val repository = DataRepository(application)

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _events.value = repository.getEvents()
            _tasks.value = repository.getTasks()
            _reminders.value = repository.getReminders()
            _isLoading.value = false
        }
    }

    fun setSelectedDate(timestamp: Long) {
        _selectedDate.value = timestamp
    }

    fun addEvent(event: CalendarEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addEvent(event)
            _events.value = repository.getEvents()
        }
    }

    fun getAllItemsForDate(timestamp: Long): List<CalendarItem> {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val targetDay = cal.get(Calendar.DAY_OF_YEAR)
        val targetYear = cal.get(Calendar.YEAR)

        val items = mutableListOf<CalendarItem>()

        _tasks.value.forEach { task ->
            if (task.dueDate != null) {
                cal.timeInMillis = task.dueDate
                if (cal.get(Calendar.DAY_OF_YEAR) == targetDay && cal.get(Calendar.YEAR) == targetYear) {
                    items.add(
                        CalendarItem(
                            id = task.id,
                            title = task.title,
                            description = task.description.ifBlank { "" },
                            date = task.dueDate!!,
                            type = CalendarItemType.TASK,
                            priority = ItemPriority.P5,
                            color = task.priority.color,
                            isCompleted = task.isCompleted
                        )
                    )
                }
            }
        }

        _reminders.value.forEach { reminder ->
            cal.timeInMillis = reminder.reminderTime
            if (cal.get(Calendar.DAY_OF_YEAR) == targetDay && cal.get(Calendar.YEAR) == targetYear) {
                items.add(CalendarItem(
                    id = reminder.id,
                    title = reminder.title,
                    description = reminder.description,
                    date = reminder.reminderTime,
                    type = CalendarItemType.REMINDER,
                    priority = reminder.priority,
                    color = reminder.color,
                    isCompleted = reminder.isCompleted
                ))
            }
        }

        _events.value.forEach { event ->
            cal.timeInMillis = event.date
            if (cal.get(Calendar.DAY_OF_YEAR) == targetDay && cal.get(Calendar.YEAR) == targetYear) {
                items.add(CalendarItem(
                    id = event.id,
                    title = event.title,
                    description = event.description,
                    date = event.date,
                    type = CalendarItemType.EVENT,
                    priority = event.priority,
                    color = event.color,
                    isCompleted = false
                ))
            }
        }

        return items.sortedBy { it.date }
    }
}
