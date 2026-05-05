package com.lssgoo.planner.features.goals.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import com.lssgoo.planner.features.goals.components.*
import com.lssgoo.planner.features.goals.models.GoalStatus
import com.lssgoo.planner.features.goals.models.Milestone
import com.lssgoo.planner.ui.viewmodel.GoalsViewModel

enum class DetailTab { MILESTONES, NOTES, INFO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: String,
    viewModel: GoalsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.goals.collectAsState()
    val goal = goals.find { it.id == goalId }

    var showEditGoalDialog by remember { mutableStateOf(false) }
    var showAddMilestoneDialog by remember { mutableStateOf(false) }
    var milestoneToEdit by remember { mutableStateOf<Milestone?>(null) }
    var milestoneToComplete by remember { mutableStateOf<Milestone?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(DetailTab.MILESTONES) }
    var showStatusMenu by remember { mutableStateOf(false) }

    if (goal == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(16.dp))
                Text("Goal not found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onBack) { Text("Go Back") }
            }
        }
        return
    }

    val completedMilestones = goal.milestones.count { it.isCompleted }
    val totalMilestones = goal.milestones.size
    val progress = if (totalMilestones > 0) completedMilestones.toFloat() / totalMilestones else 0f
    val goalColor = Color(goal.color)

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column {
                    Spacer(modifier = Modifier.height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding()))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Goals",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = " > ",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = goal.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "Goal #${goal.number}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { viewModel.toggleFavorite(goal.id) }) {
                            Icon(
                                if (goal.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (goal.isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showEditGoalDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Goal", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DetailTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                label = {
                                    Text(
                                        when (tab) {
                                            DetailTab.MILESTONES -> "Milestones"
                                            DetailTab.NOTES -> "Notes & Motivation"
                                            DetailTab.INFO -> "Info & Tips"
                                        },
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        when (tab) {
                                            DetailTab.MILESTONES -> Icons.Default.Flag
                                            DetailTab.NOTES -> Icons.Default.StickyNote2
                                            DetailTab.INFO -> Icons.Outlined.Info
                                        },
                                        null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == DetailTab.MILESTONES) {
                FloatingActionButton(
                    onClick = { showAddMilestoneDialog = true },
                    containerColor = goalColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Milestone")
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
        ) {
            item {
                GoalDetailHeader(
                    goal = goal,
                    progress = progress,
                    completedMilestones = completedMilestones,
                    totalMilestones = totalMilestones,
                    onStatusChange = { newStatus ->
                        viewModel.updateGoal(goal.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            when (selectedTab) {
                DetailTab.MILESTONES -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Milestones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Surface(
                                color = goalColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "$completedMilestones / $totalMilestones",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = goalColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(goal.milestones.sortedBy { it.orderIndex }, key = { it.id }) { milestone ->
                        MilestoneItem(
                            milestone = milestone,
                            goalColor = goalColor,
                            onToggle = {
                                if (milestone.isCompleted) {
                                    viewModel.toggleMilestone(goal.id, milestone.id)
                                } else {
                                    milestoneToComplete = milestone
                                }
                            },
                            onEdit = { milestoneToEdit = milestone },
                            onDelete = { viewModel.deleteMilestone(goal.id, milestone.id) },
                            modifier = Modifier
                                .animateItem()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    if (goal.milestones.isEmpty()) {
                        item {
                            EmptyMilestonesState(goalColor = goalColor)
                        }
                    }
                }

                DetailTab.NOTES -> {
                    item {
                        GoalNotesSection(
                            goal = goal,
                            onUpdateGoal = { viewModel.updateGoal(it) }
                        )
                    }
                }

                DetailTab.INFO -> {
                    item {
                        GoalInfoSection(goal = goal)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Goal?") },
            text = { Text("This will permanently delete \"${goal.title}\" and all its milestones. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGoal(goal.id)
                        showDeleteConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showEditGoalDialog) {
        AddEditGoalDialog(
            goal = goal,
            onDismiss = { showEditGoalDialog = false },
            onConfirm = {
                viewModel.updateGoal(it)
                showEditGoalDialog = false
            }
        )
    }

    if (showAddMilestoneDialog) {
        AddEditMilestoneDialog(
            onDismiss = { showAddMilestoneDialog = false },
            onConfirm = {
                val updatedMilestones = goal.milestones + it.copy(orderIndex = goal.milestones.size)
                viewModel.updateGoal(goal.copy(milestones = updatedMilestones))
                showAddMilestoneDialog = false
            }
        )
    }

    if (milestoneToEdit != null) {
        val m = milestoneToEdit!!
        AddEditMilestoneDialog(
            milestone = m,
            onDismiss = { milestoneToEdit = null },
            onConfirm = { updated ->
                viewModel.updateMilestone(goal.id, updated)
                milestoneToEdit = null
            }
        )
    }

    if (milestoneToComplete != null) {
        val m = milestoneToComplete!!
        MilestoneCompletionDialog(
            milestone = m,
            onDismiss = { milestoneToComplete = null },
            onConfirm = { finishedMilestone ->
                viewModel.updateMilestone(goal.id, finishedMilestone)
                milestoneToComplete = null
            }
        )
    }
}
