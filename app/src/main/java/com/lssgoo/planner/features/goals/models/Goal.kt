package com.lssgoo.planner.features.goals.models

import java.util.UUID

enum class GoalStatus(val displayName: String, val color: Long) {
    NOT_STARTED("Not Started", 0xFF9E9E9E),
    IN_PROGRESS("In Progress", 0xFF2196F3),
    COMPLETED("Completed", 0xFF4CAF50),
    ON_HOLD("On Hold", 0xFFFF9800),
    ABANDONED("Abandoned", 0xFFF44336)
}

enum class GoalPriority(val displayName: String, val color: Long, val emoji: String) {
    LOW("Low", 0xFF8BC34A, "🟢"),
    MEDIUM("Medium", 0xFFFFEB3B, "🟡"),
    HIGH("High", 0xFFFF9800, "🟠"),
    CRITICAL("Critical", 0xFFF44336, "🔴")
}

/**
 * Represents a life goal
 */
data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val number: Int,
    val title: String,
    val description: String,
    val category: GoalCategory,
    val icon: String,
    val color: Long,
    val progress: Float = 0f, // 0.0 to 1.0
    val milestones: List<Milestone> = emptyList(),
    val targetDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val status: GoalStatus = GoalStatus.NOT_STARTED,
    val priority: GoalPriority = GoalPriority.MEDIUM,
    val tags: String? = null,
    val notes: String? = null,
    val startDate: Long? = null,
    val completedDate: Long? = null,
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderFrequency: String? = null,
    val motivation: String? = null,
    val expectedOutcome: String? = null
)

data class Milestone(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val targetDate: Long? = null,
    val quality: MilestoneQuality? = null,
    val rating: Int? = null, // 1-5 stars
    val orderIndex: Int = 0,
    val priority: GoalPriority = GoalPriority.MEDIUM,
    val notes: String? = null,
    val estimatedEffort: String? = null,
    val actualEffort: String? = null,
    val reflection: String? = null
)

enum class MilestoneQuality(val displayName: String, val color: Long) {
    HIGH("Full Perfection", 0xFF4CAF50),
    MID("Medium Quality", 0xFFFF9800),
    LOW("Low Effort", 0xFFF44336)
}

enum class GoalCategory(val displayName: String, val emoji: String, val iconName: String) {
    HEALTH("Health & Fitness", "💪", "FitnessCenter"),
    CAREER("Career", "💼", "Work"),
    LEARNING("Learning", "📚", "MenuBook"),
    COMMUNICATION("Communication", "🗣️", "RecordVoiceOver"),
    LIFESTYLE("Lifestyle", "🌅", "WbSunny"),
    DISCIPLINE("Discipline", "⏰", "Schedule"),
    FINANCE("Finance", "💰", "Savings"),
    STARTUP("Startup", "🚀", "RocketLaunch"),
    MINDFULNESS("Mindfulness", "🧘", "SelfImprovement"),
    PERSONAL("Personal", "👤", "Person")
}

/**
 * Default goals based on user's plan
 */
