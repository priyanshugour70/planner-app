package com.lssgoo.planner.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.lssgoo.planner.data.local.LocalStorageManager
import com.lssgoo.planner.data.model.*
import com.lssgoo.planner.data.repository.DataRepository
import com.lssgoo.planner.features.habits.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Root ViewModel for global app state - delegates to DataRepository for backend integration
 */
class PlannerViewModel(application: Application) : BaseViewModel(application) {
    
    private val storageManager = LocalStorageManager(application)
    private val dataRepository = DataRepository(application)
    
    private val apiService = com.lssgoo.planner.data.remote.PlannerApiService(application)
    
    private val _settings = MutableStateFlow(storageManager.getSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    private val _userProfile = MutableStateFlow(storageManager.getUserProfile() ?: UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()
    
    private val _isOnboardingComplete = MutableStateFlow(storageManager.isOnboardingComplete())
    val isOnboardingComplete: StateFlow<Boolean> = _isOnboardingComplete.asStateFlow()

    private val _isCheckingSync = MutableStateFlow(false)
    val isCheckingSync: StateFlow<Boolean> = _isCheckingSync.asStateFlow()
    
    // Feature data states
    val goals = MutableStateFlow<List<Goal>>(emptyList())
    val tasks = MutableStateFlow<List<Task>>(emptyList())
    val notes = MutableStateFlow<List<Note>>(emptyList())
    val reminders = MutableStateFlow<List<Reminder>>(emptyList())
    // journalEntries declared below with logic
    val habits = MutableStateFlow<List<Habit>>(emptyList())
    val transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val financeLogs = MutableStateFlow<List<FinanceLog>>(emptyList())
    val budgets = MutableStateFlow<List<Budget>>(emptyList())
    val dashboardStats = MutableStateFlow(DashboardStats())
    val financeStats = MutableStateFlow(FinanceStats())
    val analyticsData = MutableStateFlow<AnalyticsData?>(null)
    val searchQuery = MutableStateFlow("")
    val searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val recentSearches = MutableStateFlow<List<String>>(emptyList())
    val searchFilters = MutableStateFlow(SearchFilters())
    val selectedDate = MutableStateFlow(System.currentTimeMillis())
    val events = MutableStateFlow<List<CalendarEvent>>(emptyList())

    // ======================== JOURNAL ========================
    private val _journalEntries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val journalEntries: StateFlow<List<JournalEntry>> = _journalEntries.asStateFlow()
    
    private val _journalPrompts = MutableStateFlow<List<JournalPrompt>>(emptyList())
    val journalPrompts: StateFlow<List<JournalPrompt>> = _journalPrompts.asStateFlow()

    init {
        // Launch data loading in parallel safely
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            checkCloudBackup()
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            loadFinanceData()
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            loadHabits()
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            loadJournalData()
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            loadGoals()
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            loadNotes()
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            loadDashboardStats()
        }
    }

    private suspend fun checkCloudBackup() {
        // No-op: S3 sync removed, data is synced via backend API
        _isCheckingSync.value = false
    }
    
    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            storageManager.saveUserProfile(profile)
            _userProfile.value = profile
        }
    }
    
    fun setOnboardingComplete(v: Boolean = true) {
        viewModelScope.launch {
            storageManager.setOnboardingComplete()
            _isOnboardingComplete.value = true
        }
    }
    
    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            storageManager.saveSettings(newSettings)
            _settings.value = newSettings
        }
    }
    
    fun setPinCode(pin: String?) {
        val current = _settings.value
        updateSettings(current.copy(pinCode = pin))
    }
    
    fun updateUserProfile(profile: UserProfile) {
        saveUserProfile(profile)
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            storageManager.clearAllData()
            _userProfile.value = UserProfile()
            _isOnboardingComplete.value = false
        }
    }
    
    val lastSyncTime = MutableStateFlow(System.currentTimeMillis())
    
    fun syncToCloud() {
        // No-op: S3 sync removed, data syncs through backend API automatically
    }
    
    fun syncFromCloud() {
        // No-op: S3 sync removed, data syncs through backend API automatically
    }
    
    fun logout() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                apiService.logout()
            } catch (_: Exception) {}
            storageManager.clearAllData()
            _userProfile.value = UserProfile()
            _isOnboardingComplete.value = false
        }
    }
    
    fun exportDataToFile(context: Context): android.net.Uri? {
        return try {
            val json = storageManager.exportAllData()
            val fileName = "planner_backup_${System.currentTimeMillis()}.json"
            val file = java.io.File(context.cacheDir, fileName)
            file.writeText(json)
            androidx.core.content.FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun importData(json: String): Boolean {
         val success = storageManager.importAllData(json)
         if(success) {
             // Refresh data
             loadFinanceData()
             loadGoals()
             loadHabits()
             loadJournalData()
             loadNotes()
         }
         return success
     }
     
    fun initializeAutoSync() {}

    private fun loadNotes() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                notes.value = dataRepository.getNotes()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadFinanceData() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                transactions.value = dataRepository.getTransactions()
                val currentBudgets = dataRepository.getBudgets()
                if (currentBudgets.isEmpty()) {
                     ensureDefaultBudgets()
                     budgets.value = storageManager.getBudgets()
                } else {
                     budgets.value = currentBudgets
                }
                financeLogs.value = dataRepository.getFinanceLogs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadHabits() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val loaded = dataRepository.getHabits()
                if (loaded.isEmpty()) {
                    ensureDefaultHabits(loaded)
                } else {
                    habits.value = loaded
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadJournalData() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                _journalEntries.value = dataRepository.getJournalEntries()
                val prompts = storageManager.getJournalPrompts()
                if (prompts.isEmpty()) {
                    initializeDefaultPrompts()
                } else {
                    _journalPrompts.value = prompts
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadGoals() {
         viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val loaded = dataRepository.getGoals()
                if (loaded.isEmpty()) {
                    ensureDefaultGoals()
                    goals.value = storageManager.getGoals()
                } else {
                    goals.value = loaded
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // DEFAULT DATA GENERATORS
    
    private fun ensureDefaultBudgets() {
        val defaults = listOf(
            Budget(category = TransactionCategory.FOOD, limitAmount = 500.0, spentAmount = 0.0),
            Budget(category = TransactionCategory.TRANSPORT, limitAmount = 300.0, spentAmount = 0.0),
            Budget(category = TransactionCategory.ENTERTAINMENT, limitAmount = 200.0, spentAmount = 0.0),
            Budget(category = TransactionCategory.SHOPPING, limitAmount = 400.0, spentAmount = 0.0)
        )
        defaults.forEach { storageManager.addBudget(it) }
    }
    
    private fun ensureDefaultGoals() {
        // Use the pre-defined default goals compatible with current Model
        val defaults = DefaultGoals.goals
        defaults.forEach { storageManager.updateGoal(it) }
    }

    private fun ensureDefaultHabits(currentHabits: List<Habit>) {
        val defaults = listOf(
            Habit(title = "Drink Water", description = "Stay hydrated with 8 glasses a day", icon = "💧", iconColor = 0xFF2196F3, type = HabitType.QUANTITATIVE, targetValue = 8f, unit = "glasses", timeOfDay = HabitTimeOfDay.ANY_TIME, goalId = null),
            Habit(title = "Read Books", description = "Read at least 20 pages", icon = "📚", iconColor = 0xFF9C27B0, type = HabitType.QUANTITATIVE, targetValue = 20f, unit = "pages", timeOfDay = HabitTimeOfDay.EVENING, goalId = null),
            Habit(title = "Morning Workout", description = "Start the day with energy", icon = "💪", iconColor = 0xFFF44336, type = HabitType.YES_NO, timeOfDay = HabitTimeOfDay.MORNING, goalId = null),
            Habit(title = "Meditation", description = "Mindfulness session", icon = "🧘", iconColor = 0xFF4CAF50, type = HabitType.TIMER, targetValue = 10f, unit = "mins", timeOfDay = HabitTimeOfDay.MORNING, goalId = null),
            Habit(title = "Journaling", description = "Reflect on the day", icon = "✍️", iconColor = 0xFFFFC107, type = HabitType.YES_NO, timeOfDay = HabitTimeOfDay.EVENING, goalId = null)
        )
        
        val missingDefaults = defaults.filter { default -> 
            currentHabits.none { it.title == default.title } 
        }
        
        if (missingDefaults.isNotEmpty()) {
            missingDefaults.forEach { storageManager.addHabit(it) }
            habits.value = currentHabits + missingDefaults
        } else {
            habits.value = currentHabits
        }
    }

    // JOURNAL LOGIC
    private fun initializeDefaultPrompts() {
        val defaults = listOf(
            JournalPrompt(text = "What is one thing that made you smile today?", category = PromptCategory.GRATITUDE),
            JournalPrompt(text = "What challenge did you overcome recently?", category = PromptCategory.REFLECTION),
            JournalPrompt(text = "What form of self-care did you practice today?", category = PromptCategory.SELF_IMPROVEMENT),
            JournalPrompt(text = "What is a goal you want to focus on this week?", category = PromptCategory.GOAL_REVIEW),
            JournalPrompt(text = "Who are you grateful for in your life right now?", category = PromptCategory.GRATITUDE),
            JournalPrompt(text = "What did you learn today?", category = PromptCategory.SELF_IMPROVEMENT)
        )
        storageManager.saveJournalPrompts(defaults)
        _journalPrompts.value = defaults
    }
    
    fun addJournalEntry(entry: JournalEntry) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                storageManager.addJournalEntry(entry)
                loadJournalData()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun deleteJournalEntry(entryId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                storageManager.deleteJournalEntry(entryId)
                loadJournalData()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getJournalEntryForDate(d: Long): JournalEntry? {
        val dayStart = getStartOfDay(d)
        val dayEnd = dayStart + 24 * 60 * 60 * 1000 - 1
        return _journalEntries.value.find { it.date in dayStart..dayEnd }
    }
    
    fun getDailyPrompt(): JournalPrompt? {
        val prompts = _journalPrompts.value
        if (prompts.isEmpty()) return null
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        return prompts[dayOfYear % prompts.size]
    }
    
    fun getJournalStats(): JournalStats {
        val entries = _journalEntries.value
        return JournalStats(
            totalEntries = entries.size,
            entriesThisMonth = entries.count { 
                val c = java.util.Calendar.getInstance()
                c.timeInMillis = it.date
                val current = java.util.Calendar.getInstance()
                c.get(java.util.Calendar.MONTH) == current.get(java.util.Calendar.MONTH)
            },
            currentStreak = 0, // Placeholder
            longestStreak = 0 
        )
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun addNote(n: Note) {
        viewModelScope.launch {
            storageManager.addNote(n)
            
        }
    }
    
    fun updateNote(n: Note) {
        viewModelScope.launch {
            storageManager.updateNote(n)
            loadNotes()
            
        }
    }
    
    fun deleteNote(id: String) {
        viewModelScope.launch {
            storageManager.deleteNote(id)
            loadNotes()
            
        }
    }
    
    fun toggleNotePin(id: String) {
        viewModelScope.launch {
            val note = notes.value.find { it.id == id } ?: return@launch
            val updated = note.copy(isPinned = !note.isPinned)
            storageManager.updateNote(updated)
            loadNotes()
            
        }
    }
    fun getUserGreeting(): String = "Hello!"
    fun getUpcomingTasks(): List<Task> = emptyList()
    
    fun getGoalById(id: String): Goal? {
        return goals.value.find { it.id == id }
    }
    
    fun getAllItemsForDate(d: Long): List<CalendarItem> {
        val items = mutableListOf<CalendarItem>()
        
        // Helper to check same day
        fun isDay(timestamp: Long) = com.lssgoo.planner.features.calendar.util.CalendarUtils.isSameDay(timestamp, d)

        // 1. NOTES (Created, Updated, or Reminder)
        notes.value.forEach { note ->
            if (isDay(note.createdAt)) {
                items.add(CalendarItem(
                    id = "note_new_${note.id}",
                    title = "New Note: ${note.title}",
                    description = note.content.take(60),
                    date = d,
                    type = CalendarItemType.NOTE,
                    priority = note.priority,
                    color = note.color
                ))
            } else if (isDay(note.updatedAt)) {
                items.add(CalendarItem(
                    id = "note_upd_${note.id}",
                    title = "Updated Note: ${note.title}",
                    description = note.content.take(60),
                    date = d,
                    type = CalendarItemType.NOTE,
                    priority = note.priority,
                    color = note.color
                ))
            }
            if (note.reminderTime?.let { isDay(it) } == true) {
                items.add(CalendarItem(
                    id = "note_rem_${note.id}",
                    title = "Reminder: ${note.title}",
                    description = note.content.take(60),
                    date = d,
                    type = CalendarItemType.REMINDER,
                    priority = note.priority,
                    color = note.color
                ))
            }
        }

        // 2. GOALS (Created, Updated, or Milestones)
        goals.value.forEach { goal ->
            if (isDay(goal.createdAt)) {
                items.add(CalendarItem(
                    id = "goal_new_${goal.id}",
                    title = "New Goal: ${goal.title}",
                    description = goal.description,
                    date = d,
                    type = CalendarItemType.GOAL,
                    priority = ItemPriority.P2,
                    color = goal.color
                ))
            }
            goal.milestones.forEach { milestone ->
                if (milestone.completedAt?.let { isDay(it) } == true) {
                    items.add(CalendarItem(
                        id = "milestone_comp_${milestone.id}",
                        title = "Milestone: ${milestone.title}",
                        description = "Achieved in goal: ${goal.title}",
                        date = d,
                        type = CalendarItemType.GOAL_MILESTONE,
                        priority = ItemPriority.P1,
                        color = goal.color,
                        isCompleted = true
                    ))
                }
            }
        }

        // 3. TASKS (Due)
        tasks.value.forEach { task ->
            if (task.dueDate?.let { isDay(it) } == true) {
                items.add(CalendarItem(
                    id = "task_due_${task.id}",
                    title = "Task Due: ${task.title}",
                    description = task.description,
                    date = d,
                    type = CalendarItemType.TASK,
                    priority = ItemPriority.P4,
                    color = 0xFF2196F3,
                    isCompleted = task.isCompleted
                ))
            }
        }

        // 4. JOURNAL (Entries)
        journalEntries.value.forEach { entry ->
            if (isDay(entry.date)) {
                items.add(CalendarItem(
                    id = "journal_${entry.id}",
                    title = "Journal: ${entry.mood.emoji} ${entry.title.ifBlank { "Daily Reflection" }}",
                    description = entry.content.take(60),
                    date = d,
                    type = CalendarItemType.JOURNAL,
                    priority = ItemPriority.P5,
                    color = entry.mood.color
                ))
            }
        }

        // 5. FINANCE (Transactions)
        transactions.value.forEach { txn ->
            if (isDay(txn.date)) {
                items.add(CalendarItem(
                    id = "txn_${txn.id}",
                    title = "Finance: ${txn.category.icon} ${txn.type} ₹${txn.amount}",
                    description = txn.note,
                    date = d,
                    type = CalendarItemType.FINANCE,
                    priority = if (txn.amount > 5000) ItemPriority.P1 else ItemPriority.P3,
                    color = if (txn.type.name == "EXPENSE") 0xFFF44336 else 0xFF4CAF50
                ))
            }
        }

        // 6. HABITS (Completions)
        habits.value.forEach { habit ->
            val entries = storageManager.getHabitEntries(habit.id)
            entries.forEach { entry ->
                if (isDay(entry.date) && entry.isCompleted) {
                    items.add(CalendarItem(
                        id = "habit_${entry.id}",
                        title = "Habit: ${habit.icon} ${habit.title}",
                        description = "Completed target: ${habit.targetValue} ${habit.unit ?: ""}",
                        date = d,
                        type = CalendarItemType.HABIT,
                        priority = ItemPriority.P6,
                        color = habit.iconColor,
                        isCompleted = true
                    ))
                }
            }
        }

        // 7. EVENTS & REMINDERS
        events.value.forEach { event ->
            if (isDay(event.date)) {
                items.add(CalendarItem(
                    id = "event_${event.id}",
                    title = "Event: ${event.title}",
                    description = event.description,
                    date = d,
                    type = CalendarItemType.EVENT,
                    priority = ItemPriority.P3,
                    color = 0xFFFFC107
                ))
            }
        }
        
        reminders.value.forEach { reminder ->
            if (isDay(reminder.reminderTime)) {
                items.add(CalendarItem(
                    id = "rem_${reminder.id}",
                    title = "Reminder: ${reminder.title}",
                    description = reminder.description,
                    date = d,
                    type = CalendarItemType.REMINDER,
                    priority = reminder.priority,
                    color = reminder.color
                ))
            }
        }

        return items.sortedBy { it.priority.level }
    }

    // ======================== FEATURE METHODS RESTORED ========================

    fun refreshAnalytics() {}
    fun updateSearchQuery(q: String) {
        searchQuery.value = q
        // perform search logic if needed
    }
    fun updateSearchFilters(f: SearchFilters) {
        searchFilters.value = f
    }
    fun clearSearch() {
        searchQuery.value = ""
        searchResults.value = emptyList()
    }
    fun setSelectedDate(t: Long) {
        selectedDate.value = t
    }
    fun addEvent(e: CalendarEvent) {
        viewModelScope.launch {
            storageManager.addEvent(e)
            
        }
    }
    fun deleteEvent(id: String) {
        viewModelScope.launch {
            storageManager.deleteEvent(id)
            
        }
    }
    fun toggleMilestone(g: String, m: String) {
        // Toggle milestone logic
        val goal = goals.value.find { it.id == g } ?: return
        val updatedMilestones = goal.milestones.map { 
            if (it.id == m) it.copy(isCompleted = !it.isCompleted, completedAt = if (!it.isCompleted) System.currentTimeMillis() else null) else it 
        }
        val updatedGoal = goal.copy(milestones = updatedMilestones)
        viewModelScope.launch {
            storageManager.updateGoal(updatedGoal)
            loadGoals()
            
        }
    }
    fun toggleTaskCompletion(t: String) {
        viewModelScope.launch {
            storageManager.toggleTaskCompletion(t)
            
        }
    }
    fun toggleReminderEnabled(id: String) {
        viewModelScope.launch {
            storageManager.toggleReminderEnabled(id)
            
        }
    }
    fun deleteTask(t: String) {
        viewModelScope.launch {
            storageManager.deleteTask(t)
            
        }
    }
    fun addTask(t: Task) {
        viewModelScope.launch {
            storageManager.addTask(t)
            
        }
    }
    fun updateTask(t: Task) {
        viewModelScope.launch {
            storageManager.updateTask(t)
            
        }
    }
    fun addReminder(r: Reminder) {
        viewModelScope.launch {
            storageManager.addReminder(r)
            if (r.isEnabled && r.reminderTime > System.currentTimeMillis()) {
                 com.lssgoo.planner.notification.PlannerNotificationManager(getApplication()).scheduleReminder(
                     notificationId = r.notificationId,
                     title = r.title,
                     message = r.description.ifBlank { "Reminder" },
                     triggerTime = r.reminderTime,
                     itemId = r.id
                 )
            }
            
        }
    }
    fun updateReminder(r: Reminder) {
        viewModelScope.launch {
            storageManager.updateReminder(r)
             val manager = com.lssgoo.planner.notification.PlannerNotificationManager(getApplication())
             if (r.isEnabled && r.reminderTime > System.currentTimeMillis()) {
                 manager.scheduleReminder(
                     notificationId = r.notificationId,
                     title = r.title,
                     message = r.description.ifBlank { "Reminder" },
                     triggerTime = r.reminderTime,
                     itemId = r.id
                 )
             } else {
                 manager.cancelReminder(r.notificationId)
             }
            
        }
    }
    fun deleteReminder(id: String) {
        viewModelScope.launch {
             // We need to fetch it first to get notificationId?
             // Or assumes storageManager handles it?
             // Ideally we should know notificationId.
             // For now, if we delete, we might miss cancelling if we don't have the object.
             // Let's try to get it first.
             val reminder = reminders.value.find { it.id == id }
             if (reminder != null) {
                  com.lssgoo.planner.notification.PlannerNotificationManager(getApplication()).cancelReminder(reminder.notificationId)
             }
            storageManager.deleteReminder(id)
            
        }
    }

    // HABITS
    fun addHabit(h: Habit) {
        viewModelScope.launch {
            storageManager.addHabit(h)
            
        }
    }
    
    fun getHabitStats(id: String): HabitStats {
        val entries = storageManager.getHabitEntries(id).sortedBy { it.date }
        val totalDays = entries.size 
        val completions = entries.count { it.isCompleted }
        
        val heatmap = entries.associate { it.date to if (it.isCompleted) (it.mood?.ordinal?.plus(1) ?: 2) else 0 }
        
        val cal = java.util.Calendar.getInstance()
        val last7 = (0..6).map { i ->
             val d = getStartOfDay(cal.timeInMillis)
             cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
             entries.any { it.date == d && it.isCompleted }
        }.reversed()

        return HabitStats(
            habitId = id,
            currentStreak = completions, 
            totalCompletions = completions,
            completionRate = if (totalDays > 0) completions.toFloat() / 30f else 0f,
            heatmapData = heatmap,
            last7Days = last7
        )
    }

    fun getMonthActivityCounts(month: java.util.Calendar): Map<Int, Int> {
        val counts = mutableMapOf<Int, Int>()
        val cal = month.clone() as java.util.Calendar
        val daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        
        for (day in 1..daysInMonth) {
            cal.set(java.util.Calendar.DAY_OF_MONTH, day)
            counts[day] = getAllItemsForDate(cal.timeInMillis).size
        }
        return counts
    }

    fun getGlobalHeatmap(): Map<Long, Int> {
        val allEntries = habits.value.flatMap { storageManager.getHabitEntries(it.id) }
        return allEntries.groupBy { it.date }
            .mapValues { (_, entries) -> 
                val count = entries.count { it.isCompleted }
                when {
                    count == 0 -> 0
                    count < 3 -> 1
                    count < 6 -> 2
                    count < 9 -> 3
                    else -> 4
                }
            }
    }

    fun getHabitEntriesForDate(d: Long): List<HabitEntry> = storageManager.getHabitEntriesForDate(d)
    
    fun toggleHabitEntry(id: String, date: Long, value: Float = 1f, mood: HabitMood? = null) {
        viewModelScope.launch {
            val existing = storageManager.getHabitEntries(id).find { it.date == date }
            if (existing != null) {
                if (existing.isCompleted) {
                    storageManager.deleteHabitEntry(existing.id)
                }
            } else {
                val entry = HabitEntry(
                    habitId = id,
                    date = date,
                    isCompleted = true,
                    value = value,
                    mood = mood
                )
                storageManager.addHabitEntry(entry)
            }
            loadHabits() 
            
        }
    }

    // FINANCE
    fun addTransaction(tr: Transaction) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            dataRepository.createTransaction(tr)
            loadFinanceData()
            
        }
    }
    
    fun updateTransaction(tr: Transaction) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val txns = storageManager.getTransactions().toMutableList()
            val idx = txns.indexOfFirst { it.id == tr.id }
            if (idx >= 0) txns[idx] = tr
            storageManager.saveTransactions(txns)
            loadFinanceData()
            
        }
    }
    
    fun deleteTransaction(id: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val txns = storageManager.getTransactions().filter { it.id != id }
            storageManager.saveTransactions(txns)
            loadFinanceData()
            
        }
    }

    fun addBudget(b: Budget) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            storageManager.addBudget(b)
            loadFinanceData()
            
        }
    }
    
    fun removeBudget(id: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val budgetList = storageManager.getBudgets().filter { it.id != id }
            storageManager.saveBudgets(budgetList)
            loadFinanceData()
            
        }
    }

    fun settleDebt(id: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val txns = storageManager.getTransactions().toMutableList()
            val idx = txns.indexOfFirst { it.id == id }
            if (idx >= 0) txns[idx] = txns[idx].copy(isSettled = true)
            storageManager.saveTransactions(txns)
            loadFinanceData()
            
        }
    }

    fun exportFinanceCSV(): String {
        val txns = storageManager.getTransactions()
        val sb = StringBuilder("Date,Type,Category,Amount,Note,Person\n")
        txns.forEach { t ->
            sb.appendLine("${t.date},${t.type},${t.category},${t.amount},${t.note},${t.personName ?: ""}")
        }
        return sb.toString()
    }

    fun loadDashboardStats() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            dashboardStats.value = storageManager.getDashboardStats()
        }
    }

}
