package com.lssgoo.planner.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.lssgoo.planner.features.goals.models.GoalCategory

/**
 * Extension function for GoalCategory to map category to Material Icon
 */
fun GoalCategory.getIcon(): ImageVector {
    return when (this) {
        GoalCategory.HEALTH -> Icons.Default.FitnessCenter
        GoalCategory.CAREER -> Icons.Default.Work
        GoalCategory.LEARNING -> Icons.Default.MenuBook
        GoalCategory.COMMUNICATION -> Icons.Default.RecordVoiceOver
        GoalCategory.LIFESTYLE -> Icons.Default.WbSunny
        GoalCategory.DISCIPLINE -> Icons.Default.Schedule
        GoalCategory.FINANCE -> Icons.Default.Savings
        GoalCategory.STARTUP -> Icons.Default.RocketLaunch
        GoalCategory.MINDFULNESS -> Icons.Default.SelfImprovement
        GoalCategory.PERSONAL -> Icons.Default.Person
    }
}

/**
 * Centralized Icons for the app to avoid repeated logic and fix unresolved references
 */
object AppIcons {
    val Health = Icons.Default.FitnessCenter
    val Career = Icons.Default.Work
    val Learning = Icons.Default.MenuBook
    val Communication = Icons.Default.RecordVoiceOver
    val Lifestyle = Icons.Default.WbSunny
    val Discipline = Icons.Default.Schedule
    val Finance = Icons.Default.Savings
    val Startup = Icons.Default.RocketLaunch
    val Mindfulness = Icons.Default.SelfImprovement
    val Personal = Icons.Default.Person

    val Goal = Icons.Default.Flag
    val Target = Icons.Default.Flag
    val Task = Icons.Default.TaskAlt
    val Tasks = Icons.Default.TaskAlt
    val Note = Icons.Default.StickyNote2
    val Notes = Icons.Default.StickyNote2
    val Reminder = Icons.Default.Alarm
    val Reminders = Icons.Default.Alarm
    val Event = Icons.Default.Event
    val Events = Icons.Default.Event
    val Milestone = Icons.Default.FlagCircle
    val Flag = Icons.Default.Flag
    val Journal = Icons.Default.HistoryEdu
    val Habit = Icons.Default.CheckCircle
    
    val Add = Icons.Default.Add
    val Edit = Icons.Default.Edit
    val Delete = Icons.Default.Delete
    val DeleteOutlined = Icons.Outlined.Delete
    val Search = Icons.Default.Search
    val Settings = Icons.Default.Settings
    val SettingsOutlined = Icons.Outlined.Settings
    val Profile = Icons.Default.Person
    val Notifications = Icons.Default.Notifications
    val NotificationsNone = Icons.Outlined.Notifications
    val Calendar = Icons.Default.CalendarToday
    val Dashboard = Icons.Default.Dashboard
    
    val ArrowBack = Icons.Default.ArrowBack
    val ChevronLeft = Icons.Default.ChevronLeft
    val ChevronRight = Icons.Default.ChevronRight
    val Check = Icons.Default.Check
    val CheckCircle = Icons.Default.CheckCircle
    val RadioButtonUnchecked = Icons.Outlined.RadioButtonUnchecked
    val PriorityHigh = Icons.Default.PriorityHigh
    val Schedule = Icons.Default.Schedule
    val Description = Icons.Default.Description
    val AlarmAdd = Icons.Default.AlarmAdd
    val Lightbulb = Icons.Default.Lightbulb
    val TrendingUp = Icons.Default.TrendingUp
    val PushPin = Icons.Default.PushPin
    val Trophy = Icons.Default.EmojiEvents
    val Notification = Icons.Default.Notifications
    val Celebration = Icons.Default.Celebration
    val SettingsBackupRestore = Icons.Default.SettingsBackupRestore
    val Info = Icons.Default.Info
    val Assessment = Icons.Default.Assessment
    val AccountBalanceWallet = Icons.Default.AccountBalanceWallet
    val Payments = Icons.Default.Payments
    val DonutLarge = Icons.Default.DonutLarge
}