object DefaultGoals {
    val goals = listOf(
        Goal(
            number = 1,
            title = "Health & Fitness First",
            description = "Be very serious about health and fitness. Stop fast food & invest that money in good & healthy food.",
            category = GoalCategory.HEALTH,
            icon = "fitness",
            color = 0xFF4CAF50, // Green
            milestones = listOf(
                Milestone(title = "No fast food for 1 week"),
                Milestone(title = "No fast food for 1 month"),
                Milestone(title = "Create a healthy meal plan"),
                Milestone(title = "Start morning workout routine"),
                Milestone(title = "Track food expenses weekly"),
                Milestone(title = "Invest saved money in healthy food"),
                Milestone(title = "Complete 3 months without fast food"),
                Milestone(title = "Complete 6 months without fast food"),
                Milestone(title = "Full year of healthy eating")
            )
        ),
        Goal(
            number = 2,
            title = "1 Crore Package",
            description = "Work with full focus toward a 1 Crore package.",
            category = GoalCategory.CAREER,
            icon = "work",
            color = 0xFF2196F3, // Blue
            milestones = listOf(
                Milestone(title = "Update resume and portfolio"),
                Milestone(title = "Identify target companies"),
                Milestone(title = "Master DSA fundamentals"),
                Milestone(title = "Complete System Design basics"),
                Milestone(title = "Apply to 10 companies"),
                Milestone(title = "Clear 5 coding rounds"),
                Milestone(title = "Get interview calls from top companies"),
                Milestone(title = "Clear technical interviews"),
                Milestone(title = "Negotiate and secure the package")
            )
        ),
        Goal(
            number = 3,
            title = "Management & Human Behavior",
            description = "Learn management and human behavior deeply.",
            category = GoalCategory.LEARNING,
            icon = "psychology",
            color = 0xFF9C27B0, // Purple
            milestones = listOf(
                Milestone(title = "Read 'How to Win Friends and Influence People'"),
                Milestone(title = "Read 'The 7 Habits of Highly Effective People'"),
                Milestone(title = "Study body language basics"),
                Milestone(title = "Learn negotiation techniques"),
                Milestone(title = "Read about leadership principles"),
                Milestone(title = "Practice active listening daily"),
                Milestone(title = "Study emotional intelligence"),
                Milestone(title = "Apply learnings in real situations"),
                Milestone(title = "Mentor someone for 3 months")
            )
        ),
        Goal(
            number = 4,
            title = "Think Before Speaking",
            description = "Think before speaking; choose words carefully.",
            category = GoalCategory.COMMUNICATION,
            icon = "chat",
            color = 0xFFFF9800, // Orange
            milestones = listOf(
                Milestone(title = "Practice 3-second pause before responding"),
                Milestone(title = "Journal daily conversations"),
                Milestone(title = "Identify trigger situations"),
                Milestone(title = "Learn diplomatic responses"),
                Milestone(title = "Practice in important meetings"),
                Milestone(title = "Get feedback from trusted people"),
                Milestone(title = "Master calm communication under stress"),
                Milestone(title = "Build reputation for thoughtful responses")
            )
        ),
        Goal(
            number = 5,
            title = "Early Rising",
            description = "Wake up early every day and fix your sleep timing.",
            category = GoalCategory.LIFESTYLE,
            icon = "alarm",
            color = 0xFFE91E63, // Pink
            milestones = listOf(
                Milestone(title = "Set fixed sleep time (10 PM)"),
                Milestone(title = "Wake up at 5:30 AM for 1 week"),
                Milestone(title = "No phone 1 hour before sleep"),
                Milestone(title = "Create morning routine"),
                Milestone(title = "Maintain routine for 1 month"),
                Milestone(title = "Maintain routine for 3 months"),
                Milestone(title = "Maintain routine for 6 months"),
                Milestone(title = "Full year of consistent sleep schedule")
            )
        ),
        Goal(
            number = 6,
            title = "Discipline & Consistency",
            description = "Stay disciplined and consistent every day.",
            category = GoalCategory.DISCIPLINE,
            icon = "schedule",
            color = 0xFF795548, // Brown
            milestones = listOf(
                Milestone(title = "Create daily schedule"),
                Milestone(title = "Follow schedule for 1 week"),
                Milestone(title = "Track habits daily"),
                Milestone(title = "Build 1 new habit per month"),
                Milestone(title = "Maintain 21-day streaks"),
                Milestone(title = "Complete 66-day habit formation"),
                Milestone(title = "Review and adjust quarterly"),
                Milestone(title = "Achieve 80% consistency for year")
            )
        ),
        Goal(
            number = 7,
            title = "Money Management",
            description = "Before spending a single rupee, think twice and thrice; learn money management and start investing.",
            category = GoalCategory.FINANCE,
            icon = "savings",
            color = 0xFF009688, // Teal
            milestones = listOf(
                Milestone(title = "Track all expenses for 1 month"),
                Milestone(title = "Create monthly budget"),
                Milestone(title = "Open investment account"),
                Milestone(title = "Learn about mutual funds"),
                Milestone(title = "Start SIP with ₹1000"),
                Milestone(title = "Read 'Rich Dad Poor Dad'"),
                Milestone(title = "Build emergency fund (3 months)"),
                Milestone(title = "Increase investment to 30% of income"),
                Milestone(title = "Diversify investments")
            )
        ),
        Goal(
            number = 8,
            title = "Communication & Confidence",
            description = "Improve communication and confidence while speaking.",
            category = GoalCategory.COMMUNICATION,
            icon = "mic",
            color = 0xFFFF5722, // Deep Orange
            milestones = listOf(
                Milestone(title = "Practice speaking in mirror daily"),
                Milestone(title = "Join a speaking club/group"),
                Milestone(title = "Give 1 presentation per month"),
                Milestone(title = "Record and review yourself"),
                Milestone(title = "Learn from TED talks"),
                Milestone(title = "Speak confidently in meetings"),
                Milestone(title = "Handle Q&A sessions well"),
                Milestone(title = "Give a public speech")
            )
        ),
        Goal(
            number = 9,
            title = "Long-term Life Plan",
            description = "Set a long-term life plan and review progress regularly.",
            category = GoalCategory.MINDFULNESS,
            icon = "timeline",
            color = 0xFF607D8B, // Blue Grey
            milestones = listOf(
                Milestone(title = "Write 5-year vision"),
                Milestone(title = "Write 10-year vision"),
                Milestone(title = "Break down into yearly goals"),
                Milestone(title = "Create quarterly milestones"),
                Milestone(title = "Set monthly review schedule"),
                Milestone(title = "Complete first quarterly review"),
                Milestone(title = "Adjust plan based on progress"),
                Milestone(title = "Complete mid-year review"),
                Milestone(title = "Complete year-end comprehensive review")
            )
        ),
        Goal(
            number = 10,
            title = "Focus on StartUp",
            description = "Focus on StartUp.",
            category = GoalCategory.STARTUP,
            icon = "rocket_launch",
            color = 0xFF673AB7, // Deep Purple
            milestones = listOf(
                Milestone(title = "Identify problem to solve"),
                Milestone(title = "Research market and competitors"),
                Milestone(title = "Create MVP plan"),
                Milestone(title = "Build prototype"),
                Milestone(title = "Get first 10 users"),
                Milestone(title = "Collect feedback"),
                Milestone(title = "Iterate and improve"),
                Milestone(title = "Get first paying customer"),
                Milestone(title = "Plan for scale")
            )
        ),
        Goal(
            number = 11,
            title = "Digital Detox",
            description = "Avoiding the phone until not needed.",
            category = GoalCategory.DISCIPLINE,
            icon = "phone_disabled",
            color = 0xFF3F51B5, // Indigo
            milestones = listOf(
                Milestone(title = "Track daily phone usage"),
                Milestone(title = "Set screen time limits"),
                Milestone(title = "No phone in first hour of day"),
                Milestone(title = "No phone during meals"),
                Milestone(title = "Delete time-wasting apps"),
                Milestone(title = "Phone-free bedroom"),
                Milestone(title = "Reduce to 2 hours daily"),
                Milestone(title = "Reduce to 1.5 hours daily"),
                Milestone(title = "Maintain healthy phone habits")
            )
        )
    )
}
