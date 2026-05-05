package com.lssgoo.planner.features.goals.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lssgoo.planner.data.model.Goal
import com.lssgoo.planner.data.model.GoalCategory
import com.lssgoo.planner.features.goals.models.GoalPriority
import com.lssgoo.planner.features.goals.models.GoalStatus
import com.lssgoo.planner.features.goals.models.Milestone
import com.lssgoo.planner.features.goals.models.MilestoneQuality
import com.lssgoo.planner.ui.components.AnimatedProgressBar
import com.lssgoo.planner.ui.components.getIcon
import com.lssgoo.planner.ui.theme.GradientColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CategoryCarousel(
    selectedCategory: GoalCategory?,
    onCategorySelected: (GoalCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            val isSelected = selectedCategory == null
            Surface(
                onClick = { onCategorySelected(null) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .width(80.dp)
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Dashboard,
                        null,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "All",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        items(GoalCategory.entries.toList()) { category ->
            val isSelected = selectedCategory == category
            Surface(
                onClick = { onCategorySelected(if (isSelected) null else category) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .width(80.dp)
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(category.emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        category.displayName.split(" ").first(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusFilterRow(
    selectedStatus: GoalStatus?,
    onStatusSelected: (GoalStatus?) -> Unit,
    totalCount: Int,
    inProgressCount: Int,
    completedCount: Int,
    favoriteCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChip(
            selected = selectedStatus == null,
            onClick = { onStatusSelected(null) },
            label = { Text("All ($totalCount)") },
            leadingIcon = if (selectedStatus == null) {
                { Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp)) }
            } else null
        )
        GoalStatus.entries.forEach { status ->
            val count = when (status) {
                GoalStatus.IN_PROGRESS -> inProgressCount
                GoalStatus.COMPLETED -> completedCount
                else -> 0
            }
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(if (selectedStatus == status) null else status) },
                label = {
                    Text(
                        if (count > 0) "${status.displayName} ($count)" else status.displayName,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
    }
}

@Composable
fun OverallProgressCard(
    progress: Float,
    totalGoals: Int = 0,
    completedGoals: Int = 0,
    inProgressGoals: Int = 0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(GradientColors.purpleBlue))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Overall Progress", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MiniStat(label = "Total", value = totalGoals.toString())
                        MiniStat(label = "Active", value = inProgressGoals.toString())
                        MiniStat(label = "Done", value = completedGoals.toString())
                    }
                }

                Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White,
                        strokeWidth = 8.dp,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
fun GoalDetailHeader(
    goal: Goal,
    progress: Float,
    completedMilestones: Int,
    totalMilestones: Int,
    onStatusChange: (GoalStatus) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showStatusMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(color = Color(goal.color).copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(goal.category.getIcon(), null, tint = Color(goal.color), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(goal.category.displayName, color = Color(goal.color), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                }
            }

            Box {
                Surface(
                    onClick = { showStatusMenu = true },
                    color = Color(goal.status.color).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(goal.status.displayName, color = Color(goal.status.color), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp), tint = Color(goal.status.color))
                    }
                }
                DropdownMenu(expanded = showStatusMenu, onDismissRequest = { showStatusMenu = false }) {
                    GoalStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.displayName) },
                            onClick = {
                                onStatusChange(status)
                                showStatusMenu = false
                            },
                            leadingIcon = {
                                Surface(color = Color(status.color).copy(alpha = 0.2f), shape = CircleShape) {
                                    Spacer(modifier = Modifier.size(8.dp).padding(4.dp))
                                }
                            }
                        )
                    }
                }
            }

            if (goal.priority != GoalPriority.MEDIUM) {
                GoalPriorityChip(priority = goal.priority)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(goal.color), Color(goal.color).copy(alpha = 0.6f)))),
                contentAlignment = Alignment.Center
            ) {
                Text(goal.number.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(goal.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (!goal.tags.isNullOrBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        goal.tags!!.split(",").take(3).forEach { tag ->
                            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    "#${tag.trim()}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(goal.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(20.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Progress", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(goal.color))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(60.dp), color = Color(goal.color), strokeWidth = 6.dp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                AnimatedProgressBar(progress = progress, gradientColors = listOf(Color(goal.color), Color(goal.color).copy(alpha = 0.6f)), height = 12)

                if (goal.targetDate != null || goal.startDate != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        if (goal.startDate != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Start: ${dateFormat.format(Date(goal.startDate!!))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (goal.targetDate != null) {
                            val isOverdue = goal.targetDate!! < System.currentTimeMillis() && goal.status != GoalStatus.COMPLETED
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Event, null, modifier = Modifier.size(14.dp), tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Target: ${dateFormat.format(Date(goal.targetDate!!))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MilestoneItem(
    milestone: Milestone,
    goalColor: Color,
    onToggle: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (milestone.isCompleted) {
                Color(milestone.quality?.color ?: 0xFF4CAF50).copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onToggle, onLongClick = onEdit)
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (milestone.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (milestone.isCompleted) Color(milestone.quality?.color ?: 0xFF4CAF50) else goalColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = milestone.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (milestone.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (milestone.priority != GoalPriority.MEDIUM) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(milestone.priority.emoji, fontSize = 12.sp)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (milestone.targetDate != null && !milestone.isCompleted) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(dateFormat.format(Date(milestone.targetDate!!)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (!milestone.estimatedEffort.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(milestone.estimatedEffort!!, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (milestone.isCompleted && milestone.quality != null) {
                            Surface(color = Color(milestone.quality!!.color).copy(alpha = 0.15f), shape = CircleShape) {
                                Text(
                                    milestone.quality!!.displayName,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(milestone.quality!!.color),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            milestone.rating?.let { rate ->
                                Row {
                                    repeat(rate) {
                                        Icon(Icons.Default.Star, null, modifier = Modifier.size(12.dp), tint = Color(0xFFFFC107))
                                    }
                                }
                            }
                        }
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                }
            }

            if (milestone.isCompleted && !milestone.reflection.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Outlined.FormatQuote, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            milestone.reflection!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else if (milestone.isCompleted && milestone.description != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    milestone.description!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(start = 44.dp)
                )
            }
        }
    }
}

@Composable
fun GoalInfoTipCard(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lightbulb, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pro Tips", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            TipItem("Break goals into small milestones for better tracking")
            TipItem("Set target dates to stay accountable")
            TipItem("Use tags to organize goals (e.g., \"2026\", \"Q1\")")
            TipItem("Write motivation notes to remind yourself why this matters")
            TipItem("Rate milestone completion quality for honest self-review")
        }
    }
}

@Composable
private fun TipItem(text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
        Text("  \u2022  ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
    }
}

@Composable
fun EmptyGoalsState(
    hasFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.Flag, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))
        Text(
            if (hasFilters) "No goals match your filters" else "No goals yet. Start your journey!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline
        )
        if (hasFilters) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onClearFilters) { Text("Clear Filters") }
        }
    }
}

@Composable
fun EmptyMilestonesState(goalColor: Color) {
    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Flag, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))
            Text("No milestones yet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(4.dp))
            Text("Break down your goal into actionable steps!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun GoalNotesSection(
    goal: Goal,
    onUpdateGoal: (Goal) -> Unit
) {
    var motivation by remember(goal.id) { mutableStateOf(goal.motivation ?: "") }
    var expectedOutcome by remember(goal.id) { mutableStateOf(goal.expectedOutcome ?: "") }
    var notes by remember(goal.id) { mutableStateOf(goal.notes ?: "") }
    var hasChanges by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("Notes & Motivation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = motivation,
            onValueChange = { motivation = it; hasChanges = true },
            label = { Text("Why is this goal important?") },
            placeholder = { Text("Write your motivation here...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Outlined.Lightbulb, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = expectedOutcome,
            onValueChange = { expectedOutcome = it; hasChanges = true },
            label = { Text("Expected Outcome") },
            placeholder = { Text("What will success look like?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Outlined.EmojiEvents, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it; hasChanges = true },
            label = { Text("Additional Notes") },
            placeholder = { Text("Any thoughts, resources, or references...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Outlined.StickyNote2, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) }
        )

        AnimatedVisibility(visible = hasChanges) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    onUpdateGoal(
                        goal.copy(
                            motivation = motivation.ifBlank { null },
                            expectedOutcome = expectedOutcome.ifBlank { null },
                            notes = notes.ifBlank { null },
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    hasChanges = false
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Notes")
            }
        }
    }
}

@Composable
fun GoalInfoSection(goal: Goal) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("Goal Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow(icon = Icons.Default.Numbers, label = "Goal Number", value = "#${goal.number}")
                InfoRow(icon = Icons.Default.Category, label = "Category", value = "${goal.category.emoji} ${goal.category.displayName}")
                InfoRow(icon = Icons.Outlined.Info, label = "Status", value = goal.status.displayName)
                InfoRow(icon = Icons.Default.PriorityHigh, label = "Priority", value = "${goal.priority.emoji} ${goal.priority.displayName}")

                if (!goal.tags.isNullOrBlank()) {
                    InfoRow(icon = Icons.Default.Tag, label = "Tags", value = goal.tags!!)
                }
                if (goal.startDate != null) {
                    InfoRow(icon = Icons.Default.PlayArrow, label = "Started", value = dateFormat.format(Date(goal.startDate!!)))
                }
                if (goal.targetDate != null) {
                    InfoRow(icon = Icons.Default.Event, label = "Target Date", value = dateFormat.format(Date(goal.targetDate!!)))
                }
                if (goal.completedDate != null) {
                    InfoRow(icon = Icons.Default.CheckCircle, label = "Completed", value = dateFormat.format(Date(goal.completedDate!!)))
                }
                InfoRow(icon = Icons.Default.Schedule, label = "Created", value = dateFormat.format(Date(goal.createdAt)))
                InfoRow(icon = Icons.Default.Update, label = "Last Updated", value = dateFormat.format(Date(goal.updatedAt)))

                if (goal.reminderEnabled) {
                    InfoRow(icon = Icons.Default.Notifications, label = "Reminder", value = goal.reminderFrequency ?: "Enabled")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        GoalInfoTipCard(onDismiss = {})
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
