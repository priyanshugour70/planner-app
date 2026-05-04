package com.lssgoo.planner.data.repository

import android.content.Context
import com.lssgoo.planner.data.local.LocalStorageManager
import com.lssgoo.planner.data.model.*
import com.lssgoo.planner.data.remote.ApiResponse
import com.lssgoo.planner.data.remote.PlannerApiService
import com.lssgoo.planner.features.goals.models.GoalCategory as FeatureGoalCategory
import com.lssgoo.planner.features.goals.models.Milestone
import com.lssgoo.planner.features.habits.models.Habit
import com.lssgoo.planner.features.habits.models.HabitEntry
import com.lssgoo.planner.features.habits.models.HabitTimeOfDay
import com.lssgoo.planner.features.habits.models.HabitType as FeatureHabitType
import com.lssgoo.planner.features.tasks.models.RepeatType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Unified data repository that routes to backend API when logged in,
 * and falls back to local storage when offline or unauthenticated.
 * Also caches API responses locally for offline support.
 */
class DataRepository(context: Context) {

    private val api = PlannerApiService(context)
    private val storage = LocalStorageManager(context)

    val isLoggedIn: Boolean get() = api.isLoggedIn()

    // =================== GOALS ===================

    suspend fun getGoals(page: Int = 0, size: Int = 50): List<Goal> = withContext(Dispatchers.IO) {
        if (!isLoggedIn) return@withContext storage.getGoals()
        try {
            val response = api.getGoals(page, size)
            val goals = parseGoalList(response)
            if (goals.isNotEmpty()) {
                storage.saveGoals(goals)
            }
            goals
        } catch (e: Exception) {
            storage.getGoals()
        }
    }

    suspend fun createGoal(goal: Goal): Goal? = withContext(Dispatchers.IO) {
        if (!isLoggedIn) {
            val goals = storage.getGoals().toMutableList()
            goals.add(0, goal)
            storage.saveGoals(goals)
            return@withContext goal
        }
        try {
            val body = goalToMap(goal)
            val response = api.createGoal(body)
            val created = parseGoal(response)
            if (created != null) {
                val goals = storage.getGoals().toMutableList()
                goals.add(0, created)
                storage.saveGoals(goals)
            }
            created ?: goal
        } catch (e: Exception) {
            val goals = storage.getGoals().toMutableList()
            goals.add(0, goal)
            storage.saveGoals(goals)
            goal
        }
    }

    suspend fun updateGoal(goal: Goal): Goal? = withContext(Dispatchers.IO) {
        if (!isLoggedIn) {
            val goals = storage.getGoals().toMutableList()
            val idx = goals.indexOfFirst { it.id == goal.id }
            if (idx >= 0) goals[idx] = goal
            storage.saveGoals(goals)
            return@withContext goal
        }
        try {
            val uuid = goal.id
            val body = goalToMap(goal)
            api.updateGoal(uuid, body)
            val goals = storage.getGoals().toMutableList()
            val idx = goals.indexOfFirst { it.id == goal.id }
            if (idx >= 0) goals[idx] = goal
            storage.saveGoals(goals)
            goal
        } catch (e: Exception) {
            val goals = storage.getGoals().toMutableList()
            val idx = goals.indexOfFirst { it.id == goal.id }
            if (idx >= 0) goals[idx] = goal
            storage.saveGoals(goals)
            goal
        }
    }

    suspend fun deleteGoal(goalId: String) = withContext(Dispatchers.IO) {
        if (!isLoggedIn) {
            val goals = storage.getGoals().filter { it.id != goalId }
            storage.saveGoals(goals)
            return@withContext
        }
        try {
            api.deleteGoal(goalId)
        } catch (_: Exception) {}
        val goals = storage.getGoals().filter { it.id != goalId }
        storage.saveGoals(goals)
    }

    // =================== TASKS ===================

    suspend fun getTasks(page: Int = 0, size: Int = 50): List<Task> = withContext(Dispatchers.IO) {
        if (!isLoggedIn) return@withContext storage.getTasks()
        try {
            val response = api.getTasks(page, size)
            val tasks = parseTaskList(response)
            if (tasks.isNotEmpty()) {
                storage.saveTasks(tasks)
            }
            tasks
        } catch (e: Exception) {
            storage.getTasks()
        }
    }

