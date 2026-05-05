package com.lssgoo.planner.features.goals.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.lssgoo.planner.features.goals.models.GoalPriority
import com.lssgoo.planner.features.goals.models.GoalStatus
import com.lssgoo.planner.ui.components.AnimatedProgressBar
import com.lssgoo.planner.ui.components.getIcon
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val completedMilestones = goal.milestones.count { it.isCompleted }
    val totalMilestones = goal.milestones.size
    val progress = if (totalMilestones > 0) completedMilestones.toFloat() / totalMilestones else goal.progress
    val goalColor = Color(goal.color)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(goalColor, goalColor.copy(alpha = 0.6f)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(goal.number.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (goal.isPinned) {
                                Icon(Icons.Default.PushPin, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GoalStatusChip(status = goal.status, small = true)
                            if (goal.priority != GoalPriority.MEDIUM) {
                                GoalPriorityChip(priority = goal.priority, small = true)
                            }
                        }
                    }
                }

                IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(36.dp)) {
                    Icon(
                        if (goal.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (goal.isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = goal.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = goalColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(goal.category.getIcon(), null, tint = goalColor, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(goal.category.displayName, color = goalColor, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (goal.targetDate != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(goal.targetDate!!)),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (goal.targetDate!! < System.currentTimeMillis() && goal.status != GoalStatus.COMPLETED) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (totalMilestones > 0) {
                        Icon(Icons.Default.Flag, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Text(
                            " $completedMilestones/$totalMilestones",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = goalColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedProgressBar(
                progress = progress,
                gradientColors = listOf(goalColor, goalColor.copy(alpha = 0.6f))
            )
        }
    }
}

@Composable
fun GoalGridCard(
    goal: Goal,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val completedMilestones = goal.milestones.count { it.isCompleted }
    val totalMilestones = goal.milestones.size
    val progress = if (totalMilestones > 0) completedMilestones.toFloat() / totalMilestones else goal.progress
    val goalColor = Color(goal.color)

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(goalColor, goalColor.copy(alpha = 0.6f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(goal.number.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Row {
                    if (goal.isPinned) {
                        Icon(Icons.Default.PushPin, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(28.dp)) {
                        Icon(
                            if (goal.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            null,
                            tint = if (goal.isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            GoalStatusChip(status = goal.status, small = true)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (totalMilestones > 0) {
                    Text(
                        "$completedMilestones/$totalMilestones",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = goalColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            AnimatedProgressBar(
                progress = progress,
                gradientColors = listOf(goalColor, goalColor.copy(alpha = 0.6f)),
                height = 6
            )
        }
    }
}

@Composable
fun GoalStatusChip(status: GoalStatus, small: Boolean = false) {
    Surface(
        color = Color(status.color).copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = if (small) 6.dp else 10.dp, vertical = if (small) 2.dp else 4.dp),
            style = if (small) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            color = Color(status.color),
            fontWeight = FontWeight.SemiBold,
            fontSize = if (small) 10.sp else 12.sp
        )
    }
}

@Composable
fun GoalPriorityChip(priority: GoalPriority, small: Boolean = false) {
    Surface(
        color = Color(priority.color).copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (small) 6.dp else 10.dp, vertical = if (small) 2.dp else 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = priority.emoji,
                fontSize = if (small) 10.sp else 12.sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = priority.displayName,
                style = if (small) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                color = Color(priority.color),
                fontWeight = FontWeight.SemiBold,
                fontSize = if (small) 10.sp else 12.sp
            )
        }
    }
}
