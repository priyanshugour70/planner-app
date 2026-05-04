package com.lssgoo.planner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.lssgoo.planner.data.model.Task
import com.lssgoo.planner.data.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class TasksViewModel(application: Application) : BaseViewModel(application) {

    private val repository = DataRepository(application)

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _tasks.value = repository.getTasks()
            _isLoading.value = false
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createTask(task)
            loadTasks()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTask(task)
            loadTasks()
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(taskId)
            loadTasks()
        }
    }

    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.completeTask(taskId)
            loadTasks()
        }
    }

    fun getTasksForDate(date: Long): List<Task> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = date
        val targetDay = cal.get(Calendar.DAY_OF_YEAR)
        val targetYear = cal.get(Calendar.YEAR)
        return _tasks.value.filter { task ->
            task.dueDate?.let {
                cal.timeInMillis = it
                cal.get(Calendar.DAY_OF_YEAR) == targetDay && cal.get(Calendar.YEAR) == targetYear
            } ?: false
        }
    }

    fun getPendingTasks(): List<Task> = _tasks.value.filter { !it.isCompleted }

    fun getCompletedTasks(): List<Task> = _tasks.value.filter { it.isCompleted }
}
