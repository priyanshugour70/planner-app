package com.lssgoo.planner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.lssgoo.planner.data.model.Goal
import com.lssgoo.planner.data.repository.DataRepository
import com.lssgoo.planner.features.goals.models.Milestone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GoalsViewModel(application: Application) : BaseViewModel(application) {

    private val repository = DataRepository(application)

    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    val goals: StateFlow<List<Goal>> = _goals.asStateFlow()

    init {
        loadGoals()
    }

    fun loadGoals() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _goals.value = repository.getGoals()
            _isLoading.value = false
        }
    }

    fun addGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createGoal(goal)
            loadGoals()
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateGoal(goal)
            loadGoals()
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGoal(goalId)
            loadGoals()
        }
    }

    fun toggleMilestone(goalId: String, milestoneId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val goal = getGoalById(goalId) ?: return@launch
            val updatedMilestones = goal.milestones.map {
                if (it.id == milestoneId) it.copy(isCompleted = !it.isCompleted, completedAt = if (!it.isCompleted) System.currentTimeMillis() else null) else it
            }
            val total = updatedMilestones.size
            val completed = updatedMilestones.count { it.isCompleted }
            val progress = if (total > 0) completed.toFloat() / total else 0f
            val updated = goal.copy(milestones = updatedMilestones, progress = progress, updatedAt = System.currentTimeMillis())
            repository.updateGoal(updated)
            loadGoals()
        }
    }

    fun updateMilestone(goalId: String, milestone: Milestone) {
        viewModelScope.launch(Dispatchers.IO) {
            val goal = getGoalById(goalId) ?: return@launch
            val updatedMilestones = goal.milestones.map {
                if (it.id == milestone.id) milestone else it
            }
            val total = updatedMilestones.size
            val completed = updatedMilestones.count { it.isCompleted }
            val progress = if (total > 0) completed.toFloat() / total else 0f
            val updated = goal.copy(milestones = updatedMilestones, progress = progress, updatedAt = System.currentTimeMillis())
            repository.updateGoal(updated)
            loadGoals()
        }
    }

    fun deleteMilestone(goalId: String, milestoneId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val goal = getGoalById(goalId) ?: return@launch
            val updatedMilestones = goal.milestones.filter { it.id != milestoneId }
            val total = updatedMilestones.size
            val completed = updatedMilestones.count { it.isCompleted }
            val progress = if (total > 0) completed.toFloat() / total else 0f
            val updated = goal.copy(milestones = updatedMilestones, progress = progress, updatedAt = System.currentTimeMillis())
            repository.updateGoal(updated)
            loadGoals()
        }
    }

    fun getGoalsSortedByDate(): List<Goal> {
        return _goals.value.sortedBy { it.targetDate ?: Long.MAX_VALUE }
    }

    fun getGoalById(goalId: String): Goal? {
        return _goals.value.find { it.id == goalId }
    }
}