    suspend fun createTask(task: Task): Task? = withContext(Dispatchers.IO) {
        if (!isLoggedIn) {
            val tasks = storage.getTasks().toMutableList()
            tasks.add(0, task)
            storage.saveTasks(tasks)
            return@withContext task
        }
        try {
            val body = taskToMap(task)
            val response = api.createTask(body)
            val created = parseTask(response)
            if (created != null) {
                val tasks = storage.getTasks().toMutableList()
                tasks.add(0, created)
                storage.saveTasks(tasks)
            }
            created ?: task
        } catch (e: Exception) {
            val tasks = storage.getTasks().toMutableList()
            tasks.add(0, task)
            storage.saveTasks(tasks)
            task
        }
    }

    suspend fun updateTask(task: Task): Task? = withContext(Dispatchers.IO) {
        if (!isLoggedIn) {
            val tasks = storage.getTasks().toMutableList()
            val idx = tasks.indexOfFirst { it.id == task.id }
            if (idx >= 0) tasks[idx] = task
            storage.saveTasks(tasks)
            return@withContext task
        }
        try {
            val body = taskToMap(task)
            api.updateTask(task.id, body)
            val tasks = storage.getTasks().toMutableList()
            val idx = tasks.indexOfFirst { it.id == task.id }
            if (idx >= 0) tasks[idx] = task
            storage.saveTasks(tasks)
            task
        } catch (e: Exception) {
            val tasks = storage.getTasks().toMutableList()
            val idx = tasks.indexOfFirst { it.id == task.id }
            if (idx >= 0) tasks[idx] = task
            storage.saveTasks(tasks)
            task
        }
    }

    suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        if (!isLoggedIn) {
            val tasks = storage.getTasks().filter { it.id != taskId }
            storage.saveTasks(tasks)
            return@withContext
        }
        try {
            api.deleteTask(taskId)
        } catch (_: Exception) {}
        val tasks = storage.getTasks().filter { it.id != taskId }
        storage.saveTasks(tasks)
    }

    suspend fun completeTask(taskId: String) = withContext(Dispatchers.IO) {
        if (isLoggedIn) {
            try { api.completeTask(taskId) } catch (_: Exception) {}
        }
        val tasks = storage.getTasks().toMutableList()
        val idx = tasks.indexOfFirst { it.id == taskId }
        if (idx >= 0) {
            val t = tasks[idx]
            tasks[idx] = t.copy(
                isCompleted = !t.isCompleted,
                completedAt = if (!t.isCompleted) System.currentTimeMillis() else null,
                updatedAt = System.currentTimeMillis()
            )
            storage.saveTasks(tasks)
        }
    }

    // =================== NOTES ===================

    suspend fun getNotes(page: Int = 0, size: Int = 50): List<Note> = withContext(Dispatchers.IO) {
        if (!isLoggedIn) return@withContext storage.getNotes()
        try {
            val response = api.getNotes(page, size)
            val notes = parseNoteList(response)
            if (notes.isNotEmpty()) {
                storage.saveNotes(notes)
            }
            notes
        } catch (e: Exception) {
            storage.getNotes()
        }
    }

    suspend fun createNote(note: Note): Note? = withContext(Dispatchers.IO) {
        if (!isLoggedIn) {
            val notes = storage.getNotes().toMutableList()
            notes.add(0, note)
            storage.saveNotes(notes)
            return@withContext note
        }
        try {
            val body = noteToMap(note)
            val response = api.createNote(body)
            val created = parseNote(response)
            if (created != null) {
                val notes = storage.getNotes().toMutableList()
                notes.add(0, created)
                storage.saveNotes(notes)
            }
            created ?: note
        } catch (e: Exception) {
            val notes = storage.getNotes().toMutableList()
            notes.add(0, note)
            storage.saveNotes(notes)
            note
        }
    }

    suspend fun updateNote(note: Note): Note? = withContext(Dispatchers.IO) {
        if (!isLoggedIn) {
            val notes = storage.getNotes().toMutableList()
            val idx = notes.indexOfFirst { it.id == note.id }
            if (idx >= 0) notes[idx] = note
            storage.saveNotes(notes)
            return@withContext note
        }
        try {
            val body = noteToMap(note)
            api.updateNote(note.id, body)
            val notes = storage.getNotes().toMutableList()
            val idx = notes.indexOfFirst { it.id == note.id }
            if (idx >= 0) notes[idx] = note
            storage.saveNotes(notes)
            note
        } catch (e: Exception) {
            val notes = storage.getNotes().toMutableList()
            val idx = notes.indexOfFirst { it.id == note.id }
            if (idx >= 0) notes[idx] = note
            storage.saveNotes(notes)
            note
        }
    }

    suspend fun deleteNote(noteId: String) = withContext(Dispatchers.IO) {
        if (isLoggedIn) {
            try { api.deleteNote(noteId) } catch (_: Exception) {}
        }
        val notes = storage.getNotes().filter { it.id != noteId }
        storage.saveNotes(notes)
    }

    // =================== REMINDERS ===================

    suspend fun getReminders(page: Int = 0, size: Int = 50): List<Reminder> = withContext(Dispatchers.IO) {
        if (!isLoggedIn) return@withContext storage.getReminders()
        try {
            val response = api.getReminders(page, size)
            val reminders = parseReminderList(response)
            if (reminders.isNotEmpty()) {
                storage.saveReminders(reminders)
            }
            reminders
        } catch (e: Exception) {
            storage.getReminders()
        }
    }

    suspend fun createReminder(reminder: Reminder): Reminder? = withContext(Dispatchers.IO) {
        if (!isLoggedIn) {
            val list = storage.getReminders().toMutableList()
            list.add(0, reminder)
            storage.saveReminders(list)
            return@withContext reminder
        }
        try {
            val body = reminderToMap(reminder)
            api.createReminder(body)
            val list = storage.getReminders().toMutableList()
            list.add(0, reminder)
            storage.saveReminders(list)
            reminder
        } catch (e: Exception) {
            val list = storage.getReminders().toMutableList()
            list.add(0, reminder)
            storage.saveReminders(list)
            reminder
        }
    }

    // =================== CALENDAR EVENTS ===================

    suspend fun getEvents(): List<CalendarEvent> = withContext(Dispatchers.IO) {
        storage.getEvents()
    }

    suspend fun addEvent(event: CalendarEvent) = withContext(Dispatchers.IO) {
        val list = storage.getEvents().toMutableList()
        list.add(0, event)
        storage.saveEvents(list)
    }

    // =================== FINANCE ===================

    suspend fun getTransactions(page: Int = 0, size: Int = 50): List<Transaction> = withContext(Dispatchers.IO) {
        if (!isLoggedIn) return@withContext storage.getTransactions()
        try {
            val response = api.getTransactions(page, size)
            val txns = parseTransactionList(response)
            if (txns.isNotEmpty()) {
                storage.saveTransactions(txns)
            }
            txns
        } catch (e: Exception) {
            storage.getTransactions()
        }
    }

    suspend fun createTransaction(transaction: Transaction): Transaction? = withContext(Dispatchers.IO) {
        if (!isLoggedIn) {
            val list = storage.getTransactions().toMutableList()
            list.add(0, transaction)
            storage.saveTransactions(list)
            return@withContext transaction
        }
        try {
            val body = transactionToMap(transaction)
            api.createTransaction(body)
            val list = storage.getTransactions().toMutableList()
            list.add(0, transaction)
            storage.saveTransactions(list)
            transaction
        } catch (e: Exception) {
            val list = storage.getTransactions().toMutableList()
            list.add(0, transaction)
            storage.saveTransactions(list)
            transaction
        }
    }

    suspend fun getBudgets(): List<Budget> = withContext(Dispatchers.IO) {
        storage.getBudgets()
    }

    suspend fun getFinanceLogs(): List<FinanceLog> = withContext(Dispatchers.IO) {
        storage.getFinanceLogs()
    }

    // =================== HABITS ===================

    suspend fun getHabits(): List<Habit> = withContext(Dispatchers.IO) {
        if (!isLoggedIn) return@withContext storage.getHabits()
        try {
            val response = api.getHabits(0, 50)
            val habits = parseHabitList(response)
            if (habits.isNotEmpty()) storage.saveHabits(habits)
            habits
        } catch (e: Exception) {
            storage.getHabits()
        }
    }

    suspend fun createHabit(habit: Habit): Habit? = withContext(Dispatchers.IO) {
        if (!isLoggedIn) {
            val list = storage.getHabits().toMutableList()
            list.add(0, habit)
            storage.saveHabits(list)
            return@withContext habit
        }
        try {
            api.createHabit(habitToMap(habit))
            val list = storage.getHabits().toMutableList()
            list.add(0, habit)
            storage.saveHabits(list)
            habit
        } catch (e: Exception) {
            val list = storage.getHabits().toMutableList()
            list.add(0, habit)
            storage.saveHabits(list)
            habit
        }
    }

    suspend fun getHabitEntries(): List<HabitEntry> = withContext(Dispatchers.IO) {
        storage.getHabitEntries()
    }

    suspend fun logHabitEntry(entry: HabitEntry) = withContext(Dispatchers.IO) {
        if (isLoggedIn) {
            try { api.logHabitEntry(habitEntryToMap(entry)) } catch (_: Exception) {}
        }
        val list = storage.getHabitEntries().toMutableList()
        val existing = list.indexOfFirst { it.habitId == entry.habitId && it.date == entry.date }
        if (existing >= 0) list[existing] = entry
        else list.add(0, entry)
        storage.saveHabitEntries(list)
    }

    // =================== JOURNAL ===================

    suspend fun getJournalEntries(): List<JournalEntry> = withContext(Dispatchers.IO) {
        if (!isLoggedIn) return@withContext storage.getJournalEntries()
        try {
            val response = api.getJournalEntries(0, 50)
            val entries = parseJournalList(response)
            if (entries.isNotEmpty()) storage.saveJournalEntries(entries)
            entries
        } catch (e: Exception) {
            storage.getJournalEntries()
        }
    }

    suspend fun createJournalEntry(entry: JournalEntry): JournalEntry? = withContext(Dispatchers.IO) {
        if (!isLoggedIn) {
            val list = storage.getJournalEntries().toMutableList()
            list.add(0, entry)
            storage.saveJournalEntries(list)
            return@withContext entry
        }
        try {
            api.createJournalEntry(journalToMap(entry))
            val list = storage.getJournalEntries().toMutableList()
            list.add(0, entry)
            storage.saveJournalEntries(list)
            entry
        } catch (e: Exception) {
            val list = storage.getJournalEntries().toMutableList()
            list.add(0, entry)
            storage.saveJournalEntries(list)
            entry
        }
    }

    // =================== SEARCH ===================

    suspend fun search(query: String, types: List<String>? = null): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        if (!isLoggedIn) return@withContext emptyList()
        try {
            val response = api.search(query, types)
            response?.data ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // =================== ANALYTICS ===================

    suspend fun getDashboardStats(): Map<String, Any>? = withContext(Dispatchers.IO) {
        if (!isLoggedIn) return@withContext null
        try {
            val response = api.getDashboardStats()
            response?.data
        } catch (e: Exception) {
            null
        }
    }

    // =================== ADDITIONAL CRUD ===================

    suspend fun updateReminder(reminder: Reminder) = withContext(Dispatchers.IO) {
        if (isLoggedIn) {
            try { api.updateReminder(reminder.id, reminderToMap(reminder)) } catch (_: Exception) {}
        }
        val list = storage.getReminders().toMutableList()
        val idx = list.indexOfFirst { it.id == reminder.id }
        if (idx >= 0) list[idx] = reminder
        storage.saveReminders(list)
    }

    suspend fun deleteReminder(reminderId: String) = withContext(Dispatchers.IO) {
        if (isLoggedIn) {
            try { api.deleteReminder(reminderId) } catch (_: Exception) {}
        }
        val list = storage.getReminders().filter { it.id != reminderId }
        storage.saveReminders(list)
    }

    suspend fun deleteEvent(eventId: String) = withContext(Dispatchers.IO) {
        val list = storage.getEvents().filter { it.id != eventId }
        storage.saveEvents(list)
    }

    suspend fun updateTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        if (isLoggedIn) {
            try { api.updateTransaction(transaction.id, transactionToMap(transaction)) } catch (_: Exception) {}
        }
        val list = storage.getTransactions().toMutableList()
        val idx = list.indexOfFirst { it.id == transaction.id }
        if (idx >= 0) list[idx] = transaction
        storage.saveTransactions(list)
    }

    suspend fun deleteTransaction(transactionId: String) = withContext(Dispatchers.IO) {
        if (isLoggedIn) {
            try { api.deleteTransaction(transactionId) } catch (_: Exception) {}
        }
        val list = storage.getTransactions().filter { it.id != transactionId }
        storage.saveTransactions(list)
    }

    suspend fun updateHabit(habit: Habit) = withContext(Dispatchers.IO) {
        if (isLoggedIn) {
            try { api.updateHabit(habit.id, habitToMap(habit)) } catch (_: Exception) {}
        }
        storage.updateHabit(habit)
    }

    suspend fun deleteHabit(habitId: String) = withContext(Dispatchers.IO) {
        if (isLoggedIn) {
            try { api.deleteHabit(habitId) } catch (_: Exception) {}
        }
        storage.deleteHabit(habitId)
    }

    suspend fun deleteJournalEntry(entryId: String) = withContext(Dispatchers.IO) {
        if (isLoggedIn) {
            try { api.deleteJournalEntry(entryId) } catch (_: Exception) {}
        }
        storage.deleteJournalEntry(entryId)
    }

    suspend fun addBudget(budget: Budget) = withContext(Dispatchers.IO) {
        storage.addBudget(budget)
    }

    suspend fun removeBudget(budgetId: String) = withContext(Dispatchers.IO) {
        val list = storage.getBudgets().filter { it.id != budgetId }
        storage.saveBudgets(list)
    }

    suspend fun toggleTaskCompletion(taskId: String) = withContext(Dispatchers.IO) {
        val taskList = storage.getTasks().toMutableList()
        val idx = taskList.indexOfFirst { it.id == taskId }
        if (idx >= 0) {
            val task = taskList[idx]
            val updated = task.copy(isCompleted = !task.isCompleted, updatedAt = System.currentTimeMillis())
            taskList[idx] = updated
            storage.saveTasks(taskList)
            if (isLoggedIn) {
                try { api.updateTask(taskId, taskToMap(updated)) } catch (_: Exception) {}
            }
        }
    }

    suspend fun addHabitEntry(entry: HabitEntry) = withContext(Dispatchers.IO) {
        storage.addHabitEntry(entry)
        if (isLoggedIn) {
            try {
                val body = mutableMapOf<String, Any?>(
                    "habitId" to entry.habitId,
                    "date" to entry.date,
                    "isCompleted" to entry.isCompleted,
                    "value" to entry.value,
                    "notes" to entry.notes
                )
                entry.mood?.name?.takeIf { it.isNotBlank() }?.let { body["mood"] = it }
                api.createHabitEntry(body)
            } catch (_: Exception) {}
        }
    }

    suspend fun deleteHabitEntry(entryId: String) = withContext(Dispatchers.IO) {
        storage.deleteHabitEntry(entryId)
    }

    fun getHabitEntries(habitId: String): List<HabitEntry> = storage.getHabitEntries(habitId)
    fun getHabitEntriesForDate(date: Long): List<HabitEntry> = storage.getHabitEntriesForDate(date)

    // =================== LOCAL ACCESS (always available) ===================

    fun getLocalStorage(): LocalStorageManager = storage

    // =================== MAPPING HELPERS ===================

    private fun goalToMap(goal: Goal): Map<String, Any?> = mapOf(
        "title" to goal.title,
        "description" to goal.description,
        "category" to goal.category.name,
        "icon" to goal.icon,
        "color" to goal.color,
        "progress" to goal.progress,
        "targetDate" to goal.targetDate,
        "number" to goal.number
    )

    private fun taskToMap(task: Task): Map<String, Any?> = mapOf(
        "title" to task.title,
        "description" to task.description,
        "isCompleted" to task.isCompleted,
        "priority" to task.priority.name,
        "dueDate" to task.dueDate,
        "tags" to task.tags,
        "reminderEnabled" to task.reminderEnabled
    )

    private fun noteToMap(note: Note): Map<String, Any?> = mapOf(
        "title" to note.title,
        "content" to note.content,
        "color" to note.color,
        "isPinned" to note.isPinned,
        "category" to note.category,
        "tags" to note.tags
    )

    private fun reminderToMap(reminder: Reminder): Map<String, Any?> = mapOf(
        "title" to reminder.title,
        "description" to reminder.description,
        "reminderTime" to reminder.reminderTime,
        "repeatType" to reminder.repeatType.name,
        "priority" to reminder.priority.name,
        "isEnabled" to reminder.isEnabled,
        "color" to reminder.color
    )

    private fun transactionToMap(tx: Transaction): Map<String, Any?> = mapOf(
        "amount" to tx.amount,
        "type" to tx.type.name,
        "category" to tx.category.name,
        "note" to tx.note,
        "personName" to tx.personName,
        "date" to tx.date,
        "isSettled" to tx.isSettled,
        "isRecurring" to tx.isRecurring
    )

    private fun habitToMap(habit: Habit): Map<String, Any?> = mapOf(
        "title" to habit.title,
        "description" to habit.description,
        "icon" to habit.icon,
        "iconColor" to habit.iconColor,
        "type" to habit.type.name,
        "targetValue" to habit.targetValue,
        "unit" to habit.unit,
        "frequency" to habit.frequency,
        "timeOfDay" to habit.timeOfDay.name,
        "isActive" to habit.isActive,
        "goalId" to habit.goalId
    )

    private fun habitEntryToMap(entry: HabitEntry): Map<String, Any?> = mapOf(
        "habitId" to entry.habitId,
        "date" to entry.date,
        "value" to entry.value,
        "isCompleted" to entry.isCompleted,
        "notes" to entry.notes
    )

    private fun journalToMap(entry: JournalEntry): Map<String, Any?> = mapOf(
        "title" to entry.title,
        "content" to entry.content,
        "mood" to entry.mood.name,
        "date" to entry.date,
        "tags" to entry.tags,
        "gratitude" to entry.gratitude,
        "achievements" to entry.achievements,
        "challenges" to entry.challenges,
        "reflection" to entry.reflection
    )

    @Suppress("UNCHECKED_CAST")
    private fun parseGoalList(response: ApiResponse<*>?): List<Goal> {
        if (response?.data == null) return emptyList()
        val data = response.data
        val content = when (data) {
            is Map<*, *> -> (data["content"] as? List<*>) ?: emptyList<Any>()
            is List<*> -> data
            else -> emptyList<Any>()
        }
        return content.mapNotNull { mapToGoal(it as? Map<String, Any?>) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseGoal(response: ApiResponse<*>?): Goal? {
        val data = response?.data as? Map<String, Any?> ?: return null
        return mapToGoal(data)
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseTaskList(response: ApiResponse<*>?): List<Task> {
        if (response?.data == null) return emptyList()
        val data = response.data
        val content = when (data) {
            is Map<*, *> -> (data["content"] as? List<*>) ?: emptyList<Any>()
            is List<*> -> data
            else -> emptyList<Any>()
        }
        return content.mapNotNull { mapToTask(it as? Map<String, Any?>) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseTask(response: ApiResponse<*>?): Task? {
        val data = response?.data as? Map<String, Any?> ?: return null
        return mapToTask(data)
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseNoteList(response: ApiResponse<*>?): List<Note> {
        if (response?.data == null) return emptyList()
        val data = response.data
        val content = when (data) {
            is Map<*, *> -> (data["content"] as? List<*>) ?: emptyList<Any>()
            is List<*> -> data
            else -> emptyList<Any>()
        }
        return content.mapNotNull { mapToNote(it as? Map<String, Any?>) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseNote(response: ApiResponse<*>?): Note? {
        val data = response?.data as? Map<String, Any?> ?: return null
        return mapToNote(data)
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseReminderList(response: ApiResponse<*>?): List<Reminder> {
        if (response?.data == null) return emptyList()
        val data = response.data
        val content = when (data) {
            is Map<*, *> -> (data["content"] as? List<*>) ?: emptyList<Any>()
            is List<*> -> data
            else -> emptyList<Any>()
        }
        return content.mapNotNull { mapToReminder(it as? Map<String, Any?>) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseTransactionList(response: ApiResponse<*>?): List<Transaction> {
        if (response?.data == null) return emptyList()
        val data = response.data
        val content = when (data) {
            is Map<*, *> -> (data["content"] as? List<*>) ?: emptyList<Any>()
            is List<*> -> data
            else -> emptyList<Any>()
        }
        return content.mapNotNull { mapToTransaction(it as? Map<String, Any?>) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseHabitList(response: ApiResponse<*>?): List<Habit> {
        if (response?.data == null) return emptyList()
        val data = response.data
        val content = when (data) {
            is Map<*, *> -> (data["content"] as? List<*>) ?: emptyList<Any>()
            is List<*> -> data
            else -> emptyList<Any>()
        }
        return content.mapNotNull { mapToHabit(it as? Map<String, Any?>) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseJournalList(response: ApiResponse<*>?): List<JournalEntry> {
        if (response?.data == null) return emptyList()
        val data = response.data
        val content = when (data) {
            is Map<*, *> -> (data["content"] as? List<*>) ?: emptyList<Any>()
            is List<*> -> data
            else -> emptyList<Any>()
        }
        return content.mapNotNull { mapToJournal(it as? Map<String, Any?>) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapToGoal(map: Map<String, Any?>?): Goal? {
        if (map == null) return null
        return try {
            val categoryStr = map["category"] as? String ?: "PERSONAL"
            val category = try {
                FeatureGoalCategory.valueOf(categoryStr)
            } catch (_: Exception) {
                FeatureGoalCategory.PERSONAL
            }
            Goal(
                id = (map["uuid"] as? String) ?: (map["id"] as? String) ?: return null,
                number = (map["number"] as? Number)?.toInt() ?: 0,
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                category = category,
                icon = map["icon"] as? String ?: "",
                color = parseColorLong(map["color"]),
                progress = (map["progress"] as? Number)?.toFloat() ?: 0f,
                milestones = emptyList(),
                targetDate = (map["targetDate"] as? Number)?.toLong(),
                createdAt = parseTimestamp(map["createdAt"]),
                updatedAt = parseTimestamp(map["updatedAt"])
            )
        } catch (_: Exception) { null }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapToTask(map: Map<String, Any?>?): Task? {
        if (map == null) return null
        return try {
            Task(
                id = (map["uuid"] as? String) ?: (map["id"] as? String) ?: return null,
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                isCompleted = map["isCompleted"] as? Boolean ?: false,
                priority = try { TaskPriority.valueOf(map["priority"] as? String ?: "MEDIUM") } catch (_: Exception) { TaskPriority.MEDIUM },
                dueDate = (map["dueDate"] as? Number)?.toLong(),
                completedAt = (map["completedAt"] as? Number)?.toLong(),
                tags = (map["tags"] as? List<String>) ?: emptyList(),
                createdAt = parseTimestamp(map["createdAt"]),
                updatedAt = parseTimestamp(map["updatedAt"])
            )
        } catch (_: Exception) { null }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapToNote(map: Map<String, Any?>?): Note? {
        if (map == null) return null
        return try {
            Note(
                id = (map["uuid"] as? String) ?: (map["id"] as? String) ?: return null,
                title = map["title"] as? String ?: "",
                content = map["content"] as? String ?: "",
                color = parseColorLong(map["color"]),
                isPinned = map["isPinned"] as? Boolean ?: false,
                category = map["category"] as? String ?: "General",
                tags = (map["tags"] as? List<String>) ?: emptyList(),
                createdAt = parseTimestamp(map["createdAt"]),
                updatedAt = parseTimestamp(map["updatedAt"])
            )
        } catch (_: Exception) { null }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapToReminder(map: Map<String, Any?>?): Reminder? {
        if (map == null) return null
        return try {
            val repeatStr = map["repeatType"] as? String ?: "NONE"
            val repeatType = try { RepeatType.valueOf(repeatStr) } catch (_: Exception) { RepeatType.NONE }
            Reminder(
                id = (map["uuid"] as? String) ?: (map["id"] as? String) ?: return null,
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                reminderTime = (map["reminderTime"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                repeatType = repeatType,
                priority = try { com.lssgoo.planner.features.reminders.models.ItemPriority.valueOf(map["priority"] as? String ?: "P5") } catch (_: Exception) { com.lssgoo.planner.features.reminders.models.ItemPriority.P5 },
                isEnabled = map["isEnabled"] as? Boolean ?: true,
                isCompleted = map["isCompleted"] as? Boolean ?: false,
                color = parseColorLong(map["color"]),
                createdAt = parseTimestamp(map["createdAt"]),
                updatedAt = parseTimestamp(map["updatedAt"])
            )
        } catch (_: Exception) { null }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapToTransaction(map: Map<String, Any?>?): Transaction? {
        if (map == null) return null
        return try {
            Transaction(
                id = (map["uuid"] as? String) ?: (map["id"] as? String) ?: return null,
                amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
                type = try { com.lssgoo.planner.features.finance.models.TransactionType.valueOf(map["type"] as? String ?: "EXPENSE") } catch (_: Exception) { com.lssgoo.planner.features.finance.models.TransactionType.EXPENSE },
                category = try { com.lssgoo.planner.features.finance.models.TransactionCategory.valueOf(map["category"] as? String ?: "OTHER") } catch (_: Exception) { com.lssgoo.planner.features.finance.models.TransactionCategory.OTHER },
                note = map["note"] as? String ?: "",
                date = (map["date"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                personName = map["personName"] as? String,
                isSettled = map["isSettled"] as? Boolean ?: false,
                isRecurring = map["isRecurring"] as? Boolean ?: false,
                createdAt = parseTimestamp(map["createdAt"])
            )
        } catch (_: Exception) { null }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapToHabit(map: Map<String, Any?>?): Habit? {
        if (map == null) return null
        return try {
            val typeStr = (map["type"] as? String)?.uppercase() ?: "YES_NO"
            val habitType = when (typeStr) {
                "BOOLEAN", "YES_NO" -> FeatureHabitType.YES_NO
                "QUANTITATIVE" -> FeatureHabitType.QUANTITATIVE
                "TIMER" -> FeatureHabitType.TIMER
                else -> try { FeatureHabitType.valueOf(typeStr) } catch (_: Exception) { FeatureHabitType.YES_NO }
            }
            val timeStr = map["timeOfDay"] as? String ?: "ANY_TIME"
            val timeOfDay = try { HabitTimeOfDay.valueOf(timeStr) } catch (_: Exception) { HabitTimeOfDay.ANY_TIME }
            val freqRaw = map["frequency"]
            val frequency: List<Int> = when (freqRaw) {
                is List<*> -> freqRaw.mapNotNull { (it as? Number)?.toInt() }
                else -> listOf(1, 2, 3, 4, 5, 6, 7)
            }
            Habit(
                id = (map["uuid"] as? String) ?: (map["id"] as? String) ?: return null,
                goalId = map["goalId"] as? String,
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                icon = map["icon"] as? String ?: "✨",
                iconColor = parseColorLong(map["iconColor"]),
                type = habitType,
                targetValue = (map["targetValue"] as? Number)?.toFloat() ?: 1f,
                unit = map["unit"] as? String,
                frequency = frequency,
                timeOfDay = timeOfDay,
                reminderTime = map["reminderTime"] as? String,
                isActive = map["isActive"] as? Boolean ?: true,
                createdAt = parseTimestamp(map["createdAt"])
            )
        } catch (_: Exception) { null }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapToJournal(map: Map<String, Any?>?): JournalEntry? {
        if (map == null) return null
        return try {
            val moodStr = map["mood"] as? String ?: "NEUTRAL"
            val mood = try {
                com.lssgoo.planner.features.journal.models.JournalMood.valueOf(moodStr)
            } catch (_: Exception) {
                com.lssgoo.planner.features.journal.models.JournalMood.NEUTRAL
            }
            val now = System.currentTimeMillis()
            JournalEntry(
                id = (map["uuid"] as? String) ?: (map["id"] as? String) ?: return null,
                date = (map["date"] as? Number)?.toLong() ?: now,
                title = map["title"] as? String ?: "",
                content = map["content"] as? String ?: "",
                mood = mood,
                tags = (map["tags"] as? List<String>) ?: emptyList(),
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: now,
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: now
            )
        } catch (_: Exception) { null }
    }

    private fun parseTimestamp(value: Any?): Long {
        return when (value) {
            is Number -> value.toLong()
            is String -> {
                try {
                    java.time.LocalDateTime.parse(value.replace(" ", "T"))
                        .atZone(java.time.ZoneId.systemDefault())
                        .toInstant().toEpochMilli()
                } catch (_: Exception) { System.currentTimeMillis() }
            }
            else -> System.currentTimeMillis()
        }
    }

    private fun parseColorLong(value: Any?): Long {
        when (value) {
            null -> return 0xFFE0E0E0L
            is Number -> return value.toLong()
            is String -> {
                var s = value.trim()
                if (s.startsWith("#")) s = s.substring(1)
                return try {
                    val hex = s.toLong(16)
                    if (hex <= 0xFFFFFFL) hex or 0xFF000000L else hex
                } catch (_: Exception) {
                    0xFFE0E0E0L
                }
            }
            else -> return 0xFFE0E0E0L
        }
    }
}
