package com.lssgoo.planner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.lssgoo.planner.data.model.Goal
import com.lssgoo.planner.data.repository.DataRepository
import com.lssgoo.planner.features.goals.models.GoalStatus
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
            _goals.value = listOf(goal) + _goals.value
            repository.createGoal(goal)
            loadGoals()
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            _goals.value = _goals.value.map { if (it.id == goal.id) goal else it }
            repository.updateGoal(goal)
            loadGoals()
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _goals.value = _goals.value.filter { it.id != goalId }
            repository.deleteGoal(goalId)
            loadGoals()
        }
    }

    fun toggleFavorite(goalId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val goal = getGoalById(goalId) ?: return@launch
            val updated = goal.copy(isFavorite = !goal.isFavorite, updatedAt = System.currentTimeMillis())
            _goals.value = _goals.value.map { if (it.id == goalId) updated else it }
            repository.updateGoal(updated)
        }
    }

    fun togglePin(goalId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val goal = getGoalById(goalId) ?: return@launch
            val updated = goal.copy(isPinned = !goal.isPinned, updatedAt = System.currentTimeMillis())
            _goals.value = _goals.value.map { if (it.id == goalId) updated else it }
            repository.updateGoal(updated)
        }
    }

    fun toggleMilestone(goalId: String, milestoneId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val goal = getGoalById(goalId) ?: return@launch
            val updatedMilestones = goal.milestones.map {
                if (it.id == milestoneId) it.copy(
                    isCompleted = !it.isCompleted,
                    completedAt = if (!it.isCompleted) System.currentTimeMillis() else null
                ) else it
            }
            val total = updatedMilestones.size
            val completed = updatedMilestones.count { it.isCompleted }
            val progress = if (total > 0) completed.toFloat() / total else 0f

            val newStatus = when {
                progress >= 1.0f -> GoalStatus.COMPLETED
                progress > 0f && goal.status == GoalStatus.NOT_STARTED -> GoalStatus.IN_PROGRESS
                else -> goal.status
            }

            val updated = goal.copy(
                milestones = updatedMilestones,
                progress = progress,
                status = newStatus,
                completedDate = if (newStatus == GoalStatus.COMPLETED) System.currentTimeMillis() else goal.completedDate,
                startDate = if (newStatus == GoalStatus.IN_PROGRESS && goal.startDate == null) System.currentTimeMillis() else goal.startDate,
                updatedAt = System.currentTimeMillis()
            )
            _goals.value = _goals.value.map { if (it.id == goalId) updated else it }
            repository.updateGoal(updated)
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

            val newStatus = when {
                progress >= 1.0f -> GoalStatus.COMPLETED
                progress > 0f && goal.status == GoalStatus.NOT_STARTED -> GoalStatus.IN_PROGRESS
                else -> goal.status
            }

            val updated = goal.copy(
                milestones = updatedMilestones,
                progress = progress,
                status = newStatus,
                completedDate = if (newStatus == GoalStatus.COMPLETED) System.currentTimeMillis() else goal.completedDate,
                startDate = if (newStatus == GoalStatus.IN_PROGRESS && goal.startDate == null) System.currentTimeMillis() else goal.startDate,
                updatedAt = System.currentTimeMillis()
            )
            _goals.value = _goals.value.map { if (it.id == goalId) updated else it }
            repository.updateGoal(updated)
        }
    }

    fun deleteMilestone(goalId: String, milestoneId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val goal = getGoalById(goalId) ?: return@launch
            val updatedMilestones = goal.milestones.filter { it.id != milestoneId }
            val total = updatedMilestones.size
            val completed = updatedMilestones.count { it.isCompleted }
            val progress = if (total > 0) completed.toFloat() / total else 0f
            val updated = goal.copy(
                milestones = updatedMilestones,
                progress = progress,
                updatedAt = System.currentTimeMillis()
            )
            _goals.value = _goals.value.map { if (it.id == goalId) updated else it }
            repository.updateGoal(updated)
        }
    }

    fun getGoalsSortedByDate(): List<Goal> {
        return _goals.value.sortedBy { it.targetDate ?: Long.MAX_VALUE }
    }

    fun getGoalById(goalId: String): Goal? {
        return _goals.value.find { it.id == goalId }
    }
}
